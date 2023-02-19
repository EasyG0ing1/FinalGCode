package com.simtechdata.finalgcode.guis;

import com.simtechdata.easyfxcontrols.containers.CVBox;
import com.simtechdata.easyfxcontrols.controls.CButton;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import javafx.geometry.Pos;
import javafx.scene.text.Text;

public class DiagonalInfo {

	public DiagonalInfo() {
		setControls();
		SceneOne.set(sceneId, vbox, 500, 600).centered().alwaysOnTop().newStage().showAndWait();
	}

	private final String sceneId = SceneOne.getRandomSceneId();
	private final Text   txtInfo = new Text(getInfo());
	private final CButton btnOK = new CButton.Builder("OK").onAction(e -> SceneOne.close(sceneId)).width(55).build();
	private final CVBox  vbox  = new CVBox.Builder(txtInfo, btnOK).alignment(Pos.CENTER).build();

	private void setControls() {
		txtInfo.setFont(AppSettings.getFontMonaco(12.5));
		txtInfo.setWrappingWidth(450);
	}
	private String getInfo() {
		return """
				Diagonal ZHop can be used in situations where you have a difficult print and you have a lot of small pillars or other smaller elevating parts of the model where ZHopping alone doesn't quite yield the results you need.
				    
				The way it works is it will ZHop the print head but while it's moving to a different portion of the model within the same layer. And while it's moving to the next extrusion point in that layer, it will gradually climb vertically till it gets half way there then it will gradually descend until it reaches the extrusion point. This can help offset stringing in the print.
				    
				When you use diagonal ZHopping, there are some other options that you need to decide on:
				    
				Traditional is when the behavior is exactly as described above.
				    
				Alternate is when the print head first does the total height Z-Lift before it starts moving to the next extrusion point, but then gradually lowers until it reaches the layer height at the next extrusion point. This can be helpful if the traditional method didn't reduce stringing enough.
				    
				Minimum Distance is there so that you can decide on how long of a gap must exist before actually doing diagonal ZHop. It can sometimes be useless when the gap between points is very small so it might make sense to only implement diagonal ZHop when the gap is more than say 2mm or so.
				""";
	}
}
