module com.simtechdata {
    requires javafx.controls;
	requires java.datatransfer;
	requires org.apache.commons.io;
	requires org.apache.commons.collections4;
	requires java.desktop;
	requires javafx.webEmpty;
	requires javafx.graphics;
	requires com.simtechdata.sceneonefx;
	requires com.simtechdata.easyfxcontrols;
	requires java.prefs;
	requires io.github.classgraph;
	requires org.apache.commons.lang3;

	exports com.simtechdata.finalgcode;
 	exports com.simtechdata.finalgcode.enums;
 	exports com.simtechdata.finalgcode.guis;
 	exports com.simtechdata.finalgcode.processing;
 	exports com.simtechdata.finalgcode.processing.structure;
 	exports com.simtechdata.finalgcode.settings;
}
