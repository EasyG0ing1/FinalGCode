package com.simtechdata.finalgcode.settings;

import com.simtechdata.finalgcode.enums.Filament;

import java.io.File;
import java.util.prefs.Preferences;

public class Get {

	private final Preferences prefs = LABEL.prefs;

	public boolean addEndGCode() {
		return prefs.getBoolean(LABEL.ADD_END_GCODE.Name(), false);
	}

	public String endGCode() {
		return prefs.get(LABEL.END_GCODE.Name(), "");
	}

	public boolean loadKlipperBedMesh() {
		return prefs.getBoolean(LABEL.LOAD_KLIPPER_BED_MESH.Name(), false);
	}

	public boolean holdHotEndTemp() {
		return prefs.getBoolean(LABEL.HOLD_HOT_END_TEMP.Name(), false);
	}

	public int hotEndHoldTemp(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.HOT_END_HOLD_TEMP_PLA.Name(), 160);
			case ABS -> prefs.getInt(LABEL.HOT_END_HOLD_TEMP_ABS.Name(), 200);
		};
	}

	public int hotEndPrintTemp(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.HOT_END_PRINT_TEMP_PLA.Name(), 205);
			case ABS -> prefs.getInt(LABEL.HOT_END_PRINT_TEMP_ABS.Name(), 250);
		};
	}

	public int bedHoldTemp(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.BED_HOLD_TEMP_PLA.Name(), 60);
			case ABS -> prefs.getInt(LABEL.BED_HOLD_TEMP_ABS.Name(), 95);
		};
	}

	public int bedPrintTemp(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.BED_PRINT_TEMP_PLA.Name(), 65);
			case ABS -> prefs.getInt(LABEL.BED_PRINT_TEMP_ABS.Name(), 100);
		};
	}

	public boolean setBedTempAtLayer() {
		return prefs.getBoolean(LABEL.SET_BED_TEMP_AT_LAYER.Name(), false);
	}

	public int bedTempAtLayer(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.BED_TEMP_AT_LAYER_PLA.Name(), 55);
			case ABS -> prefs.getInt(LABEL.BED_TEMP_AT_LAYER_ABS.Name(), 90);
		};
	}

	public int layerForNewBedTemp(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.LAYER_FOR_NEW_BED_TEMP_PLA.Name(), 5);
			case ABS -> prefs.getInt(LABEL.LAYER_FOR_NEW_BED_TEMP_ABS.Name(), 5);
		};
	}

	public boolean fadeBedTempAtEndOfPrint() {
		return prefs.getBoolean(LABEL.FADE_BED_TEMP_AT_END_OF_PRINT.Name(), false);
	}

	public int finalBedTempForFade(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.FINAL_BED_TEMP_FOR_FADE_PLA.Name(), 40);
			case ABS -> prefs.getInt(LABEL.FINAL_BED_TEMP_FOR_FADE_ABS.Name(), 40);
		};
	}

	public int timeForBedTempFade(Filament filament) {
		return switch (filament) {
			case PLA -> prefs.getInt(LABEL.TIME_FOR_BED_TEMP_FADE_PLA.Name(), 15);
			case ABS -> prefs.getInt(LABEL.TIME_FOR_BED_TEMP_FADE_ABS.Name(), 15);
		};
	}

	public boolean homeHotEnd() {
		return prefs.getBoolean(LABEL.HOME_HOT_END.Name(), true);
	}

	public File lastGcodeFolder() {
		return new File(prefs.get(LABEL.LAST_GCODE_FOLDER.Name(), System.getProperty("user.home")));
	}

}
