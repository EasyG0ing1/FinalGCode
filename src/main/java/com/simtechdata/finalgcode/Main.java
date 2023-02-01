package com.simtechdata.finalgcode;

import com.simtechdata.finalgcode.guis.GUI;
import com.simtechdata.finalgcode.settings.AppSettings;
import com.simtechdata.sceneonefx.SceneOne;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

import static com.simtechdata.finalgcode.enums.OS.MAC;

public class Main extends Application {

	private static final String     dockIconBase = "icons/FinalGCodeIcon.png";
	private static final String     fontMonaco   = "fonts/Monaco.ttf";
	private static final String     iconInfo     = "icons/InfoIcon.png";
	private static final JFrame     jFrame       = new JFrame();
	private final        ScanResult scanResult   = new ClassGraph().enableAllInfo().scan();
	private              Taskbar    taskbar      = null;
	private              Path       appFolder;
	private              Path       appIcon      = null;
	private              Path       infoIcon     = null;
	private              Path       monacoFont   = null;
	private static       Path       gcodePath    = null;
	private static       String[]   args;

	public static void main(String[] args) {
		Main.args = args;
		launch(args);
	}

	private void copyResources() {
		appFolder  = AppSettings.getAppFolder();
		appIcon    = Paths.get(appFolder.toString(), "FinalGCodeIcon.png");
		infoIcon   = Paths.get(appFolder.toString(), "InfoIcon.png");
		monacoFont = Paths.get(appFolder.toString(), "Monaco.ttf");
		try {
			if (!appFolder.toFile().exists()) {
				FileUtils.createParentDirectories(appIcon.toFile());
			}
			ResourceList resources = scanResult.getAllResources();
			URL          url       = null;
			for (URL u : resources.getURLs()) {
				if (u.toString().contains(dockIconBase)) {
					if (!appIcon.toFile().exists()) {
						FileUtils.copyURLToFile(u, appIcon.toFile());
					}
				}
				else if (u.toString().contains(fontMonaco)) {
					if (!monacoFont.toFile().exists()) {
						FileUtils.copyURLToFile(u, monacoFont.toFile());
					}
				}
				else if (u.toString().contains(iconInfo)) {
					if (!infoIcon.toFile().exists()) {
						FileUtils.copyURLToFile(u, infoIcon.toFile());
					}
				}
			}
			if (url != null) {FileUtils.copyURLToFile(url, appIcon.toFile());}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setTaskbarDockIcon() {
		try {
			Image image = ImageIO.read(appIcon.toFile());
			taskbar = Taskbar.getTaskbar();
			if (AppSettings.getOS().equals(MAC)) {
				taskbar.setIconImage(image);
			}
			else {
				jFrame.setUndecorated(true);
				jFrame.setIconImage(image);
				jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);
				jFrame.pack();
				jFrame.setVisible(true);
				jFrame.setSize(new Dimension(26, 26));
			}
		}
		catch (UnsupportedOperationException e) {
			System.out.println("This os does not support taskbar.setIconImage()");
		}
		catch (final SecurityException e) {
			System.out.println("There was a security exception for: 'taskbar.setIconImage'");
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void getEnvironmentVariables() {
		Map<String, String> map = System.getenv();
		StringBuilder sb = new StringBuilder("Name,Value\n");
		int x = 1;
		for(String arg: args) {
			sb.append("COMMAND_LINE_ARGUMENT_").append(x).append(",").append(arg).append("\n");
			x++;
		}
		LinkedList<String> indexes = new LinkedList<>(map.keySet());
		indexes.sort(Comparator.comparing(String::toString));
		for(String name : indexes) {
			String value = map.get(name);
			sb.append(name).append(",").append(value).append("\n");
		}
		try{
			File file = new File(System.getProperty("user.home"),"PrusaSlicerEnvironmentVariables.csv");
			FileUtils.writeStringToFile(file, sb.toString(), Charset.defaultCharset());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public void start(Stage primaryStage) {
		boolean logging = false;
		if(args[0] != null) {
			if(!args[0].equalsIgnoreCase("log")) {
				gcodePath = Paths.get(args[0]);
				if (!gcodePath.toFile().exists()) {
					SceneOne.showMessage(500, 125, "GCode File does not exist:\n" + args[0], true, Pos.CENTER_LEFT);
					System.exit(1);
				}
			}
		}
		for (String arg : args) {
			if(arg.equalsIgnoreCase("log"))
				logging = true;
		}

		AppSettings.set().logging(logging);
		copyResources();
		setTaskbarDockIcon();
		new GUI(gcodePath);
	}
}
