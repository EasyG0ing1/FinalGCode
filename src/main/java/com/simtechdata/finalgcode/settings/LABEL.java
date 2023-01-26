package com.simtechdata.finalgcode.settings;

import java.util.prefs.Preferences;

public enum LABEL {
	ADD_END_GCODE,
	END_GCODE,

	LOAD_KLIPPER_BED_MESH,

	HOLD_HOT_END_TEMP,
	HOT_END_HOLD_TEMP_PLA,
	HOT_END_HOLD_TEMP_ABS,
	HOT_END_PRINT_TEMP_PLA,
	HOT_END_PRINT_TEMP_ABS,
	BED_HOLD_TEMP_PLA,
	BED_HOLD_TEMP_ABS,
	BED_PRINT_TEMP_PLA,
	BED_PRINT_TEMP_ABS,

	SET_BED_TEMP_AT_LAYER,
	BED_TEMP_AT_LAYER_PLA,
	BED_TEMP_AT_LAYER_ABS,
	LAYER_FOR_NEW_BED_TEMP_PLA,
	LAYER_FOR_NEW_BED_TEMP_ABS,

	FADE_BED_TEMP_AT_END_OF_PRINT,
	FINAL_BED_TEMP_FOR_FADE_PLA,
	FINAL_BED_TEMP_FOR_FADE_ABS,
	TIME_FOR_BED_TEMP_FADE_PLA,
	TIME_FOR_BED_TEMP_FADE_ABS,
	HOME_HOT_END,
	LAST_GCODE_FOLDER;

	public String Name(LABEL this) {
		return switch (this) {
			case ADD_END_GCODE -> "Add_End_Gcode";
			case END_GCODE -> "End_Gcode";
			case LOAD_KLIPPER_BED_MESH -> "Load_Klipper_Bed_Mesh";
			case HOLD_HOT_END_TEMP -> "Hold_Hot_End_Temp";
			case HOT_END_HOLD_TEMP_PLA -> "Hot_End_Hold_Temp_Pla";
			case HOT_END_HOLD_TEMP_ABS -> "Hot_End_Hold_Temp_Abs";
			case HOT_END_PRINT_TEMP_PLA -> "Hot_End_Print_Temp_Pla";
			case HOT_END_PRINT_TEMP_ABS -> "Hot_End_Print_Temp_Abs";
			case BED_HOLD_TEMP_PLA -> "Bed_Hold_Temp_Pla";
			case BED_HOLD_TEMP_ABS -> "Bed_Hold_Temp_Abs";
			case BED_PRINT_TEMP_PLA -> "Bed_Print_Temp_Pla";
			case BED_PRINT_TEMP_ABS -> "Bed_Print_Temp_Abs";
			case SET_BED_TEMP_AT_LAYER -> "Set_Bed_Temp_At_Layer";
			case BED_TEMP_AT_LAYER_PLA -> "Bed_Temp_At_Layer_Pla";
			case BED_TEMP_AT_LAYER_ABS -> "Bed_Temp_At_Layer_Abs";
			case LAYER_FOR_NEW_BED_TEMP_PLA -> "Layer_For_New_Bed_Temp_Pla";
			case LAYER_FOR_NEW_BED_TEMP_ABS -> "Layer_For_New_Bed_Temp_Abs";
			case FADE_BED_TEMP_AT_END_OF_PRINT -> "Fade_Bed_Temp_At_End_Of_Print";
			case FINAL_BED_TEMP_FOR_FADE_PLA -> "Final_Bed_Temp_For_Fade_Pla";
			case FINAL_BED_TEMP_FOR_FADE_ABS -> "Final_Bed_Temp_For_Fade_Abs";
			case TIME_FOR_BED_TEMP_FADE_PLA -> "Time_For_Bed_Temp_Fade_Pla";
			case TIME_FOR_BED_TEMP_FADE_ABS -> "Time_For_Bed_Temp_Fade_Abs";
			case HOME_HOT_END -> "Home_Hot_End";
			case LAST_GCODE_FOLDER -> "Last_Gcode_Folder";
		};
	}


	public static final Preferences prefs = Preferences.userNodeForPackage(LABEL.class);

}
