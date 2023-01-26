package com.simtechdata.finalgcode.guis;

import com.simtechdata.easyfxcontrols.containers.CHBox;
import com.simtechdata.easyfxcontrols.containers.CVBox;
import com.simtechdata.easyfxcontrols.controls.*;
import com.simtechdata.finalgcode.enums.Filament;
import com.simtechdata.finalgcode.processing.GCode;
import com.simtechdata.finalgcode.processing.Time;
import com.simtechdata.finalgcode.processing.Tracking;
import com.simtechdata.finalgcode.processing.UserChoices;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

import static com.simtechdata.finalgcode.enums.Filament.ABS;
import static com.simtechdata.finalgcode.enums.Filament.PLA;

public class GUI {

	public GUI(Path gcodePath) {
		Path newGcodePath = null;
		if (gcodePath == null) {
			newGcodePath = loadGCodeFile();
		}
		else {
			newGcodePath = gcodePath;
		}
		AppSettings.setGCode(newGcodePath);
		time = new Time();
		if (System.getenv("SLIC3R_FILAMENT_TYPE") != null) {
			String filament = System.getenv("SLIC3R_FILAMENT_TYPE");
			this.filament      = filament.equals("ABS") ? ABS : PLA;
			lockFilamentChoice = filament.equals("ABS") || filament.matches("PLA");
			GCode.loadGCode(this.filament);
		}
		makeControls();
		updateSummary();
		if (!cbHoldHotEnd.isSelected()) {height -= 50;}
		if (!cbLayerBedTemp.isSelected()) {height -= 50;}
		if (!cbFadeBedTemp.isSelected()) {height -= 50;}
		SceneOne.set(sceneId, vbox, width, height).onCloseEvent(e -> cancel()).centered().show();
	}

	private final Time       time;
	private final String     sceneId            = SceneOne.getRandom(25);
	private final double     width              = 600;
	private       double     height             = 640;
	private       CVBox      vbox;
	private       Filament   filament           = PLA;
	private       boolean    lockFilamentChoice = false;
	private       CLabel     lblSummary;
	private       CLabel     lblLayerTime;
	private       CLabel     lblLayerTimeLeft;
	private       CLabel     lblMode;
	private       CTextField tfHotEndHoldTemp;
	private       CTextField tfHotEndPrintTemp;
	private       CTextField tfBedHoldTemp;
	private       CTextField tfBedPrintTemp;
	private       CTextField tfBedTempAtLayer;
	private       CTextField tfLayerForNewBedTemp;
	private       CTextField tfFinalBedTempForFade;
	private       CTextField tfTimeForBedTempFade;
	private       CVBox      vboxHoldHotEnd;
	private       CVBox      vboxBedTempAtLayer;
	private       CVBox      vboxFadeBedTemp;
	private       CCheckBox  cbHoldHotEnd;
	private       CCheckBox  cbLayerBedTemp;
	private       CCheckBox  cbFadeBedTemp;
	private       CHBox      boxSlider;
	private       CVBox      sliderBox;
	private       CVBox      boxButtons;
	private       CLabel     lblProcessing;
	private       CCheckBox  cbAddEndGCode;
	private       CCheckBox  cbLoadBedMesh;
	private       CLabel     lblTotalLayers;
	private       CLabel     lblTotalHeight;
	private       CLabel     lblCurrentLayer;
	private       CLabel     lblLayersLeft;
	private       CLabel     lblLayerHeight;
	private       CLabel     lblHeightLeft;

	private void setHeight() {
		if (SceneOne.sceneExists(sceneId)) {SceneOne.setHeight(sceneId, height);}
	}

	private CHBox getKlipperBedMesh() {
		cbLoadBedMesh = new CCheckBox.Builder().selected(AppSettings.get().loadKlipperBedMesh()).toolTip("Klipper no longer loads the bed mesh by default, checking this box will make sure the default profile gets loaded").build();
		CLabel lblBedMesh = new CLabel.Builder("Load default bed mesh profile in Klipper").width(215).alignment(Pos.CENTER_LEFT).build();
		cbLoadBedMesh.selectedProperty().addListener((ob, ov, nv) -> AppSettings.set().loadKlipperBedMesh(nv));
		return new CHBox.Builder(15, lblBedMesh, cbLoadBedMesh).padding(10, 0, 0, 0).alignment(Pos.CENTER_LEFT).build();
	}

	private CHBox getEndGCode() {
		CLabel lblAddEndGCode = new CLabel.Builder("Add Ending GCode").build();
		cbAddEndGCode = new CCheckBox.Builder().selected(AppSettings.get().addEndGCode()).build();
		Button btnAddEndCode = new Button.Builder().text("GCode").width(65).visible(cbAddEndGCode.isSelected()).build();
		CHBox  boxCheckEnd   = new CHBox.Builder(15, lblAddEndGCode, cbAddEndGCode, btnAddEndCode).padding(10, 0, 0, 0).build();
		cbAddEndGCode.selectedProperty().addListener((observable, oldValue, newValue) -> {
			btnAddEndCode.setVisible(newValue);
			AppSettings.set().addEndGCode(newValue);
			if (AppSettings.get().endGCode().isEmpty()) {
				AppSettings.set().endGCode(GCode.getCustomEndGCode());
			}
		});
		btnAddEndCode.setOnAction(e -> new EndGCode().editGCode());
		return boxCheckEnd;
	}

	private CVBox getHoldHotEnd() {
		cbHoldHotEnd = new CCheckBox.Builder().selected(AppSettings.get().holdHotEndTemp()).build();
		CLabel lblHoldHotEnd = new CLabel.Builder("Hold hot end at lower temp while bed heats").build();
		CLabel lblHoldTemp   = new CLabel.Builder("Hold").width(45).build();
		CLabel lblPrintTemp  = new CLabel.Builder("Print").width(45).build();
		CLabel lblBedHold    = new CLabel.Builder("Bed Hold").width(55).build();
		CLabel lblBedPrint   = new CLabel.Builder("Bed Print").width(55).build();
		tfHotEndHoldTemp  = new CTextField.Builder().width(45).toolTip("Temp to hold hot end at while the bed heats").build();
		tfHotEndPrintTemp = new CTextField.Builder().width(45).toolTip("Temperature that the hot end needs to be when the print starts").build();
		tfBedHoldTemp     = new CTextField.Builder().width(55).toolTip("When the bed reaches this temp, the hot end will be set to its final print temp").build();
		tfBedPrintTemp    = new CTextField.Builder().width(55).toolTip("Temp the bed needs to be when the print starts").build();
		tfHotEndHoldTemp.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfHotEndHoldTemp.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().hotEndHoldTempPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().hotEndHoldTempABS(Integer.parseInt(newValue));
			}
		});
		tfHotEndPrintTemp.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfHotEndPrintTemp.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().hotEndPrintTempPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().hotEndPrintTempABS(Integer.parseInt(newValue));
			}
		});
		tfBedHoldTemp.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfBedHoldTemp.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().bedHoldTempPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().bedHoldTempABS(Integer.parseInt(newValue));
			}
		});
		tfBedPrintTemp.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfBedPrintTemp.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().bedPrintTempPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().bedPrintTempABS(Integer.parseInt(newValue));
			}
		});
		CHBox boxCheck      = new CHBox.Builder(15, cbHoldHotEnd, lblHoldHotEnd).build();
		CHBox boxLabels     = new CHBox.Builder(15, lblHoldTemp, lblPrintTemp, lblBedHold, lblBedPrint).padding(0, 0, 0, 35).visible(cbHoldHotEnd.isSelected()).build();
		CHBox boxTextFields = new CHBox.Builder(15, tfHotEndHoldTemp, tfHotEndPrintTemp, tfBedHoldTemp, tfBedPrintTemp).padding(0, 0, 0, 35).visible(cbHoldHotEnd.isSelected()).build();
		cbHoldHotEnd.selectedProperty().addListener((observable, oldValue, newValue) -> {
			boxLabels.setVisible(newValue);
			boxTextFields.setVisible(newValue);
			AppSettings.set().holdHotEndTemp(newValue);
			double heightChange = newValue ? 55 : -55;
			height += heightChange;
			setHeight();
			vbox.getChildren().removeAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
			if (newValue) {vboxHoldHotEnd = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(15, 0, 10, 0)).build();}
			else {vboxHoldHotEnd = new CVBox.Builder(5, boxCheck).padding(new Insets(15, 0, 10, 0)).build();}
			vbox.getChildren().addAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
		});
		tfHotEndHoldTemp.setText(String.valueOf(AppSettings.get().hotEndHoldTemp(filament)));
		tfHotEndPrintTemp.setText(String.valueOf(AppSettings.get().hotEndPrintTemp(filament)));
		tfBedHoldTemp.setText(String.valueOf(AppSettings.get().bedHoldTemp(filament)));
		tfBedPrintTemp.setText(String.valueOf(AppSettings.get().bedPrintTemp(filament)));
		if (cbHoldHotEnd.isSelected()) {vboxHoldHotEnd = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(15, 0, 10, 0)).build();}
		else {
			vboxHoldHotEnd = new CVBox.Builder(5, boxCheck).padding(new Insets(15, 0, 10, 0)).build();
		}
		return vboxHoldHotEnd;
	}

	private CVBox getBedTempAtLayer() {
		cbLayerBedTemp = new CCheckBox.Builder().selected(AppSettings.get().setBedTempAtLayer()).build();
		CLabel lblLayerBedTemp         = new CLabel.Builder("Set Bed Temp To Lower Value At Specified Layer").build();
		CLabel lblNewLayerBedTemp      = new CLabel.Builder("New Temp").width(65).build();
		CLabel lblLayerToChangeBedTemp = new CLabel.Builder("Layer").width(45).build();
		tfBedTempAtLayer     = new CTextField.Builder().width(65).toolTip("Temp set the bed to when it reaches designated layer").build();
		tfLayerForNewBedTemp = new CTextField.Builder().width(45).toolTip("Designated layer at which to set the new bed temp").build();
		tfBedTempAtLayer.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfBedTempAtLayer.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().bedTempAtLayerPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().hotEndHoldTempABS(Integer.parseInt(newValue));
			}
		});
		tfLayerForNewBedTemp.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfLayerForNewBedTemp.setText(value);
			switch (filament) {
				case PLA -> AppSettings.set().layerForNewBedTempPLA(Integer.parseInt(newValue));
				case ABS -> AppSettings.set().layerForNewBedTempABS(Integer.parseInt(newValue));
			}
		});
		CHBox boxCheck      = new CHBox.Builder(15, cbLayerBedTemp, lblLayerBedTemp).build();
		CHBox boxLabels     = new CHBox.Builder(15, lblNewLayerBedTemp, lblLayerToChangeBedTemp).visible(cbLayerBedTemp.isSelected()).padding(0, 0, 0, 35).build();
		CHBox boxTextFields = new CHBox.Builder(15, tfBedTempAtLayer, tfLayerForNewBedTemp).visible(cbLayerBedTemp.isSelected()).padding(0, 0, 0, 35).build();
		cbLayerBedTemp.selectedProperty().addListener((observable, oldValue, newValue) -> {
			boxLabels.setVisible(newValue);
			boxTextFields.setVisible(newValue);
			AppSettings.set().setBedTempAtLayer(newValue);
			double heightChange = newValue ? 55 : -55;
			height += heightChange;
			setHeight();
			vbox.getChildren().removeAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
			if (newValue) {vboxBedTempAtLayer = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(0, 0, 10, 0)).build();}
			else {vboxBedTempAtLayer = new CVBox.Builder(5, boxCheck).padding(new Insets(0, 0, 10, 0)).build();}

			vbox.getChildren().addAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
		});
		tfBedTempAtLayer.setText(String.valueOf(AppSettings.get().bedTempAtLayer(filament)));
		tfLayerForNewBedTemp.setText(String.valueOf(AppSettings.get().layerForNewBedTemp(filament)));
		if (cbLayerBedTemp.isSelected()) {vboxBedTempAtLayer = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(0, 0, 10, 0)).build();}
		else {
			vboxBedTempAtLayer = new CVBox.Builder(5, boxCheck).padding(new Insets(0, 0, 10, 0)).build();
		}

		return vboxBedTempAtLayer;
	}

	private CVBox getFadeBedTemp() {
		cbFadeBedTemp = new CCheckBox.Builder().selected(AppSettings.get().fadeBedTempAtEndOfPrint()).build();
		CLabel lblFadeBedTemp         = new CLabel.Builder("Fade bed temp at end of print").build();
		CLabel lblFinalBedTempForFade = new CLabel.Builder("Final Temp").width(65).build();
		CLabel lblTimeForBedTempFade  = new CLabel.Builder("Time").width(45).build();
		tfFinalBedTempForFade = new CTextField.Builder().width(65).toolTip("Temp that the bed should be at when done fading").build();
		tfTimeForBedTempFade  = new CTextField.Builder().width(45).toolTip("How much time (in minutes) it should take for the fade to happen").build();
		tfFinalBedTempForFade.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfFinalBedTempForFade.setText(value);
			AppSettings.set().finalBedTempForFade(filament, Integer.parseInt(newValue));

		});
		tfTimeForBedTempFade.textProperty().addListener((observable, oldValue, newValue) -> {
			String value = newValue.replaceAll("[^0-9]+", "");
			tfTimeForBedTempFade.setText(value);
			AppSettings.set().timeForBedTempFade(filament, Integer.parseInt(newValue));
		});
		CHBox boxCheck      = new CHBox.Builder(15, cbFadeBedTemp, lblFadeBedTemp).build();
		CHBox boxLabels     = new CHBox.Builder(15, lblFinalBedTempForFade, lblTimeForBedTempFade).visible(cbFadeBedTemp.isSelected()).padding(0, 0, 0, 35).build();
		CHBox boxTextFields = new CHBox.Builder(15, tfFinalBedTempForFade, tfTimeForBedTempFade).visible(cbFadeBedTemp.isSelected()).padding(0, 0, 0, 35).build();
		cbFadeBedTemp.selectedProperty().addListener((observable, oldValue, newValue) -> {
			boxLabels.setVisible(newValue);
			boxTextFields.setVisible(newValue);
			AppSettings.set().fadeBedTempAtEndOfPrint(newValue);
			double heightChange = newValue ? 55 : -55;
			height += heightChange;
			setHeight();
			vbox.getChildren().removeAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
			if (newValue) {vboxFadeBedTemp = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(0, 0, 10, 0)).build();}
			else {vboxFadeBedTemp = new CVBox.Builder(5, boxCheck).padding(new Insets(0, 0, 10, 0)).build();}
			vbox.getChildren().addAll(vboxHoldHotEnd, vboxBedTempAtLayer, vboxFadeBedTemp, boxSlider, sliderBox, boxButtons);
		});
		tfFinalBedTempForFade.setText(String.valueOf(AppSettings.get().finalBedTempForFade(filament)));
		tfTimeForBedTempFade.setText(String.valueOf(AppSettings.get().timeForBedTempFade(filament)));
		tfTimeForBedTempFade.focusedProperty().addListener((ob, ov, nv) -> {
			if (ov) {
				int totalTime = time.getTotalPrintTime();
				if (AppSettings.get().timeForBedTempFade(filament) > totalTime) {
					SceneOne.showMessage(400, 125, "Time entered for fade temp exceeds print time of " + totalTime + " minutes", true, Pos.CENTER_LEFT);
					tfTimeForBedTempFade.clear();
					tfTimeForBedTempFade.requestFocus();
					AppSettings.clear().timeForBedTempFade(filament);
				}
			}
		});
		if (cbFadeBedTemp.isSelected()) {
			vboxFadeBedTemp = new CVBox.Builder(5, boxCheck, boxLabels, boxTextFields).padding(new Insets(0, 0, 10, 0)).build();
		}
		else {
			vboxFadeBedTemp = new CVBox.Builder(5, boxCheck).padding(new Insets(0, 0, 10, 0)).build();
		}
		return vboxFadeBedTemp;
	}

	private void makeControls() {
		lblSummary = new CLabel.Builder().width(width * .9).alignment(Pos.CENTER).font(Font.font("Arial")).build();
		lblMode    = new CLabel.Builder().width(width * .8).build();
		CLabel               lblFilament = new CLabel.Builder("Filament").build();
		CChoiceBox<Filament> cbFilament  = new CChoiceBox.Builder<Filament>().width(65).item(Filament.ABS).item(PLA).build();
		Button               btnZHop     = new Button.Builder().text("Add ZHop").width(90).build();

		CLabel    lblHomeHotEnd = new CLabel.Builder("Home Hot End Before Print").build();
		CCheckBox cbHomeHotEnd  = new CCheckBox.Builder().build();
		Button    btnProcess    = new Button.Builder("Process GCode").disabled(AppSettings.gcodePathNull()).width(125).build();
		Button    btnCancel     = new Button.Builder("Cancel").onAction(e -> cancel()).width(60).build();
		lblTotalLayers   = new CLabel.Builder("Layers:").size(115, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblTotalHeight   = new CLabel.Builder("Height:").size(115, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblCurrentLayer  = new CLabel.Builder("Layer:").size(95, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblLayersLeft    = new CLabel.Builder("Remain:").size(95, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblLayerHeight   = new CLabel.Builder("Height:").size(120, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblHeightLeft    = new CLabel.Builder("Remain:").size(120, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblLayerTime     = new CLabel.Builder("Time:").size(300, 15).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblLayerTimeLeft = new CLabel.Builder("Remain:").width(300).alignment(Pos.CENTER_LEFT).font(Font.font("Monaco", 10)).build();
		lblProcessing    = new CLabel.Builder("").width(300).alignment(Pos.CENTER).font(Font.font("Monaco", 10), Color.BLUE).build();
		Slider slider = getSlider();

		//HBoxes
		CHBox boxMode       = newHBox(lblMode);
		CHBox boxSummary    = newHBox(lblSummary);
		CHBox boxFilament   = newHBox(lblFilament, cbFilament, btnZHop);
		CHBox boxHomeHotEnd = newHBox(lblHomeHotEnd, cbHomeHotEnd);
		boxHomeHotEnd.setPadding(new Insets(10, 0, 0, 0));
		boxSlider = newHBox(slider);
		CHBox boxLayerTop    = newHBox(15, lblTotalLayers, lblCurrentLayer, lblLayerHeight, lblLayerTime);
		CHBox boxLayerBottom = newHBox(15, lblTotalHeight, lblLayersLeft, lblHeightLeft, lblLayerTimeLeft);
		sliderBox = new CVBox.Builder(boxSlider, boxLayerTop, boxLayerBottom).padding(new Insets(0, 0, 10, 0)).build();
		CHBox boxBothButtons = newHBox(btnProcess, btnCancel);
		boxBothButtons.setAlignment(Pos.CENTER);
		boxButtons = new CVBox.Builder(10, boxBothButtons, lblProcessing).alignment(Pos.CENTER).build();

		vbox = new CVBox.Builder(
				boxMode,
				boxSummary,
				boxFilament,
				getKlipperBedMesh(),
				boxHomeHotEnd,
				getEndGCode(),
				getHoldHotEnd(),
				getBedTempAtLayer(),
				getFadeBedTemp(),
				sliderBox,
				boxButtons
		).padding(new Insets(10, 25, 10, 25)).size(width * .8, height).build();

		//Control Settings and Bindings
		boxMode.setAlignment(Pos.CENTER);
		boxButtons.setAlignment(Pos.CENTER);
		btnZHop.setOnAction(e -> new ZHop());
		cbHomeHotEnd.setOnAction(e -> AppSettings.set().homeHotEnd(cbHomeHotEnd.isSelected()));
		cbHomeHotEnd.setSelected(AppSettings.get().homeHotEnd());
		cbFilament.setOnAction(e -> {
			this.filament = cbFilament.getValue();
			tfHotEndHoldTemp.setText(String.valueOf(AppSettings.get().hotEndHoldTemp(filament)));
			tfHotEndPrintTemp.setText(String.valueOf(AppSettings.get().hotEndPrintTemp(filament)));
			tfBedHoldTemp.setText(String.valueOf(AppSettings.get().bedHoldTemp(filament)));
			tfBedPrintTemp.setText(String.valueOf(AppSettings.get().bedPrintTemp(filament)));
			tfBedTempAtLayer.setText(String.valueOf(AppSettings.get().bedTempAtLayer(filament)));
			tfLayerForNewBedTemp.setText(String.valueOf(AppSettings.get().layerForNewBedTemp(filament)));
			tfFinalBedTempForFade.setText(String.valueOf(AppSettings.get().finalBedTempForFade(filament)));
			tfTimeForBedTempFade.setText(String.valueOf(AppSettings.get().timeForBedTempFade(filament)));
			GCode.loadGCode(filament);
		});
		cbFilament.setValue(filament);
		cbFilament.setDisable(lockFilamentChoice);
		btnProcess.setOnAction(e -> {
			boolean homeHotEnd           = cbHomeHotEnd.isSelected();
			boolean addEndGCode          = cbAddEndGCode.isSelected();
			boolean holdHotEnd           = cbHoldHotEnd.isSelected();
			boolean loadBedMesh          = cbLoadBedMesh.isSelected();
			int     hotEndHoldTemp       = Integer.parseInt(tfHotEndHoldTemp.getText());
			int     hotEndPrintTemp      = Integer.parseInt(tfHotEndPrintTemp.getText());
			int     bedHoldTemp          = Integer.parseInt(tfBedHoldTemp.getText());
			int     bedPrintTemp         = Integer.parseInt(tfBedPrintTemp.getText());
			boolean changeTempAtLayer    = cbLayerBedTemp.isSelected();
			int     bedAtLayerTemp       = Integer.parseInt(tfBedTempAtLayer.getText());
			int     layerToChangeBedTemp = Integer.parseInt(tfLayerForNewBedTemp.getText());
			boolean fadeBedTemp          = cbFadeBedTemp.isSelected();
			int     finalFadeTemp        = Integer.parseInt(tfFinalBedTempForFade.getText());
			int     fadeTime             = Integer.parseInt(tfTimeForBedTempFade.getText());

			UserChoices userChoices = new UserChoices(
					homeHotEnd,
					addEndGCode,
					holdHotEnd,
					loadBedMesh,
					hotEndHoldTemp,
					hotEndPrintTemp,
					bedHoldTemp,
					bedPrintTemp,
					changeTempAtLayer,
					bedAtLayerTemp,
					layerToChangeBedTemp,
					fadeBedTemp,
					finalFadeTemp,
					fadeTime);
			new Thread(() -> {
				Platform.runLater(() -> {
					btnProcess.setDisable(true);
					btnCancel.setDisable(true);
					lblProcessing.setText("Processing GCode - could take a little time");
				});
				GCode.startProcessing(userChoices);
			}).start();
			new Timer().scheduleAtFixedRate(new TimerTask() {
				@Override public void run() {
					String process = Tracking.getProcess();
					if (process != null) {
						if (!process.isEmpty()) {
							Platform.runLater(() -> lblProcessing.setText(process));
						}
					}
				}
			}, 2000, 100);
		});
	}

	private Slider getSlider() {
		Slider slider = new Slider();
		slider.setMinWidth(width * .9);
		slider.setMaxWidth(width * .9);
		slider.setPrefWidth(width * .9);
		slider.setMin(0.0);
		slider.setMax(time.getLayerCount());
		int    totalLayers = time.getLayerCount();
		double totalHeight = time.getTotalHeight();
		lblTotalLayers.setText("Layers: " + totalLayers);
		lblTotalHeight.setText("Height: " + padD(totalHeight) + " mm");
		int totalTime = time.getTotalPrintTime();
		slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			int    layerTime     = time.getTotalTimeAtLayer(newValue.intValue());
			int    layer         = newValue.intValue();
			int    layersLeft    = totalLayers - layer;
			double layerHeight   = time.getHeightAtLayer(layer);
			double heightLeft    = totalHeight - layerHeight;
			int    timeRemaining = totalTime - layerTime;
			int    hourIn        = layerTime / 60 / 60;
			layerTime -= hourIn * 60 * 60;
			int minuteIn        = layerTime / 60;
			int hourOut         = timeRemaining / 60 / 60;
			int totalMinutesOut = timeRemaining / 60;
			timeRemaining -= hourOut * 60 * 60;
			int minuteOut = timeRemaining / 60;
			lblCurrentLayer.setText("Layer:  " + layer);
			lblLayersLeft.setText("Remain: " + layersLeft);
			lblLayerHeight.setText("Height: " + padD(layerHeight) + " mm");
			lblHeightLeft.setText("Remain: " + padD(heightLeft) + " mm");
			lblLayerTime.setText("Time   (hh:mm): " + pad(hourIn) + ":" + pad(minuteIn));
			lblLayerTimeLeft.setText("Remain (hh:mm): " + pad(hourOut) + ":" + pad(minuteOut) + " (minutes: " + totalMinutesOut + ")");
		});
		return slider;
	}

	private void updateSummary() {
		String filename = "";
		if (AppSettings.gcodePathNull()) {
			if (System.getenv("SLIC3R_PP_OUTPUT_NAME") != null) {
				Path rootFile = Paths.get(System.getenv("SLIC3R_PP_OUTPUT_NAME"));
				filename = rootFile.getFileName().toString();

			}
			else {
				filename = AppSettings.getFilename();
			}
		}
		lblMode.setText(AppSettings.gcodePathNull() ? "No GCode Processing (Test Mode)" : "Live - GCode will be processed (" + filename + ")");
		lblMode.setStyle(AppSettings.gcodePathNull() ? "-fx-text-fill:RED" : "-fx-text-fill:GREEN");
		int printTime = time.getTotalPrintTime();
		int hours = printTime / 60 / 60;
		printTime -= (hours * 60 * 60);
		int minutes = printTime / 60;
		lblSummary.setText("Print Time (hh:mm): " + pad(hours) + ":" + pad(minutes) + " / Layers: " + GCode.getLastLayerNumber() + " / Height: " + padD(time.getTotalHeight()) + " mm");
	}

	private String padD(double number) {
		return String.format("%.2f", number);
	}

	private String pad(int number) {
		String response = String.valueOf(number);
		if (number < 10) {
			return "0" + response;
		}
		return response;
	}

	private void cancel() {
		System.err.println("User canceled gcode export.");
		System.exit(0);
	}

	private Path loadGCodeFile() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(AppSettings.get().lastGcodeFolder());
		fc.setTitle("Load GCode File");
		File gcodeFile = fc.showOpenDialog(null);
		if (gcodeFile != null) {
			AppSettings.set().lastGcodeFolder(gcodeFile.getParent());
			return gcodeFile.toPath();
		}
		return null;
	}

	private CHBox newHBox(Node... nodes) {
		return new CHBox.Builder(nodes).spacing(5).padding(15, 0, 0, 0).alignment(Pos.BASELINE_LEFT).build();
	}

	private CHBox newHBox(double height, Node... nodes) {
		return new CHBox.Builder(nodes).height(height).spacing(0).padding(0, 0, 0, 0).alignment(Pos.BASELINE_LEFT).build();
	}
}
