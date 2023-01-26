package com.simtechdata.finalgcode.settings;

import com.simtechdata.finalgcode.enums.Filament;

import java.util.prefs.Preferences;

public class Set {

	private final Preferences prefs = LABEL.prefs;

	public void addEndGCode(boolean value) {
		AppSettings.clear().addEndGCode();
		prefs.putBoolean(LABEL.ADD_END_GCODE.Name(), value);
	}

	public void endGCode(String value) {
		AppSettings.clear().endGCode();
		prefs.put(LABEL.END_GCODE.Name(), value);
	}

	public void loadKlipperBedMesh(boolean value) {
		AppSettings.clear().loadKlipperBedMesh();
		prefs.putBoolean(LABEL.LOAD_KLIPPER_BED_MESH.Name(), value);
	}

	public void holdHotEndTemp(boolean value) {
		AppSettings.clear().holdHotEndTemp();
		prefs.putBoolean(LABEL.HOLD_HOT_END_TEMP.Name(), value);
	}

	public void hotEndHoldTempPLA(int value) {
		AppSettings.clear().hotEndHoldTempPLA();
		prefs.putInt(LABEL.HOT_END_HOLD_TEMP_PLA.Name(), value);
	}

	public void hotEndHoldTempABS(int value) {
		AppSettings.clear().hotEndHoldTempABS();
		prefs.putInt(LABEL.HOT_END_HOLD_TEMP_ABS.Name(), value);
	}

	public void hotEndPrintTempPLA(int value) {
		AppSettings.clear().hotEndPrintTempPLA();
		prefs.putInt(LABEL.HOT_END_PRINT_TEMP_PLA.Name(), value);
	}

	public void hotEndPrintTempABS(int value) {
		AppSettings.clear().hotEndPrintTempABS();
		prefs.putInt(LABEL.HOT_END_PRINT_TEMP_ABS.Name(), value);
	}

	public void bedHoldTempPLA(int value) {
		AppSettings.clear().bedHoldTempPLA();
		prefs.putInt(LABEL.BED_HOLD_TEMP_PLA.Name(), value);
	}

	public void bedHoldTempABS(int value) {
		AppSettings.clear().bedHoldTempABS();
		prefs.putInt(LABEL.BED_HOLD_TEMP_ABS.Name(), value);
	}

	public void bedPrintTempPLA(int value) {
		AppSettings.clear().bedPrintTempPLA();
		prefs.putInt(LABEL.BED_PRINT_TEMP_PLA.Name(), value);
	}

	public void bedPrintTempABS(int value) {
		AppSettings.clear().bedPrintTempABS();
		prefs.putInt(LABEL.BED_PRINT_TEMP_ABS.Name(), value);
	}

	public void setBedTempAtLayer(boolean value) {
		AppSettings.clear().setBedTempAtLayer();
		prefs.putBoolean(LABEL.SET_BED_TEMP_AT_LAYER.Name(), value);
	}

	public void bedTempAtLayerPLA(int value) {
		AppSettings.clear().bedTempAtLayerPLA();
		prefs.putInt(LABEL.BED_TEMP_AT_LAYER_PLA.Name(), value);
	}

	public void bedTempAtLayerABS(int value) {
		AppSettings.clear().bedTempAtLayerABS();
		prefs.putInt(LABEL.BED_TEMP_AT_LAYER_ABS.Name(), value);
	}

	public void layerForNewBedTempPLA(int value) {
		AppSettings.clear().layerForNewBedTempPLA();
		prefs.putInt(LABEL.LAYER_FOR_NEW_BED_TEMP_PLA.Name(), value);
	}

	public void layerForNewBedTempABS(int value) {
		AppSettings.clear().layerForNewBedTempABS();
		prefs.putInt(LABEL.LAYER_FOR_NEW_BED_TEMP_ABS.Name(), value);
	}

	public void fadeBedTempAtEndOfPrint(boolean value) {
		AppSettings.clear().fadeBedTempAtEndOfPrint();
		prefs.putBoolean(LABEL.FADE_BED_TEMP_AT_END_OF_PRINT.Name(), value);
	}

	public void finalBedTempForFade(Filament filament, int value) {
		AppSettings.clear().finalBedTempForFade(filament);
		switch (filament) {
			case PLA -> prefs.putInt(LABEL.FINAL_BED_TEMP_FOR_FADE_PLA.Name(), value);
			case ABS -> prefs.putInt(LABEL.FINAL_BED_TEMP_FOR_FADE_ABS.Name(), value);
		}
	}

	public void timeForBedTempFade(Filament filament, int value) {
		AppSettings.clear().timeForBedTempFade(filament);
		switch (filament) {
			case PLA -> prefs.putInt(LABEL.TIME_FOR_BED_TEMP_FADE_PLA.Name(), value);
			case ABS -> prefs.putInt(LABEL.TIME_FOR_BED_TEMP_FADE_ABS.Name(), value);
		}
	}

	public void homeHotEnd(boolean value) {
		AppSettings.clear().homeHotEnd();
		prefs.putBoolean(LABEL.HOME_HOT_END.Name(), value);
	}

	public void lastGcodeFolder(String value) {
		AppSettings.clear().lastGcodeFolder();
		prefs.put(LABEL.LAST_GCODE_FOLDER.Name(), value);
	}


}
