package com.simtechdata.finalgcode.settings;

import com.simtechdata.finalgcode.enums.Filament;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Clear {

	private final Preferences prefs = LABEL.prefs;

	public void addEndGCode() {
		prefs.remove(LABEL.ADD_END_GCODE.Name());
	}

	public void endGCode() {
		prefs.remove(LABEL.END_GCODE.Name());
	}

	public void loadKlipperBedMesh() {
		prefs.remove(LABEL.LOAD_KLIPPER_BED_MESH.Name());
	}

	public void holdHotEndTemp() {
		prefs.remove(LABEL.HOLD_HOT_END_TEMP.Name());
	}

	public void hotEndHoldTempPLA() {
		prefs.remove(LABEL.HOT_END_HOLD_TEMP_PLA.Name());
	}

	public void hotEndHoldTempABS() {
		prefs.remove(LABEL.HOT_END_HOLD_TEMP_ABS.Name());
	}

	public void hotEndPrintTempPLA() {
		prefs.remove(LABEL.HOT_END_PRINT_TEMP_PLA.Name());
	}

	public void hotEndPrintTempABS() {
		prefs.remove(LABEL.HOT_END_PRINT_TEMP_ABS.Name());
	}

	public void bedHoldTempPLA() {
		prefs.remove(LABEL.BED_HOLD_TEMP_PLA.Name());
	}

	public void bedHoldTempABS() {
		prefs.remove(LABEL.BED_HOLD_TEMP_ABS.Name());
	}

	public void bedPrintTempPLA() {
		prefs.remove(LABEL.BED_PRINT_TEMP_PLA.Name());
	}

	public void bedPrintTempABS() {
		prefs.remove(LABEL.BED_PRINT_TEMP_ABS.Name());
	}

	public void setBedTempAtLayer() {
		prefs.remove(LABEL.SET_BED_TEMP_AT_LAYER.Name());
	}

	public void bedTempAtLayerPLA() {
		prefs.remove(LABEL.BED_TEMP_AT_LAYER_PLA.Name());
	}

	public void bedTempAtLayerABS() {
		prefs.remove(LABEL.BED_TEMP_AT_LAYER_ABS.Name());
	}

	public void layerForNewBedTempPLA() {
		prefs.remove(LABEL.LAYER_FOR_NEW_BED_TEMP_PLA.Name());
	}

	public void layerForNewBedTempABS() {
		prefs.remove(LABEL.LAYER_FOR_NEW_BED_TEMP_ABS.Name());
	}

	public void fadeBedTempAtEndOfPrint() {
		prefs.remove(LABEL.FADE_BED_TEMP_AT_END_OF_PRINT.Name());
	}

	public void finalBedTempForFade(Filament filament) {
		switch (filament) {
			case PLA -> prefs.remove(LABEL.FINAL_BED_TEMP_FOR_FADE_PLA.Name());
			case ABS -> prefs.remove(LABEL.FINAL_BED_TEMP_FOR_FADE_ABS.Name());
		}
	}

	public void timeForBedTempFade(Filament filament) {
		switch (filament) {
			case PLA -> prefs.remove(LABEL.TIME_FOR_BED_TEMP_FADE_PLA.Name());
			case ABS -> prefs.remove(LABEL.TIME_FOR_BED_TEMP_FADE_ABS.Name());
		}
	}

	public void homeHotEnd() {
		prefs.remove(LABEL.HOME_HOT_END.Name());
	}

	public void lastGcodeFolder() {
		prefs.remove(LABEL.LAST_GCODE_FOLDER.Name());
	}

	public void ALL() {
		try {
			prefs.clear();
		}
		catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

}
