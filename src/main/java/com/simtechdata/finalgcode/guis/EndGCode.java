package com.simtechdata.finalgcode.guis;

import com.simtechdata.easyfxcontrols.containers.CHBox;
import com.simtechdata.easyfxcontrols.containers.CVBox;
import com.simtechdata.easyfxcontrols.controls.Button;
import com.simtechdata.easyfxcontrols.controls.CTextArea;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

public class EndGCode {


	private final String         sceneId = SceneOne.randomSceneId();
	private final StringProperty gcode   = new SimpleStringProperty();

	private final double width  = SceneOne.getScreenWidth() * .4;
	private final double height = SceneOne.getScreenHeight() * .4;

	public void editGCode() {
		Font monaco = AppSettings.getFontMonaco(12);
		String    title = "End Gcode";
		CTextArea ta    = new CTextArea.Builder().size(width - 50, height - 115).build();
		ta.textProperty().bindBidirectional(gcode);
		ta.setFont(monaco);
		gcode.setValue(AppSettings.get().endGCode());
		Button btnSave    = new Button.Builder("Save").width(65).onAction(e -> save()).build();
		Button btnCancel  = new Button.Builder("Cancel").width(65).onAction(e -> close()).build();
		CHBox  boxButtons = new CHBox.Builder(20, btnSave, btnCancel).alignment(Pos.CENTER).build();
		CVBox  vbox       = new CVBox.Builder(20, ta, boxButtons).height(height).padding(20).build();
		SceneOne.set(sceneId, vbox, width, height).newStage().centered().alwaysOnTop().title(title).showAndWait();
	}

	private void close() {
		SceneOne.close(sceneId);
	}

	private void save() {
		AppSettings.set().endGCode(gcode.getValue());
		close();
	}
}
