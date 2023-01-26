package com.simtechdata.finalgcode.processing;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.Range;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZHopping {

	private static       String              gcode;
	private static final LinkedList<Profile> profileList = new LinkedList<>();
	private static final String              ret         = System.getProperty("line.separator");
	private static       int                 lastLine    = 0;

	public static void clearZHops() {
		profileList.clear();
	}

	public static boolean insertZHop(double height, int startLayer, int endLayer) {
		boolean added = true;
		List<Range<Integer>> rangeList = new ArrayList<>();

		for (Profile profile : profileList) {
			int start = profile.startLayer;
			int end   = profile.endLayer;
			rangeList.add(Range.between(profile.startLayer, profile.endLayer));
		}

		for (Range range : rangeList) {
			for (int x = startLayer; x <= endLayer; x++) {
				if (range.contains(x)) {
					added = false;
					break;
				}
			}
			if(!added)
				break;
		}

		if (added) {
			profileList.addLast(new Profile(height, startLayer, endLayer));
			profileList.sort(Comparator.comparing(Profile::startLayer));
		}
		return added;
	}

	private static int getLayerNumber(String line) {
		String  regex = "(;LAYER:)([0-9]+)";
		Pattern p     = Pattern.compile(regex);
		Matcher m     = p.matcher(line);
		if (m.find()) {
			return Integer.parseInt(m.group(2));
		}
		return -1;
	}

	public static double getZValue(String line) {
		String  regex  = "(Z)([0-9.]+)";
		Pattern p      = Pattern.compile(regex);
		Matcher m      = p.matcher(line);
		double  zValue = 0.0;
		if (m.find()) {
			zValue = Double.parseDouble(m.group(2));
		}
		return zValue;
	}

	private static class ZCount {
		public ZCount(int layerNumber) {
			this.layerNumber = layerNumber;
		}

		private final LinkedList<String> moveLines = new LinkedList<>();
		private final int                layerNumber;

		public void listMoveLines() {
			for (String line : moveLines) {
			}
		}

		public void addMoveLine(String line) {
			moveLines.addLast(line);
		}
	}

	private static final Map<Integer, ZCount> zCountMap = new HashMap<>();

	public static void testCura(Path pathToGCode) {
		try {
			String             gcode  = Files.readString(pathToGCode);
			LinkedList<String> lines  = new LinkedList<>(Arrays.stream(gcode.split("\\n")).toList());
			int                layer  = -1;
			ZCount             zcount = null;
			double             lastZ  = 0.0;
			for (String line : lines) {
				if (line.startsWith(";LAYER:")) {
					if (zcount != null) {
						zCountMap.put(layer, zcount);
					}
					layer  = getLayerNumber(line);
					zcount = new ZCount(layer);
				}
				else if (line.contains("Z") && layer >= 0 && (line.startsWith("G1") || line.startsWith("G0"))) {
					lastZ = getZValue(line);
				}
				else if (isMoveLine(line) && layer >= 0) {
					if (!line.contains("F")) {zcount.addMoveLine("NO F: " + line);}
					else {zcount.addMoveLine("Z" + lastZ + ":\t" + line);}
				}
			}
			for (Integer layerNum : zCountMap.keySet()) {
				ZCount zCount = zCountMap.get(layerNum);
				zCount.listMoveLines();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String addZHops(String gcode) {
		ZHopping.gcode = gcode;
		int            lastLayerNumber = -1;
		StringProperty process         = new SimpleStringProperty("Adding zHop - scanning");
		process.addListener((ob, ov, nv) -> {
			if (!nv.equals(ov)) {
				Tracking.setProcess(nv);
			}
		});
		Tracking.setProcess("Adding zHops - scanning");
		for (Profile profile : profileList) {

			int                endLayer   = profile.endLayer;
			double             zLift      = profile.height;
			int                startLayer = profile.startLayer;
			double             ZValue     = 0.0;
			int                layer      = -1;
			String             newLine;
			LinkedList<String> gcodeLines = new LinkedList<>(Arrays.stream(ZHopping.gcode.split("\\n")).toList());
			lastLine = gcodeLines.size();
			StringBuilder sb                = new StringBuilder();
			int           currentLineNumber = -1;
			ZSet          zSet              = null;
			boolean       buildingZSet      = false;
			for (String line : gcodeLines) {
				currentLineNumber++;
				newLine = line;
				boolean isZLine         = hasZAction(line);
				boolean isExtrudingMove = isExtrusionMove(line);
				boolean isBlankMove     = isBlankMove(line);
				if (isZLine) {
					ZValue = getZValue(line);
				}
				if (line.startsWith(";LAYER:")) {
					layer = getLayerNumber(line);
					if (layer != lastLayerNumber) {
						lastLayerNumber = layer;
					}
					sb.append(line).append(ret);
					continue;
				}
				if (layer >= startLayer && layer <= endLayer) {
					process.setValue("Adding zHop - Layer: " + layer);
					if (buildingZSet && !isExtrudingMove) {
						zSet.addLine(currentLineNumber, line);
					}
					else if (buildingZSet && isExtrudingMove) {
						LinkedList<String> lines = zSet.getZHopSet(line, ZValue, zLift);
						for (String thisLine : lines) {
							sb.append(thisLine).append(ret);
						}
						buildingZSet = false;
						continue;
					}
					else if (!buildingZSet && isBlankMove) {
						if (qualifiedZHopPoint(gcodeLines, currentLineNumber)) {
							zSet         = new ZSet(line);
							buildingZSet = true;
						}
					}

				}
				if (!buildingZSet) {sb.append(newLine).append(ret);}
			}
			ZHopping.gcode = sb.toString();
		}
		return ZHopping.gcode;
	}

	private static boolean qualifiedZHopPoint(LinkedList<String> gcodeList, int currentLineNumber) {
		int     lineNumber         = currentLineNumber;
		boolean foundZ             = false;
		boolean foundExtrusionMove = false;
		while (true) {
			lineNumber++;
			if (lineNumber >= lastLine) {
				break;
			}
			String line = gcodeList.get(lineNumber);
			foundZ = hasZAction(line);
			if (foundZ) {
				break;
			}
			foundExtrusionMove = isExtrusionMove(line);
			if (foundExtrusionMove) {
				break;
			}
		}
		return !foundZ && foundExtrusionMove;
	}

	private static boolean hasZAction(String line) {
		return (line.startsWith("G1") || line.startsWith("G0")) && line.contains("Z");
	}

	private static boolean isExtrusionMove(String line) {
		boolean isAction    = line.startsWith("G1") || line.startsWith("G0");
		boolean hasXorYAndE = (line.contains("X") || line.contains("Y")) && line.contains("E");
		return isAction && hasXorYAndE;
	}

	private static boolean isMoveLine(String line) {
		boolean isAction = line.startsWith("G1") || line.startsWith("G0");
		boolean hasXYorZ = line.contains("X") || line.contains("Y") || line.contains("Z");
		boolean hasNoE   = !line.contains("E");
		return isAction && hasXYorZ && hasNoE;
	}

	private static boolean isBlankMove(String line) {
		boolean isAction = line.startsWith("G1") || line.startsWith("G0");
		boolean hasXorY  = line.contains("X") || line.contains("Y");
		boolean hasNoZE  = !line.contains("Z") && !line.contains("E");
		return isAction && hasXorY && hasNoZE;
	}

	public static String getZHops() {
		StringBuilder sb = new StringBuilder("Current ZHops:\tDistance  Start  End\n\t\t--------  -----  ---\n");
		for (Profile profile : profileList) {
			String height = String.valueOf(profile.height);
			String start  = String.valueOf(profile.startLayer);
			String end    = String.valueOf(profile.endLayer);
			int    dSpace = 10 - height.length();
			int    sSpace = 7 - start.length();
			sb.append("\t\t").append(height).append(" ".repeat(dSpace)).append(start).append(" ".repeat(sSpace)).append(end).append("\n");
		}
		return (profileList.size() > 0) ? sb.toString() : "";
	}

	public static boolean haveZHops() {
		return profileList.size() > 0;
	}

	private static class Profile {

		public Profile(double height, int startLayer, int endLayer) {
			this.height     = height;
			this.startLayer = startLayer;
			this.endLayer   = endLayer;
		}

		private final double height;
		private final int    startLayer;
		private final int    endLayer;

		public int startLayer() {
			return startLayer;
		}

	}
}
