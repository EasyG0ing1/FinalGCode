package com.simtechdata.finalgcode.guis;

import com.simtechdata.easyfxcontrols.containers.CHBox;
import com.simtechdata.easyfxcontrols.containers.CVBox;
import com.simtechdata.easyfxcontrols.controls.Button;
import com.simtechdata.easyfxcontrols.controls.CCheckBox;
import com.simtechdata.easyfxcontrols.controls.CLabel;
import com.simtechdata.easyfxcontrols.controls.CTextField;
import com.simtechdata.finalgcode.processing.GCode;
import com.simtechdata.finalgcode.processing.ZHopping;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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
				.show();
		tfZHopDistance.requestFocus();
	}

	private       String sceneId = SceneOne.randomSceneId();
	private final double width   = 530;
	private final double base    = 275;
	private       double height;
	private       CVBox  vbox;

	private Text       txtCurrentZHops;
	private CLabel     lblDiagonal;
	private CCheckBox  cbDiagonal;
	private CLabel     lblTraditional;
	private CCheckBox  cbTraditional;
	private CLabel     lblAlternate;
	private CCheckBox  cbAlternate;
	private CLabel     lblMinDistance;
	private CTextField tfMinDistance;
	private CLabel     lblZHopDistance;
	private CLabel     lblStartLayer;
	private CLabel     lblEndLayer;
	private CTextField tfZHopDistance;
	private CTextField tfStartLayer;
	private CTextField tfEndLayer;
	private Button     btnAdd;
	private Button     btnCancel;
	private Button     btnClear;
	private Image      icon;
	private ImageView  ivInfo;
	private boolean    addedDistance = false;
	private boolean    addedStart    = false;

	private void makeControls() {
		String  currentZHops = ZHopping.getInstance().getZHopsString();
		boolean haveZHops    = !currentZHops.isEmpty();
		int     lines        = currentZHops.split("\\n").length;
		height          = haveZHops ? base + (lines * 15) : base;
		txtCurrentZHops = new Text(currentZHops);
		lblDiagonal     = new CLabel.Builder("Diagonal ZHop").width(85).build();
		cbDiagonal      = new CCheckBox.Builder().toolTip("Click on the question mark for\nmore information about diagonal ZHop").build();
		lblTraditional  = new CLabel.Builder("Traditional").visibleBinding(cbDiagonal.selectedProperty()).width(85).build();
		cbTraditional   = new CCheckBox.Builder().visibleBinding(cbDiagonal.selectedProperty()).selected(true).build();
		lblAlternate    = new CLabel.Builder("Alternate").width(85).visibleBinding(cbDiagonal.selectedProperty()).build();
		cbAlternate     = new CCheckBox.Builder().visibleBinding(cbDiagonal.selectedProperty()).selected(false).build();
		lblMinDistance  = new CLabel.Builder("Minimum Distance").width(140).visibleBinding(cbDiagonal.selectedProperty()).build();
		tfMinDistance   = new CTextField.Builder("2").width(140).visibleBinding(cbDiagonal.selectedProperty()).numbersWithDecimals().build();
		icon            = new Image(AppSettings.getInfoIcon());
		ivInfo          = new ImageView(icon);
		ivInfo.setFitWidth(35);
		ivInfo.setPreserveRatio(true);
		ivInfo.setOnMouseClicked(e->new DiagonalInfo());


		lblZHopDistance = new CLabel.Builder("ZHop Distance").width(150).alignment(Pos.BASELINE_CENTER).build();
		lblStartLayer   = new CLabel.Builder("Start Layer (first layer is always 0)").width(150).wordWrap(true).alignment(Pos.BASELINE_CENTER).build();
		lblEndLayer     = new CLabel.Builder("End Layer (Last layer number is " + GCode.getLastLayerNumber() + ")").width(150).wordWrap(true).alignment(Pos.BASELINE_CENTER).build();
		tfZHopDistance  = new CTextField.Builder().width(150).numbersWithDecimals().build();
		tfStartLayer    = new CTextField.Builder().width(150).numbersWithoutDecimals().build();
		tfEndLayer      = new CTextField.Builder().width(150).numbersWithoutDecimals().build();
		btnAdd          = new Button.Builder().text("Add").width(55).build();
		btnCancel       = new Button.Builder().text("Close").width(85).build();
		btnClear        = new Button.Builder().text("Clear All ZHops").width(115).build();

		CVBox vbDiagInfo    = new CVBox.Builder(5, ivInfo).alignment(Pos.BOTTOM_LEFT).padding(new Insets(0,15,0,0)).build();
		CVBox vbDiagonal    = new CVBox.Builder(5, lblDiagonal, cbDiagonal).alignment(Pos.CENTER).build();
		CVBox vbTraditional = new CVBox.Builder(5, lblTraditional, cbTraditional).alignment(Pos.CENTER).build();
		CVBox vbAlternate   = new CVBox.Builder(5, lblAlternate, cbAlternate).alignment(Pos.CENTER).build();
		CVBox vbMinDistance = new CVBox.Builder(5, lblMinDistance, tfMinDistance).alignment(Pos.CENTER).build();

		CHBox boxDiagonal   = new CHBox.Builder(vbDiagInfo, vbDiagonal, vbTraditional, vbAlternate, vbMinDistance).alignment(Pos.CENTER).build();
		CHBox boxCurrent    = new CHBox.Builder(txtCurrentZHops).alignment(Pos.CENTER).build();
		CHBox boxLabels     = new CHBox.Builder(15, lblZHopDistance, lblStartLayer, lblEndLayer).build();
		CHBox boxTextFields = new CHBox.Builder(15, tfZHopDistance, tfStartLayer, tfEndLayer).build();
		CHBox boxButtons    = new CHBox.Builder(15, btnAdd, btnCancel, btnClear).alignment(Pos.CENTER).build();

		if (haveZHops) {vbox = new CVBox.Builder(10, boxCurrent, boxDiagonal, boxLabels, boxTextFields, boxButtons).padding(20).alignment(Pos.CENTER).build();}
		else {vbox = new CVBox.Builder(10, boxDiagonal, boxLabels, boxTextFields, boxButtons).padding(new Insets(0, 0, 0, 15)).alignment(Pos.CENTER).build();}
	}

	private void setControlActions() {
		Font monaco = AppSettings.getFontMonaco(11.5);
		tfZHopDistance.textProperty().addListener((ob, ov, nv) -> addedDistance = nv.matches("[0-9]+") ||
																			  nv.matches("[0-9]+\\.[0-9]+") ||
																			  nv.matches("\\.[0-9]+"));

		tfStartLayer.textProperty().addListener((ob, ov, nv) -> addedStart = nv.matches("[0-9]+"));
		tfEndLayer.textProperty().addListener((ob, ov, nv) -> {});
		tfMinDistance.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			boolean esc = event.getCode().equals(KeyCode.ESCAPE);
			if (tab || enter) {
				tfZHopDistance.requestFocus();
			}
			if (esc)
				close();
		});
		tfZHopDistance.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			boolean esc = event.getCode().equals(KeyCode.ESCAPE);
			if (tab || enter) {
				tfStartLayer.requestFocus();
			}
			if (esc)
				close();
		});
		tfStartLayer.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			boolean esc = event.getCode().equals(KeyCode.ESCAPE);
			if (tab || enter) {
				tfEndLayer.requestFocus();
			}
			if (esc)
				close();
		});
		tfEndLayer.setOnKeyPressed(event -> {
			boolean tab   = event.getCode().equals(KeyCode.TAB);
			boolean enter = event.getCode().equals(KeyCode.ENTER);
			boolean esc = event.getCode().equals(KeyCode.ESCAPE);
			if (tab || enter) {
				processEnd(tab);
			}
			if (esc)
				close();
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
		cbAlternate.selectedProperty().addListener((ob, ov, nv) -> cbTraditional.setSelected(ov));
		cbTraditional.selectedProperty().addListener((ob, ov, nv) -> cbAlternate.setSelected(ov));
		addedDistance = false;
		addedStart    = false;
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
			return;
		}

		if (end > lastLayerNumber) {
			SceneOne.showMessage(500, 140, "There are only " + lastLayerNumber + " layers in this print. Your end layer value of " + end + " is higher than the number of layers in this print. Please chose a different ending layer number or leave that field blank to automatically set end to last layer.", true, Pos.CENTER_LEFT);
			tfEndLayer.clear();
			tfEndLayer.requestFocus();
			return;
		}

		boolean added;
		boolean useDiagonal = cbDiagonal.isSelected();
		boolean alternate = cbAlternate.isSelected();
		double minDistance = tfMinDistance.getText().isEmpty() ? 0 : Double.parseDouble(tfMinDistance.getText());

		if(useDiagonal)
			added = ZHopping.getInstance().insertZHop(height, start, end, true, alternate, minDistance);
		else
			added = ZHopping.getInstance().insertZHop(height, start, end);

		if (!added) {
			SceneOne.showMessage(450, 140, "Either your start layer (" + start + ") or end layer (" + end + ") or any layer in between already exists in a set you have previously added. Please chose different layers for a new set", true, Pos.CENTER_LEFT);
			tfStartLayer.clear();
			tfEndLayer.clear();
			tfStartLayer.requestFocus();
			addedStart = false;
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
				.show();
		tfZHopDistance.requestFocus();
	}

	private void clearZHops() {
		ZHopping.getInstance().clearZHops();
		resetForm();
	}

	private void close() {
		SceneOne.close(sceneId);
	}
}
