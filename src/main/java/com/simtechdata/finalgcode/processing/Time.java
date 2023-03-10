package com.simtechdata.finalgcode.processing;

import com.simtechdata.finalgcode.settings.AppSettings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time {

	private static final Time INSTANCE = new Time();

	public static Time getInstance() {
		return INSTANCE;
	}

	private Time() {
		logging = AppSettings.get().logging();
		analyze();
	}

	private       double  currentX       = 0;
	private       double  currentY       = 0;
	private       double  currentZ       = 0;
	private       double  lastFeedRate   = 60;
	private       double  totalHeight    = 0.0;
	private       double  totalPrintTime = 0;
	private final boolean logging;

	private final Map<Integer, Double> layerHeightMap = new HashMap<>();
	private final Map<Integer, Double> layerTimeMap   = new HashMap<>();

	private void analyze() {
		LinkedList<String> G1Lines      = getG10Commands();
		int                layer        = 0;
		LinkedList<Double> lineTimeList = new LinkedList<>();
		for (String gLine : G1Lines) {
			if (gLine.startsWith(";LAYER:")) {
				String num = gLine.replaceAll("(;LAYER[: ]+)([0-9]+)([a-zA-Z0-9 ,:._]+)","$2");
				layer = Integer.parseInt(num.replaceAll("[^0-9]+",""));
				if (layer != 0) {
					double totalTime = 0;
					for (double time : lineTimeList) {
						totalTime += time;
					}
					layerTimeMap.put(layer, totalTime);
					lineTimeList = new LinkedList<>();
				}
			}
			if (gLine.contains("HEIGHT:")) {
				double height = getHeight(gLine);
				layerHeightMap.put(layer, height);
				totalHeight = height;
			}
			if (gLine.startsWith("G1") || gLine.startsWith("G0")) {
				lineTimeList.addLast(addTime(getLine(gLine)));
			}
		}
		if (logging) {
			File logFile = new File(("/Users/michael/Java/PrintTimeLog.csv"));
			try {
				FileUtils.writeStringToFile(logFile, log.toString(), Charset.defaultCharset());
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	StringBuilder log = new StringBuilder("G,X,Y,Z,E,F,Line,Accumulation\n");

	private double getHeight(String line) {
		String  regex  = "(HEIGHT:)([0-9.]+)";
		Pattern p      = Pattern.compile(regex);
		Matcher m      = p.matcher(line);
		double  height = 0;
		if (m.find()) {
			height = Double.parseDouble(m.group(2));
		}
		return height;
	}

	private double addTime(Line gline) {
		double printTime = 0.0;
		double newX      = gline.Xv;
		double newY      = gline.Yv;
		double newZ      = gline.Zv;
		double newE      = gline.Ev;
		double newF      = gline.Fv;


		double feedRate = newF > 0 ? newF / 60 : lastFeedRate;
		lastFeedRate = feedRate;

		if (gline.EOnly()) {
			double totalEMovement = newE * 2;
			printTime = totalEMovement / feedRate;
		}
		else if (gline.XorY()) {
			double deltaX   = Math.abs(newX - currentX);
			double deltaY   = Math.abs(newY - currentY);
			double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
			printTime = distance / feedRate;
			currentX  = newX == 0 ? currentX : newX;
			currentY  = newY == 0 ? currentY : newY;
		}
		else if (gline.onlyZ()) {
			double deltaZ = Math.abs(newZ - currentZ);
			printTime = deltaZ / feedRate;
			currentZ  = newZ;
		}
		totalPrintTime += printTime;
		if (logging) {log.append(gline.getLine()).append(",").append(printTime).append(",").append(totalPrintTime).append("\n");}
		return printTime;
	}

	private LinkedList<String> getG10Commands() {
		LinkedList<String> G10CommandList = new LinkedList<>();
		LinkedList<String> lineList       = AppSettings.getGcodeList();
		int                index          = 0;
		boolean            nextIsLayer    = false;
		for (String gline : lineList) {
			if (nextIsLayer) {
				nextIsLayer = false;
				continue;
			}
			if (gline.contains("HEIGHT:")) {
				String nextLine = lineList.get(index + 1);
				nextIsLayer = nextLine.startsWith(";LAYER:");
				if (nextIsLayer) {G10CommandList.addLast(nextLine);}
			}
			G10CommandList.addLast(gline);
			index++;
		}
		return G10CommandList;
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

	private static class Line {

		public Line(String g, String x, String y, String z, String e, String f) {
			G  = g;
			X  = x;
			Y  = y;
			Z  = z;
			E  = e;
			F  = f;
			Xv = X.isEmpty() ? 0.0 : Double.parseDouble(X.replaceAll("[^0-9.]+", ""));
			Yv = Y.isEmpty() ? 0.0 : Double.parseDouble(Y.replaceAll("[^0-9.]+", ""));
			Zv = Z.isEmpty() ? 0.0 : Double.parseDouble(Z.replaceAll("[^0-9.]+", ""));
			Ev = E.isEmpty() ? 0.0 : Double.parseDouble(E.replaceAll("[^0-9.]+", ""));
			Fv = F.isEmpty() ? 0.0 : Double.parseDouble(F.replaceAll("[^0-9.]+", ""));
		}

		public final String G;
		public final String X;
		public final String Y;
		public final String Z;
		public final String E;
		public final String F;
		public final double Xv;
		public final double Yv;
		public final double Zv;
		public final double Ev;
		public final double Fv;

		public String getLine() {
			return G + "," + Xv + "," + Yv + "," + Zv + "," + Ev + "," + Fv;
		}

		public boolean EOnly() {
			return Ev > 0 && Xv == 0 && Yv == 0 && Zv == 0;
		}

		public boolean XorY() {
			return Xv > 0 || Yv > 0;
		}

		public boolean onlyZ() {
			return Zv > 0 && Xv == 0 && Yv == 0;
		}
	}

	public double getHeightAtLayer(int layer) {
		return layerHeightMap.get(layer);
	}

	public double getTotalHeight() {
		return totalHeight;
	}

	public int getTotalPrintTime() {
		return (int) totalPrintTime;
	}

	public int getTotalPrintMinutes() {
		return (int) totalPrintTime / 60;
	}

	public int getLayerCount() {
		int layers = 0;
		for (int layer : layerTimeMap.keySet()) {
			layers = layer;
		}
		return layers;
	}

	public int getTimeForLayer(int layer) {
		Double time = layerTimeMap.get(layer);
		return (time != null) ? time.intValue() : 0;
	}

	public int getTotalTimeAtLayer(int layer) {
		int totalTime = 0;
		for (int x = 0; x <= layer; x++) {
			totalTime += getTimeForLayer(x);
		}
		return totalTime;
	}
}
