package com.simtechdata.finalgcode.guis;

import com.simtechdata.easyfxcontrols.containers.CHBox;
import com.simtechdata.easyfxcontrols.containers.CVBox;
import com.simtechdata.easyfxcontrols.controls.Button;
import com.simtechdata.easyfxcontrols.controls.CLabel;
import com.simtechdata.easyfxcontrols.controls.CTextField;
import com.simtechdata.finalgcode.processing.GCode;
import com.simtechdata.finalgcode.processing.ZHopping;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZHop {

	public ZHop() {
		makeControls();
		setControlActions();
		SceneOne.set(sceneId, vbox, width, height)
				.centered()
				.newStage()
				.alwaysOnTop()
				.onKeyPressed(event -> {
					if (event.getCode().equals(KeyCode.ESCAPE)) {
						close();
					}
				})
				.onCloseEvent(e -> close())
				.showAndWait();
	}

	private       String sceneId = SceneOne.randomSceneId();
	private final double width   = 530;
	private final double base    = 210;
	private       double height;
	private       CVBox  vbox;

	private       Text       txtCurrentZHops;
	private       CLabel     lblZHopDistance;
	private       CLabel     lblStartLayer;
	private       CLabel     lblEndLayer;
	private       CTextField tfZHopDistance;
	private       CTextField tfStartLayer;
	private       CTextField tfEndLayer;
	private       Button     btnAdd;
	private       Button     btnCancel;
	private       Button     btnClear;
	private       boolean    addedDistance = false;
	private       boolean    addedStart    = false;
	private       boolean    addedEnd      = false;

	private void makeControls() {
		String  currentZHops = ZHopping.getZHops();
		boolean haveZHops    = !currentZHops.isEmpty();
		int     lines        = currentZHops.split("\\n").length;
		height          = haveZHops ? base + (lines * 15) : 165;
		txtCurrentZHops = new Text(currentZHops);
		lblZHopDistance = new CLabel.Builder("ZHop Distance").width(150).alignment(Pos.BASELINE_CENTER).build();
		lblStartLayer   = new CLabel.Builder("Start Layer (first layer is always 0)").width(150).wordWrap(true).alignment(Pos.BASELINE_CENTER).build();
		lblEndLayer     = new CLabel.Builder("End Layer (Last layer number is "+GCode.getLastLayerNumber()+")").width(150).wordWrap(true).alignment(Pos.BASELINE_CENTER).build();
		tfZHopDistance  = new CTextField.Builder().width(150).build();
		tfStartLayer    = new CTextField.Builder().width(150).build();
		tfEndLayer      = new CTextField.Builder().width(150).build();
		btnAdd          = new Button.Builder().text("Add").width(55).build();
		btnCancel       = new Button.Builder().text("Close").width(85).build();
		btnClear        = new Button.Builder().text("Clear All ZHops").width(115).build();
		CHBox boxCurrent    = new CHBox.Builder(txtCurrentZHops).alignment(Pos.CENTER).build();
		CHBox boxLabels     = new CHBox.Builder(15, lblZHopDistance, lblStartLayer, lblEndLayer).build();
		CHBox boxTextFields = new CHBox.Builder(15, tfZHopDistance, tfStartLayer, tfEndLayer).build();
		CHBox boxButtons    = new CHBox.Builder(15, btnAdd, btnCancel, btnClear).alignment(Pos.CENTER).build();
		if (haveZHops) {vbox = new CVBox.Builder(10, boxCurrent, boxLabels, boxTextFields, boxButtons).padding(20).alignment(Pos.CENTER).build();}
		else {vbox = new CVBox.Builder(10, boxLabels, boxTextFields, boxButtons).padding(new Insets(0, 0, 0, 15)).alignment(Pos.CENTER).build();}
	}


	private void setControlActions() {
		Font monaco = AppSettings.getFontMonaco(11.5);
		tfEndLayer.focusedProperty().addListener((ob, ov, nv) -> {
			if (nv) {addedEnd = true;}
		});
		tfZHopDistance.textProperty().addListener((observable, oldValue, newValue) -> {
			tfZHopDistance.setText(tfZHopDistance.getText().replaceAll("[^0-9.]", ""));
			String t = tfZHopDistance.getText();
			addedDistance = t.matches("[0-9]+") ||
							t.matches("[0-9]+\\.[0-9]+") ||
							t.matches("\\.[0-9]+");
		});
		tfStartLayer.textProperty().addListener((observable, oldValue, newValue) -> {
			tfStartLayer.setText(tfStartLayer.getText().replaceAll("[^0-9]", ""));
			addedStart = tfStartLayer.getText().matches("[0-9]+");
		});
		tfEndLayer.textProperty().addListener((observable, oldValue, newValue) -> tfEndLayer.setText(tfEndLayer.getText().replaceAll("[^0-9]", "")));
		tfZHopDistance.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			if (tab || enter) {
				tfStartLayer.requestFocus();
			}
		});
		tfStartLayer.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			if (tab || enter) {
				tfEndLayer.requestFocus();
			}
		});
		tfEndLayer.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			if (tab || enter) {
				processEnd(tab);
			}
		});
		btnAdd.setOnKeyPressed(event -> {
			boolean tab = event.getCode().equals(KeyCode.TAB);
			if (tab) {
				btnCancel.requestFocus();
			}
		});
		btnCancel.setOnKeyPressed(event -> {
			boolean tab = event.getCode().equals(KeyCode.TAB);
			if (tab) {
				tfZHopDistance.requestFocus();
			}
		});
		btnAdd.setOnAction(e -> addZHop());
		btnClear.setOnAction(e -> clearZHops());
		btnCancel.setOnAction(e -> close());
		txtCurrentZHops.setFont(monaco);
		txtCurrentZHops.setLineSpacing(.65);
		txtCurrentZHops.setTextAlignment(TextAlignment.JUSTIFY);
		addedDistance = false;
		addedStart    = false;
		addedEnd      = false;
	}

	private void processDistance(boolean tab) {
		if (!addedDistance) {tfZHopDistance.requestFocus();}
		else if (!addedStart) {tfStartLayer.requestFocus();}
		else if (!addedEnd) {tfEndLayer.requestFocus();}
		else if (tab) {tfStartLayer.requestFocus();}
		else {addZHop();}
	}

	private void processStart(boolean tab) {
		if (!addedDistance) {tfZHopDistance.requestFocus();}
		else if (!addedStart) {tfStartLayer.requestFocus();}
		else if (!addedEnd || tab) {tfEndLayer.requestFocus();}
		else {addZHop();}
	}

	private void processEnd(boolean tab) {
		if (!addedDistance) {tfZHopDistance.requestFocus();}
		else if (!addedStart) {tfStartLayer.requestFocus();}
		else if (tab) {btnAdd.requestFocus();}
		else {addZHop();}
	}

	private void addZHop() {

		if (!addedDistance) {
			SceneOne.showMessage(300, 125, "Must have a value entered in zHop distance");
			tfZHopDistance.requestFocus();
			return;
		}
		else if (!addedStart) {
			SceneOne.showMessage(300, 125, "Must have a starting layer number");
			tfStartLayer.requestFocus();
			return;
		}

		String heightString = tfZHopDistance.getText();
		String startString  = tfStartLayer.getText();
		String endString    = tfEndLayer.getText();

		double height = Double.parseDouble(heightString);
		if (height == 0) {
			SceneOne.showMessage(300, 125, "zHop distance must be greater than 0");
			tfZHopDistance.requestFocus();
			addedStart = false;
			addedEnd   = false;
			return;
		}

		int start           = Integer.parseInt(startString);
		int end             = endString.isEmpty() ? GCode.getLastLayerNumber() : Integer.parseInt(endString);
		int lastLayerNumber = GCode.getLastLayerNumber();
		if ((end - start) < 0) {
			SceneOne.showMessage(300, 125, "End layer cannot be lower than start layer");
			tfStartLayer.clear();
			tfEndLayer.clear();
			tfStartLayer.requestFocus();
			addedStart = false;
			addedEnd   = false;
			return;
		}

		if (end > lastLayerNumber) {
			SceneOne.showMessage(500, 140, "There are only " + lastLayerNumber + " layers in this print. Your end layer value of " + end + " is higher than the number of layers in this print. Please chose a different ending layer number or leave that field blank to automatically set end to last layer.", true, Pos.CENTER_LEFT);
			tfEndLayer.clear();
			tfEndLayer.requestFocus();
			addedEnd = false;
			return;
		}

		boolean added = ZHopping.insertZHop(height, start, end);

		if (!added) {
			SceneOne.showMessage(450, 140, "Either your start layer (" + start + ") or end layer (" + end + ") or any layer in between already exists in a set you have previously added. Please chose different layers for a new set", true, Pos.CENTER_LEFT);
			tfStartLayer.clear();
			tfEndLayer.clear();
			tfStartLayer.requestFocus();
			addedStart = false;
			addedEnd   = false;
			return;
		}
		resetForm();
	}

	private void resetForm() {
		SceneOne.close(sceneId);
		sceneId = SceneOne.randomSceneId();
		makeControls();
		setControlActions();
		SceneOne.set(sceneId, vbox, width, height)
				.centered()
				.newStage()
				.alwaysOnTop()
				.onKeyPressed(event -> {
					if (event.getCode().equals(KeyCode.ESCAPE)) {
						close();
					}
				})
				.onCloseEvent(e -> close())
				.showAndWait();
	}

	private void clearZHops() {
		ZHopping.clearZHops();
		resetForm();
	}

	private void close() {
		SceneOne.close(sceneId);
	}
}
