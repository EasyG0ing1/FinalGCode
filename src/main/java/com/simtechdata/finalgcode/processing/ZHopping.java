package com.simtechdata.finalgcode.processing;

import com.simtechdata.finalgcode.enums.DiagonalMethod;
import com.simtechdata.finalgcode.settings.AppSettings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simtechdata.finalgcode.enums.DiagonalMethod.ALTERNATE;
import static com.simtechdata.finalgcode.enums.DiagonalMethod.TRADITIONAL;

public class ZHopping {

	private static final ZHopping INSTANCE = new ZHopping();

	private ZHopping() {}

	private       String              gcode;
	private       LinkedList<String>  gcodeList;
	private final LinkedList<Profile> profileList = new LinkedList<>();
	private final String              ret         = System.getProperty("line.separator");
	private       int                 lastLine    = 0;

	public String addZHops() {
		gcode = AppSettings.getGCode();
		int            lastLayerNumber = -1;
		StringProperty process         = new SimpleStringProperty("Adding zHop - scanning");
		process.addListener((ob, ov, nv) -> {
			if (!nv.equals(ov)) {
				Tracking.setProcess(nv);
			}
		});
		Tracking.setProcess("Adding zHops - scanning");
		for (Profile profile : profileList) {
			int                startLayer = profile.startLayer;
			int                endLayer   = profile.endLayer;
			double             zLift      = profile.zLift;
			double             ZValue     = 0.0;
			int                layer      = -1;
			String             newLine;
			LinkedList<String> gcodeLines = new LinkedList<>(Arrays.stream(gcode.split("\\n")).toList());
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
						LinkedList<String> lines;
						if (profile.isDiagonal) {lines = zSet.getDiagZHopSet(line, currentLineNumber, ZValue, zLift, profile.method, profile.minDistance);}
						else {lines = zSet.getZHopSet(line, ZValue, zLift);}
						for (String thisLine : lines) {
							sb.append(thisLine).append(ret);
						}
						buildingZSet = false;
						continue;
					}
					else if (!buildingZSet && isBlankMove) {
						if (qualifiedZHopPoint(gcodeLines, currentLineNumber)) {
							if (profile.isDiagonal) {zSet = new ZSet(line, currentLineNumber);}
							else {zSet = new ZSet(line);}
							buildingZSet = true;
						}
					}

				}
				if (!buildingZSet) {sb.append(newLine).append(ret);}
			}
			gcode = sb.toString();
		}
		AppSettings.setGCode(gcode);
		return gcode;
	}

	public static ZHopping getInstance() {
		return INSTANCE;
	}

	public void clearZHops() {
		profileList.clear();
	}

	public boolean insertZHop(double height, int startLayer, int endLayer) {
		boolean added = verifyZHopSettings(startLayer, endLayer);
		if (added) {
			profileList.addLast(new Profile(height, startLayer, endLayer));
			profileList.sort(Comparator.comparing(Profile::startLayer));
		}
		return added;
	}

	public boolean insertZHop(double height, int startLayer, int endLayer, boolean diagonalZHop, boolean alternateMethod, double minDistance) {
		boolean added = verifyZHopSettings(startLayer, endLayer);
		if (added) {
			profileList.addLast(new Profile(height, startLayer, endLayer, diagonalZHop, alternateMethod, minDistance));
			profileList.sort(Comparator.comparing(Profile::startLayer));
		}
		return added;
	}

	private boolean verifyZHopSettings(int startLayer, int endLayer) {
		boolean              added     = true;
		List<Range<Integer>> rangeList = new ArrayList<>();

		for (Profile profile : profileList) {
			int start = profile.startLayer;
			int end   = profile.endLayer;
			rangeList.add(Range.between(start, end));
		}

		for (Range range : rangeList) {
			for (int x = startLayer; x <= endLayer; x++) {
				if (range.contains(x)) {
					added = false;
					break;
				}
			}
			if (!added) {break;}
		}
		return added;
	}

	private int getLayerNumber(String line) {
		String  regex = "(;LAYER[: ]+)([0-9]+)";
		Pattern p     = Pattern.compile(regex);
		Matcher m     = p.matcher(line);
		if (m.find()) {
			return Integer.parseInt(m.group(2));
		}
		return -1;
	}

	public double getZValue(String line) {
		String  regex  = "(Z)([0-9.]+)";
		Pattern p      = Pattern.compile(regex);
		Matcher m      = p.matcher(line);
		double  zValue = 0.0;
		if (m.find()) {
			zValue = Double.parseDouble(m.group(2));
		}
		return zValue;
	}

	private boolean qualifiedZHopPoint(LinkedList<String> gcodeList, int currentLineNumber) {
		if (this.gcodeList == null) {
			this.gcodeList = gcodeList;
		}
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

	private boolean hasZAction(String line) {
		return (line.startsWith("G1") || line.startsWith("G0")) && line.contains("Z");
	}

	private boolean isExtrusionMove(String line) {
		boolean isAction    = line.startsWith("G1") || line.startsWith("G0");
		boolean hasXorYAndE = (line.contains("X") || line.contains("Y")) && line.contains("E");
		return isAction && hasXorYAndE;
	}

	private boolean isBlankMove(String line) {
		boolean isAction = line.startsWith("G1") || line.startsWith("G0");
		boolean hasXorY  = line.contains("X") || line.contains("Y");
		boolean hasNoZE  = !line.contains("Z") && !line.contains("E");
		return isAction && hasXorY && hasNoZE;
	}

	public String getZHopsString() {
		StringBuilder sb = new StringBuilder("Current ZHops:\tD  Distance  Start  End T/A Min\n\t\t-  --------  -----  --- --- ---\n");
		for (Profile profile : profileList) {
			String diagonal    = profile.diagonalZHop ? "X" : "";
			String method      = (profile.method == null) ? "" : profile.method.equals(ALTERNATE) ? " A" : " T";
			String minimum     = (profile.minDistance == -1) ? "" : String.valueOf(profile.minDistance);
			String zLift       = String.valueOf(profile.zLift);
			String start       = String.valueOf(profile.startLayer);
			String end         = String.valueOf(profile.endLayer);
			int    diagSpace   = 3 - diagonal.length();
			int    disSpace    = 10 - zLift.length();
			int    startSpace  = 7 - start.length();
			int    endSpace    = 4 - end.length();
			int    methodSpace = 4 - method.length();
			sb.append("\t\t")
			  .append(diagonal).append(" ".repeat(diagSpace))
			  .append(zLift).append(" ".repeat(disSpace))
			  .append(start).append(" ".repeat(startSpace))
			  .append(end).append(" ".repeat(endSpace))
			  .append(method).append(" ".repeat(methodSpace))
			  .append(minimum).append("\n");
		}
		return (profileList.size() > 0) ? sb.toString() : "";
	}

	public boolean haveZHops() {
		return profileList.size() > 0;
	}

	private static class Profile {

		public Profile(double zLift, int startLayer, int endLayer) {
			this.zLift        = zLift;
			this.startLayer   = startLayer;
			this.endLayer     = endLayer;
			this.method       = null;
			this.minDistance  = -1;
			this.diagonalZHop = false;
		}

		public Profile(double zLift, int startLayer, int endLayer, boolean diagonalZHop, boolean alternateMethod, double minDistance) {
			this.zLift        = zLift;
			this.startLayer   = startLayer;
			this.endLayer     = endLayer;
			this.diagonalZHop = diagonalZHop;
			this.method       = alternateMethod ? ALTERNATE : TRADITIONAL;
			this.minDistance  = minDistance;
			this.isDiagonal   = true;
		}

		private final double         zLift;
		private final int            startLayer;
		private final int            endLayer;
		private final boolean        diagonalZHop;
		private final DiagonalMethod method;
		private final double         minDistance;
		private       boolean        isDiagonal = false;

		public int startLayer() {
			return startLayer;
		}

	}
}
