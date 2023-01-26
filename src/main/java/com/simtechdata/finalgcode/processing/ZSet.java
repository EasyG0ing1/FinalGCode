package com.simtechdata.finalgcode.processing;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZSet {

	public ZSet(String startLine) {
		this.startLine = startLine;
	}

	private final Map<Integer, String> allLines = new HashMap<>();
	private final String               startLine;
	private       LinkedList<String>   workingGCodeList;

	public void addLine(Integer lineNumber, String line) {
		allLines.put(lineNumber, line);
	}

	private void startNewList() {
		workingGCodeList = new LinkedList<>();
	}

	private void add(String line) {
		workingGCodeList.addLast(line);
	}

	public LinkedList<String> getZHopSet(String extrudingLine, double layerZ, double zHop) {
		int                 totalMoves  = 1;
		LinkedList<Integer> lineNumbers = new LinkedList<>(allLines.keySet());
		lineNumbers.sort(Comparator.comparing(Integer::intValue));
		for (Integer lineNumber : lineNumbers) {
			Line line = getLine(allLines.get(lineNumber));
			if (moveLine(line)) totalMoves++;
		}
		boolean finalMove = totalMoves < 2;
		startNewList();
		String G, X, Y, F, Z;
		Line   line = getLine(startLine);
		G = line.G;
		X = line.X;
		Y = line.Y;
		F = line.F;
		Z = " Z" + round(layerZ + zHop);

		String lastF = F;
		String zHopF = F;

		String zhF = F.isEmpty() ? "" : " " + F;
		String thisZHop = G + Z + zhF + " ; zHop";

		String comment  = !finalMove ? " ; Interim move" : " ; Move to extrusion point";

		String fmX = X.isEmpty() ? "" : " " + X;
		String fmY = Y.isEmpty() ? "" : " " + Y;

		String firstMove = G + fmX + fmY + comment;
		String lowerZ = G + " Z" + layerZ + " ; restore Z";

		add(thisZHop);
		add(firstMove);
		if(finalMove) {
			add(lowerZ);
		}

		totalMoves--;
		String newMove;

		for (Integer lineNumber : lineNumbers) {
			String gLine = allLines.get(lineNumber);
			line = getLine(gLine);
			boolean moveLine = moveLine(line);
			if (!line.F.isEmpty()) {
				lastF = line.F;
			}
			if (moveLine) {
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
				totalMoves--;
				continue;
			}
			add(gLine);
		}
		add(extrudingLine);
		return workingGCodeList;
	}

	private boolean moveLine(Line line) {
		boolean isMove = line.G.contains("G");
		boolean hasX   = line.X.contains("X");
		boolean hasY   = line.Y.contains("Y");
		boolean noZ    = line.Z.isEmpty();
		boolean noE    = line.E.isEmpty();
		return isMove && hasX && hasY && noZ && noE;
	}

	private Line getLine(String line) {
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
		return new Line(g, x, y, z, e, f);
	}

	private String round(double digit) {
		Double number = BigDecimal.valueOf(digit).setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		return number.toString();
	}

	private static class Line {

		public Line(String g, String x, String y, String z, String e, String f) {
			G = g;
			X = x;
			Y = y;
			Z = z;
			E = e;
			F = f;
		}

		public final String G;
		public final String X;
		public final String Y;
		public final String Z;
		public final String E;
		public final String F;
	}

}
