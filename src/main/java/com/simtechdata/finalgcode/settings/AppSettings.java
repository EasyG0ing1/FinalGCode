package com.simtechdata.finalgcode.settings;

import com.simtechdata.finalgcode.enums.OS;
import com.simtechdata.finalgcode.processing.ZSet;
import com.simtechdata.finalgcode.processing.structure.Line;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.simtechdata.finalgcode.enums.OS.*;

public class AppSettings {

	private static final Get                getter        = new Get();
	private static final Set                setter        = new Set();
	private static final Clear              clear         = new Clear();
	private static final String             OSystem       = System.getProperty("os.name").toLowerCase();
	private static final Path               monacoPath    = Paths.get(getAppFolder().toString(), "Monaco.ttf");
	private static final Path               infoIconPath  = Paths.get(getAppFolder().toString(), "InfoIcon.png");
	private static       Font               fontMonaco;
	private static       String             gcode         = "";
	private static final String             ret           = System.getProperty("line.separator");
	private static       Path               gcodePath;
	private static       Path               gcodeOutPath;
	private static final Map<Integer, Line> gcodeLineMap  = new HashMap<>();
	private static       LinkedList<String> gcodeList     = new LinkedList<>();
	private static final LinkedList<Line>   gcodeLineList = new LinkedList<>();


	public static void setGCode(Path gcodePath) {
		AppSettings.gcodePath = gcodePath;
		try {
			gcode = FileUtils.readFileToString(gcodePath.toFile(), Charset.defaultCharset());
			formatGCode();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		gcodeLineMap.clear();
		gcodeList.clear();
		gcodeLineList.clear();
	}

	public static void setGCode(String gcode) {
		AppSettings.gcode = gcode;
		formatGCode();
		gcodeLineMap.clear();
		gcodeList.clear();
		gcodeLineList.clear();
	}

	public static void setOutPath(Path outPath) {
		gcodeOutPath = outPath;
	}

	public static Map<Integer, Line> getGcodeLineMap() {
		checkLists();
		return gcodeLineMap;
	}

	public static LinkedList<String> getGcodeList() {
		checkLists();
		return gcodeList;
	}

	private static void checkLists() {
		if (gcodeList.isEmpty()) {
			gcodeList = new LinkedList<>(Arrays.stream(gcode.split("\\n")).toList());
		}
		if (gcodeLineMap.isEmpty()) {
			int index = 0;
			for (String line : gcodeList) {
				gcodeLineMap.put(index, ZSet.getLine(line));
				index++;
			}
		}
		if (gcodeLineList.isEmpty()) {
			for (String line : gcodeList) {
				gcodeLineList.addLast(ZSet.getLine(line));
			}
		}
	}

	private static void formatGCode() {
		LinkedList<String> gcodeList = new LinkedList<>(Arrays.stream(gcode.split("\\n")).toList());
		StringBuilder      sb        = new StringBuilder();
		for (String line : gcodeList) {
			if (line.length() < 3) {sb.append(";").append(ret);}
			else {sb.append(line).append(ret);}
		}
		AppSettings.gcode = sb.toString();
	}

	public static String getGCode() {
		return gcode;
	}

	public static boolean gcodePathNull() {
		return gcodePath == null;
	}

	public static String getOutFilename() {
		return gcodeOutPath == null ? "" : FilenameUtils.getName(gcodeOutPath.toString());
	}

	public static File getOutFile() {
		return gcodeOutPath == null ? null : gcodeOutPath.toFile();
	}

	public static Font getFontMonaco(double size) {
		if (fontMonaco == null) {
			fontMonaco = Font.loadFont("file:" + monacoPath, size);
		}
		return fontMonaco;
	}

	public static Get get() {
		return getter;
	}

	public static Set set() {
		return setter;
	}

	public static Clear clear() {
		return clear;
	}

	public static OS getOS() {
		if (OSystem.toLowerCase().contains("win")) {return WINDOWS;}
		else if (OSystem.toLowerCase().contains("mac")) {return MAC;}
		else {return LINUX;}
	}

	public static String getInfoIcon() {
		return "file:" + infoIconPath;
	}

	public static Path getAppFolder() {
		Path path;
		if (getOS() == WINDOWS) {
			path = Paths.get(System.getenv("APPDATA"), "FinalGCode");
		}
		else {
			path = Paths.get(System.getProperty("user.dir"));
		}
		return path;
	}

	public static void sleep(long time) {
		try {
			TimeUnit.MILLISECONDS.sleep(time);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
