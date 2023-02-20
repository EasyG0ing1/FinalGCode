package com.simtechdata.finalgcode.processing;

import com.simtechdata.finalgcode.enums.DiagonalMethod;
import com.simtechdata.finalgcode.processing.structure.Line;
import com.simtechdata.finalgcode.settings.AppSettings;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simtechdata.finalgcode.enums.DiagonalMethod.ALTERNATE;

public class ZSet {

	public ZSet(String startLine) {
		this.startLine = getLine(startLine);
	}

	public ZSet(String startLine, int lineNumber) {
		this.startLine       = getLine(startLine);
		this.startLineNumber = lineNumber;
		getLastXY();
	}

	public void addLine(Integer lineNumber, String line) {
		Line fLine = getLine(line);
		if (fLine == null) {
			System.out.println("Line is null (ZSet.addLine)");
			System.exit(0);
		}
		allLines.put(lineNumber, fLine);
	}

	private final Map<Integer, Line> allLines        = new HashMap<>();
	private final Line               startLine;
	private       int                startLineNumber = 0;
	private       LinkedList<String> workingGCodeList;
	private       double             lastX           = -1;
	private       double             lastY           = -1;

	private void startNewList() {
		workingGCodeList = new LinkedList<>();
	}

	private void add(String line) {
		if (!line.toLowerCase().contains("moveline")) {workingGCodeList.addLast(line);}
	}

	private void getLastXY() {
		Line               lastExtrusionLine;
		Map<Integer, Line> lineMap = AppSettings.getGcodeLineMap();
		int                index   = startLineNumber;
		Line               line;
		while (true) {
			if (index >= 0) {
				line = lineMap.get(index);
				index--;
				if (line.isExtrudingLine) {
					lastExtrusionLine = line;
					break;
				}
			}
			else {
				lastExtrusionLine = startLine;
				break;
			}
		}
		lastX = lastExtrusionLine.xV();
		lastY = lastExtrusionLine.yV();
	}

	public LinkedList<String> getZHopSet(String extrudingLine, double layerZ, double zHop) {
		int                 totalMoves  = 1;
		LinkedList<Integer> lineNumbers = new LinkedList<>(allLines.keySet());
		lineNumbers.sort(Comparator.comparing(Integer::intValue));

		for (Integer lineNumber : lineNumbers) {
			Line line = allLines.get(lineNumber);
			if (line.isMoveLine) totalMoves++;
		}
		boolean finalMove = totalMoves < 2;
		startNewList();
		String G, X, Y, F, Z;
		Line   line = startLine;
		G = line.G;
		X = line.X;
		Y = line.Y;
		F = line.F;
		Z = " Z" + round(layerZ + zHop);

		String lastF = F;
		String zHopF = F;

		String zhF      = F.isEmpty() ? "" : " " + F;
		String thisZHop = G + Z + zhF + " ; zHop";

		String comment = !finalMove ? " ; Interim move" : " ; Move to extrusion point";

		String fmX = X.isEmpty() ? "" : " " + X;
		String fmY = Y.isEmpty() ? "" : " " + Y;

		String firstMove = G + fmX + fmY + comment;
		String lowerZ    = G + " Z" + layerZ + " ; restore Z";

		lastX = Double.parseDouble(X.replace("X", ""));
		lastY = Double.parseDouble(Y.replace("Y", ""));

		add(thisZHop);
		add(firstMove);
		if (finalMove) {
			add(lowerZ);
		}

		totalMoves--;
		String newMove;

		for (Integer lineNumber : lineNumbers) {
			line = allLines.get(lineNumber);
			if (!line.F.isEmpty()) {
				lastF = line.F;
			}
			if (line.isMoveLine) {
				G       = line.G.isEmpty() ? "" : (line.G);
				X       = line.X.isEmpty() ? "" : (" " + line.X);
				Y       = line.Y.isEmpty() ? "" : (" " + line.Y);
				F       = line.F.isEmpty() ? "" : (" " + line.F);
				comment = totalMoves > 1 ? " ; Interim move" : " ; Move to extrusion point";
				newMove = G + X + Y + F + comment;
				add(newMove);
				if (!(totalMoves > 1)) {
					String finalF = lastF.equals(zHopF) ? "" : " " + zHopF;
					String zLower = G + " Z" + layerZ + finalF + " ; restore Z";
					add(zLower);
					if (!finalF.isEmpty()) {
						add(G + " " + lastF + " ; restore feedrate");
					}
				}
				lastX = Double.parseDouble(X.replace("X", ""));
				lastY = Double.parseDouble(Y.replace("Y", ""));
				totalMoves--;
				continue;
			}
			add(line.line);
		}
		add(extrudingLine);
		return workingGCodeList;
	}

	private String getFinalF() {
		String              finalF      = "";
		LinkedList<Integer> lineNumbers = new LinkedList<>(allLines.keySet());
		lineNumbers.sort(Comparator.comparing(Integer::intValue));
		for (int lineNumber : lineNumbers) {
			Line line = allLines.get(lineNumber);
			if (!line.F.isEmpty()) {finalF = line.F;}
		}
		return finalF;
	}

	public LinkedList<String> getDiagZHopSet(String extrudingLine, int extrudingLineNumber, double layerZ, double zHop, DiagonalMethod method, double minDistance) {
		startNewList();
		allLines.put(startLineNumber, startLine);
		LinkedList<Integer> lineNumbers = new LinkedList<>(allLines.keySet());
		lineNumbers.sort(Comparator.comparing(Integer::intValue));
		LinkedList<Line>   moveLines = new LinkedList<>();
		LinkedList<String> setLines  = new LinkedList<>();
		int                moveCount = 0;
		for (Integer lineNumber : lineNumbers) {
			Line line = allLines.get(lineNumber);
			if (line.isMoveLine) {
				moveCount++;
				moveLines.addLast(line);
				setLines.addLast("MoveLine:" + moveCount);
			}
			else {
				setLines.addLast(line.line);
			}
		}
		double totalMoveDistance;
		double thisLastX = lastX;
		double thisLastY = lastY;
		double totalX    = 0;
		double totalY    = 0;
		for (Line line : moveLines) {
			double pointX = line.xV();
			double pointY = line.yV();
			totalX += Math.abs(pointX - thisLastX);
			totalY += Math.abs(pointY - thisLastY);
			thisLastX = pointX;
			thisLastY = pointY;
		}
		totalMoveDistance = Math.sqrt((totalX * totalX) + (totalY * totalY));

		if (totalMoveDistance < minDistance || (startLine.xV() == lastX && startLine.yV() == lastY)) {
			allLines.put(extrudingLineNumber, getLine(extrudingLine));
			lineNumbers = new LinkedList<>(allLines.keySet());
			lineNumbers.sort(Comparator.comparing(Integer::intValue));
			startNewList();
			add("; move too short for diagonal ZHop");
			for (int lineNumber : lineNumbers) {
				Line line = allLines.get(lineNumber);
				add(line.line);
			}
			return workingGCodeList;
		}

		String   finalF    = getFinalF();
		String   addLine;
		double   zh        = layerZ + zHop;
		String   F         = moveLines.get(0).F.isEmpty() ? "" : " " + moveLines.get(0).F;
		String   G         = moveLines.get(0).G;
		String[] lineArray = new String[moveCount + 1];
		if (moveCount > 1) {
			if (getLine(setLines.get(1)).primeRetract) {
				String retraction = setLines.remove(1);
				setLines.addFirst(retraction);
			}
		}
		int index = 1;
		if (moveCount > 1) {
			if (method.equals(ALTERNATE)) {
				addLine = G + " Z" + round(zh) + F + " ; start multi-move diagonal ZHop alternate method";
				double zOff = zHop / moveCount;
				for (Line line : moveLines) {
					zh -= zOff;
					String lineF = line.F.isEmpty() ? "" : " " + line.F;
					F = (!lineF.equals(F) ? " " + line.F : "");
					String comment = index == moveCount ? " ; finish diagonal ZHop" : " ; mid point ZHop";
					if (index == 1) {addLine += "\n" + line.G + " " + line.X + " " + line.Y + " Z" + round(zh) + F + comment;}
					else {addLine = line.G + " " + line.X + " " + line.Y + " Z" + round(zh) + F + comment;}
					lineArray[index] = addLine;
					index++;
				}
			}
			else {
				lineArray = getDZHopMoves(moveLines, layerZ, zHop, moveCount);
			}
		}
		else {
			if (method.equals(ALTERNATE)) {
				addLine = G + " Z" + round(zh) + F + " ; do diagonal ZHop alternate method\n";
				Line line = moveLines.get(0);
								   addLine += line.G + " " + line.X + " " + line.Y + " Z" + layerZ + " ; finish diagonal ZHop";
				lineArray[index] = addLine;
			}
			else {
				lineArray = getDZHopMoves(moveLines, layerZ, zHop, moveCount);
			}
		}
		index = 1;
		for (String line : setLines) {
			if (line.toLowerCase().contains("moveline")) {
				add(lineArray[index]);
				index++;
			}
			else {
				add(line);
			}
		}
		String fRestore = G + " " + finalF;
		if (!workingGCodeList.getLast().contains(fRestore)) {
			addLine = fRestore + " ; restore feedrate";
			add(addLine);
		}
		add(extrudingLine);
		return workingGCodeList;
	}

	private String[] getDZHopMoves(LinkedList<Line> moveLines, double layerZ, double zHop, int moveCount) {
		String[] finalMoves = new String[moveCount + 1];
		String   addLine;
		if (moveCount > 1) {
			boolean even      = moveCount % 2 == 0;
			int     midDigit;
			String  cumNum    = "";
			int     lastIndex = 0;
			for (int x = 1; x <= moveCount; x++) {
				cumNum    = cumNum + x;
				lastIndex = x;
			}
			int digits = Integer.parseInt(cumNum.replaceAll("[^0-9]+",""));
			midDigit = findMiddle(digits);
			double  midValue = layerZ + zHop;
			double  delta    = zHop / moveCount;
			boolean up       = true;
			int     index;
			double  thisZ    = layerZ;
			if (!even && moveCount > 1) {
				index = 1;
				for (Line line : moveLines) {
					String G = line.G;
					String X = line.X.isEmpty() ? "" : " " + line.X;
					String Y = line.Y.isEmpty() ? "" : " " + line.Y;
					String F = line.F.isEmpty() ? "" : " " + line.F;
					String Z;
					if (index == midDigit) {
						Z     = " Z" + round(midValue);
						up    = false;
						thisZ = midValue;
					}
					else if (index == lastIndex) {
						Z = " Z" + layerZ;
					}
					else {
						if (up) {
							thisZ += delta;
						}
						else {
							thisZ -= delta;
						}
						Z = " Z" + round(thisZ);
					}
					String comment = (index == lastIndex) ? " ; end diagonal ZHop" : " ; mid point diagonal ZHop using Traditional method";
					if (index == 1) comment = " ; start diagonal ZHop using Traditional method";
					addLine           = G + X + Y + Z + F + comment;
					finalMoves[index] = addLine;
					index++;
				}
			}
			else if (moveCount == 2) {
				index = 1;
				String Z1 = " Z" + round(layerZ + zHop);
				String Z2 = " Z" + layerZ;
				for (Line line : moveLines) {
					String G       = line.G;
					String X       = line.X.isEmpty() ? "" : " " + line.X;
					String Y       = line.Y.isEmpty() ? "" : " " + line.Y;
					String F       = line.F.isEmpty() ? "" : " " + line.F;
					String Z       = index == 1 ? Z1 : Z2;
					String comment = index == 1 ? " ; start diagonal ZHop using Traditional method" : " ; end diagonal ZHop";
					addLine           = G + X + Y + Z + F + comment;
					finalMoves[index] = addLine;
					index++;
				}
			}
			else if (even) {
				String midDigits = String.valueOf(midDigit);
				index = 1;
				for (Line line : moveLines) {
					String G           = line.G;
					String X           = line.X.isEmpty() ? "" : " " + line.X;
					String Y           = line.Y.isEmpty() ? "" : " " + line.Y;
					String F           = line.F.isEmpty() ? "" : " " + line.F;
					String Z;
					String indexString = String.valueOf(index);
					if (midDigits.contains(indexString)) {
						Z     = " Z" + round(midValue);
						up    = false;
						thisZ = midValue;
					}
					else if (index == lastIndex) {
						Z = " Z" + layerZ;
					}
					else {
						if (up) {
							thisZ += delta;
						}
						else {
							thisZ -= delta;
						}
						Z = " Z" + round(thisZ);
					}
					String comment = (index == lastIndex) ? " ; end diagonal ZHop" : " ; mid point diagonal ZHop using Traditional method";
					if (index == 1) comment = " ; start diagonal ZHop using Traditional method";
					addLine           = G + X + Y + Z + F + comment;
					finalMoves[index] = addLine;
					index++;
				}
			}
		}
		else {
			Line   line = moveLines.get(0);
			Double x    = line.xV();
			Double y    = line.yV();
			String G, F;
			G = line.G;
			F = line.F.isEmpty() ? "" : " " + line.F;
			double zh   = layerZ + zHop;
			double midX = x + ((lastX - x) / 2);
			double midY = y + ((lastY - y) / 2);
			addLine       = G + " X" + round(midX) + " Y" + round(midY) + " Z" + round(zh) + F + " ; start diagonal ZHop using Traditional method\n";
			addLine += G + " X" + round(x) + " Y" + round(y) + " Z" + layerZ + " ; end diagonal ZHop using Traditional method";
			finalMoves[1] = addLine;
		}
		return finalMoves;
	}

	private int findMiddle(int n) {
		int num, count = 0;
		num = Math.abs(n);
		int finalMid;
		for (int i = num; i != 0; i = i / 10) {count++;}
		if (count % 2 == 1) {
			for (int i = 1; i <= count / 2; i++) {
				num = num / 10;
			}
			finalMid = num % 10;
		}
		else {
			for (int i = 1; i < count / 2; i++) {
				num = num / 10;
			}
			finalMid = num % 100;
		}
		return finalMid;
	}

	public static Line getLine(String line) {
		if (line.startsWith("G")) {
			String[] vals = new String[]{"G", "X", "Y", "Z", "E", "F"};
			String   g    = "", x = "", y = "", z = "", e = "", f = "";
			for (String val : vals) {
				String  regex = val.equals("G") ? "(G1 |G0 )" : "(" + val + "[0-9.]+)";
				Pattern p     = Pattern.compile(regex);
				Matcher m     = p.matcher(line);
				if (m.find()) {
					switch (val) {
						case "G" -> g = m.group(1).trim();
						case "X" -> x = m.group(1).trim();
						case "Y" -> y = m.group(1).trim();
						case "Z" -> z = m.group(1).trim();
						case "E" -> e = m.group(1).trim();
						case "F" -> f = m.group(1).trim();
					}
				}
			}
			return new Line(line, g, x, y, z, e, f);
		}
		else {
			return new Line(line);
		}
	}

	private String round(double digit) {
		Double number = BigDecimal.valueOf(digit).setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		return number.toString();
	}
}
