package com.simtechdata.finalgcode.settings;

import com.simtechdata.finalgcode.enums.OS;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.prefs.BackingStoreException;

import static com.simtechdata.finalgcode.enums.OS.*;

public class AppSettings {

	private static final Get     getter     = new Get();
	private static final Set     setter     = new Set();
	private static final Clear   clear      = new Clear();
	private static final String  OSystem    = System.getProperty("os.name").toLowerCase();
	private static       Taskbar taskbar;
	private static       JFrame  jFrame;
	private static final Path    monacoPath = Paths.get(getAppFolder().toString(), "Monaco.ttf");
	private static       Font    fontMonaco;
	private static       String  gcode      = "";
	private static final String  ret        = System.getProperty("line.separator");
	private static Path gcodePath;


	public static void setGCode(Path gcodePath) {
		AppSettings.gcodePath = gcodePath;
		try {
			gcode     = FileUtils.readFileToString(gcodePath.toFile(), Charset.defaultCharset());
			formatGCode();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setGCode(String gcode) {
		AppSettings.gcode = gcode;
		formatGCode();
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

	public static String getFilename() {
		return gcodePath == null ? "" : gcodePath.toFile().getName();
	}

	public static String getBaseFilename() {
		return FilenameUtils.getBaseName(gcodePath != null ?  gcodePath.getFileName().toString() : "");
	}

	public static String getRootFolder() {
		return gcodePath != null ?  gcodePath.toFile().getParentFile().toPath().toString() : "";
	}

	public static LinkedList<String> getGCodeList() {
		return new LinkedList<>(Arrays.stream(gcode.split(ret)).toList());
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

	public static void setJFrame(JFrame jFrame) {
		AppSettings.jFrame = jFrame;
	}

	public static void setTaskbar(Taskbar taskbar) {
		AppSettings.taskbar = taskbar;
	}

	public static OS getOS() {
		if (OSystem.toLowerCase().contains("win")) {return WINDOWS;}
		else if (OSystem.toLowerCase().contains("mac")) {return MAC;}
		else {return LINUX;}
	}

	public static void clearAllSettings() {
		try {
			LABEL.prefs.clear();
		}
		catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
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
}
