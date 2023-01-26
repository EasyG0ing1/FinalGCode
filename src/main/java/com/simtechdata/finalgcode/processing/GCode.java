package com.simtechdata.finalgcode.processing;

import com.simtechdata.finalgcode.enums.Filament;
import com.simtechdata.finalgcode.enums.OS;
import com.simtechdata.finalgcode.settings.AppSettings;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCode {


	public static void loadGCode(Filament filament) {
		haveZHops            = ZHopping.haveZHops();
		hotEndPrintTemp      = AppSettings.get().hotEndPrintTemp(filament);
		bedHoldTemp          = AppSettings.get().bedPrintTemp(filament);
		hotEndHoldTemp       = AppSettings.get().hotEndHoldTemp(filament);
		changeTempAtLayer    = AppSettings.get().setBedTempAtLayer();
		layerToChangeBedTemp = AppSettings.get().layerForNewBedTemp(filament);
		bedAtLayerTemp       = AppSettings.get().bedTempAtLayer(filament);
		finalFadeTemp        = AppSettings.get().finalBedTempForFade(filament);
		finalFadeTemp        = AppSettings.get().finalBedTempForFade(filament);
		fadeTime             = AppSettings.get().timeForBedTempFade(filament);
		homeHotEnd           = AppSettings.get().homeHotEnd();
		fadeBedTemp          = AppSettings.get().fadeBedTempAtEndOfPrint();
		addEndGCode          = AppSettings.get().addEndGCode();
		holdHotEnd           = AppSettings.get().holdHotEndTemp();
	}

	public static void startProcessing(UserChoices userChoices) {
		haveZHops   = ZHopping.haveZHops();
		homeHotEnd  = userChoices.homeHotEnd();
		loadBedMesh = userChoices.loadBedMesh();
		addEndGCode = userChoices.addEndGCode();

		holdHotEnd      = userChoices.holdHotEnd();
		hotEndHoldTemp  = userChoices.hotEndHoldTemp();
		hotEndPrintTemp = userChoices.hotEndPrintTemp();
		bedHoldTemp     = userChoices.bedHoldTemp();
		bedPrintTemp    = userChoices.bedPrintTemp();


		changeTempAtLayer    = userChoices.changeTempAtLayer();
		bedAtLayerTemp       = userChoices.bedAtLayerTemp();
		layerToChangeBedTemp = userChoices.layerToChangeBedTemp();

		fadeBedTemp   = userChoices.fadeBedTemp();
		finalFadeTemp = userChoices.finalFadeTemp();
		fadeTime      = userChoices.fadeTime();

		processGCode();
	}

	private static final String ret = System.lineSeparator();

	private static Time    time;
	private static boolean haveZHops;

	private static boolean addEndGCode;
	private static boolean holdHotEnd;
	private static boolean homeHotEnd;
	private static boolean loadBedMesh;
	private static int     bedHoldTemp;
	private static int     bedPrintTemp = 0;
	private static int     hotEndPrintTemp;
	private static int     hotEndHoldTemp;
	private static boolean changeTempAtLayer;
	private static int     layerToChangeBedTemp;
	private static int     bedAtLayerTemp;
	private static boolean fadeBedTemp;
	private static int     finalFadeTemp;
	private static int     fadeTime;

	private static boolean addStartGCode         = false;
	private static int     fadeStartTemp         = 0;
	private static int     fadeLayerStep         = 0;
	private static int     firstLayerForCoolDown = 0;
	private static int     lastLayerNumber       = 0;

	private static final StringBuilder startGCode = new StringBuilder();

	private static int getLayerNumber(String line) {
		if (!line.startsWith(";LAYER:")) {return -1;}
		String  regex = "(;LAYER:)([0-9]+)";
		Pattern p     = Pattern.compile(regex);
		Matcher m     = p.matcher(line);
		if (m.find()) {
			return Integer.parseInt(m.group(2));
		}
		return -1;
	}

	public static int getLastLayerNumber() {
		if (lastLayerNumber == 0) {
			for (String line : getGCodeLinkedList()) {
				if (line.startsWith(";LAYER:")) {
					lastLayerNumber = getLayerNumber(line);
				}
			}
		}
		return lastLayerNumber;
	}

	public static void processGCode() {
		try {
			if (holdHotEnd || homeHotEnd) {buildStartGCode();}
			Path               finalPath = getFinalPath();
			LinkedList<String> gcode     = getGCodeLinkedList();
			time = new Time();
			if (fadeBedTemp) {
				double totalPrintMinutes = time.getTotalPrintMinutes();
				if (fadeTime > totalPrintMinutes) {
					fadeTime = (int) totalPrintMinutes;
				}
				bedPrintTemp  = findLastBedTemp(gcode);
				fadeStartTemp = bedPrintTemp - 1;
				int tempDelta = fadeStartTemp - finalFadeTemp;
				firstLayerForCoolDown = getFirstLayerForCoolDown();
				int totalLayers     = time.getLayerCount();
				int stepsInCoolDown = totalLayers - firstLayerForCoolDown;
				fadeLayerStep = stepsInCoolDown / tempDelta;
			}
			String finalGCode = getFinalGCode(gcode);
			if (AppSettings.getOS().equals(OS.WINDOWS)) {finalGCode = cleanup(finalGCode);}
			AppSettings.setGCode(finalGCode);
			FileUtils.writeStringToFile(finalPath.toFile(), AppSettings.getGCode(), Charset.defaultCharset());
			System.out.println("Success");
			System.exit(0);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getFinalGCode(LinkedList<String> gcode) {
		StringBuilder FGC                 = new StringBuilder();
		int           startCodeLineNumber = findStartCodeInsertionLine(gcode);
		int           endCodeLineNumber   = findEndCodeInsertionPoint(gcode);
		int           lineCount           = 0;
		int           fadeLayer           = firstLayerForCoolDown;
		int           fadeTemp            = fadeStartTemp;
		int           layerNumber         = -1;
		for (String l : gcode) {
			String line = l.replaceAll("\\n", "").trim();
			lineCount++;
			int lnum = getLayerNumber(line);
			layerNumber = (lnum >= 0) ? lnum : layerNumber;
			if (lineCount == startCodeLineNumber) {
				if (loadBedMesh) {FGC.append(ret).append("BED_MESH_PROFILE LOAD=default").append(" ;  Make sure the bed mesh is loaded since Klipper no longer auto loads it").append(ret);}
				if (addStartGCode) {
					Tracking.setProcess("Added starting gcode");
					FGC.append(";").append(ret).append(startGCode).append(ret).append(";").append(ret);
				}
			}
			if (changeTempAtLayer) {
				if (atLayerMarker(line)) {
					if (layerNumber == layerToChangeBedTemp) {
						Tracking.setProcess("Added temp at layerRecord");
						FGC.append(line).append(ret);
						FGC.append("M140 S").append(bedAtLayerTemp).append(" ; New bed temp for this height").append(ret);
						continue;
					}
				}
			}
			if (fadeBedTemp) {
				if (atLayerMarker(line)) {
					if ((layerNumber == fadeLayer) && (fadeTemp >= finalFadeTemp)) {
						Tracking.setProcess("Adding Fade Bed Temp");
						FGC.append(line).append(ret);
						FGC.append("M140 S").append(fadeTemp).append(" ; New bed temp for Fadeout").append(ret);
						fadeTemp--;
						fadeLayer += fadeLayerStep;
						continue;
					}
				}
			}
			if (addEndGCode) {
				if (lineCount == endCodeLineNumber) {
					Tracking.setProcess("Added End gcode");
					FGC.append(line).append(ret);
					FGC.append(getEndGCode());
					continue;
				}
			}
			FGC.append(line).append(ret);
		}
		return haveZHops ? ZHopping.addZHops(FGC.toString()) : FGC.toString();
	}

	private static String cleanup(String gcode) {
		return gcode.replaceAll("\\s{0,}\\n", "\n").replaceAll(";\\s+;", ";");
	}

	private static void buildStartGCode() {
		startGCode.append(";*********** BEGIN CUSTOM START GCODE *********").append(ret);
		String command = "";
		int    len     = 0;
		if (holdHotEnd) {
			startGCode.append(format("M104 S", hotEndHoldTemp, "; Start heating the hot end to pre temp"));
			startGCode.append(format("M140 S", bedHoldTemp, "; Start heating the bed to pre temp"));
		}
		if (homeHotEnd) {
			startGCode.append(format("G28", 0, "; Home Hot End"));
		}
		if (holdHotEnd) {
			startGCode.append(format("M109 S", hotEndHoldTemp, "; Wait for hot end to reach pre temp"));
			startGCode.append(format("M190 S", bedHoldTemp, "; Wait for bed to reach pre temp"));
			startGCode.append(format("", 0, "; Once bed reaches pre-temp, set final temps"));
			startGCode.append(format("M104 S", hotEndPrintTemp, "; Continue heating the hot end to final temp"));
			startGCode.append(format("M140 S", bedHoldTemp, "; Continue heating the bed to final temp"));
			startGCode.append(format("M109 S", hotEndPrintTemp, "; Wait for hot end to reach final temp"));
			startGCode.append(format("M190 S", bedHoldTemp, "; Wait for bed to reach final temp"));
		}
		startGCode.append(";***********  END CUSTOM START GCODE  *********").append(ret);
		addStartGCode = true;
	}

	private static String format(String command, double value, String comment) {
		String newCommand = command + (value == 0 ? "" : value);
		int    len        = newCommand.length();
		return newCommand + " ".repeat(14 - len) + comment + ret;
	}

	private static int findLastBedTemp(LinkedList<String> gcode) {
		int bedTemp;
		int finalBedTemp = 100;
		for (String line : gcode) {
			if (line.startsWith("M140") || line.startsWith("M190")) {
				String temp = line.replaceAll("(M\\d+|M\\d+\\s+)(S)(\\d+)(.+|)", "$3");
				bedTemp = Integer.parseInt(temp);
				if (bedTemp > 20) {
					finalBedTemp = Math.min(bedTemp, finalBedTemp);
				}
			}
		}
		return changeTempAtLayer ? Math.min(finalBedTemp, bedAtLayerTemp) : finalBedTemp;
	}

	private static String getEndGCode() {
		String endGCode = AppSettings.get().endGCode();
		if (endGCode.isEmpty()) {
			endGCode = getCustomEndGCode();
		}
		return endGCode;
	}

	private static boolean atLayerMarker(String line) {
		return line.startsWith(";LAYER:");
	}

	private static int findStartCodeInsertionLine(LinkedList<String> gcode) {
		int lineNumber = 0;
		for (String line : gcode) {
			lineNumber++;
			if (line.startsWith("G") || line.startsWith("M")) {break;}
		}
		return lineNumber;
	}

	private static int findEndCodeInsertionPoint(LinkedList<String> gcode) {
		int lineNumber      = 0;
		int finalLineNumber = 0;
		for (String line : gcode) {
			lineNumber++;
			if (line.startsWith("G") || line.startsWith("M")) {
				finalLineNumber = lineNumber;
			}
		}
		return finalLineNumber;
	}

	private static LinkedList<String> getGCodeLinkedList() {
		return AppSettings.getGCodeList();
	}

	private static Path getFinalPath() {
		String fileBase    = AppSettings.getBaseFilename();
		String rootFolder  = AppSettings.getRootFolder();
		String newFileName = fileBase + "Modified.gcode";
		return Paths.get(rootFolder, newFileName);
	}

	private static int getFirstLayerForCoolDown() {
		int     layer        = time.getLayerCount();
		double  totalMinutes = 0;
		boolean finished     = false;
		while (!finished) {
			double layerTime = time.getTimeForLayer(layer);
			double minutes   = layerTime / 60;
			totalMinutes += minutes;
			finished = totalMinutes >= fadeTime;
			layer--;
		}
		return layer;
	}

	public static String getCustomEndGCode() {
		return """
				; ***** - FINAL CUSTOM ENDING GCODE
				M107		; Fan off
				G91		; Relative Movement Mode
				G1 E-1 F2000	; Retract some filament
				G1 Z5		; Move head up
				M104 S0		; Turn off hot end
				M140 S0		; Turn off bed heater
				G90		; Absolute reference mode
				G1 X0		; Home X Axis
				G1 Y275		; Present print
				M84		; Disable motors
				;************************************* - END
				""";
	}
}
