package com.simtechdata.finalgcode.processing;

public record UserChoices(boolean homeHotEnd,
						  boolean addEndGCode,

						  boolean holdHotEnd,
						  boolean loadBedMesh,
						  int hotEndHoldTemp,
						  int hotEndPrintTemp,
						  int bedHoldTemp,
						  int bedPrintTemp,

						  boolean changeTempAtLayer,
						  int bedAtLayerTemp,
						  int layerToChangeBedTemp,

						  boolean fadeBedTemp,
						  int finalFadeTemp,
						  int fadeTime) {

}
