package com.simtechdata.finalgcode.processing.structure;

public class Line {
	public Line(String line, String g, String x, String y, String z, String e, String f) {
		G          = g;
		X          = x;
		Y          = y;
		Z          = z;
		E          = e;
		F          = f;
		this.line  = line;
		isMoveLine = (!X.isEmpty() || !Y.isEmpty()) && E.isEmpty() && Z.isEmpty();
		if (!E.isEmpty()) {
			String num    = E.replace("E", "");
			double eValue = Double.parseDouble(num);
			isExtrudingLine = (eValue > 0) && (!X.isEmpty() || !Y.isEmpty());
		}
		primeRetract = line.startsWith("G1 E") || line.startsWith("G0 E");
		hasZ         = (G.contains("G1") || G.contains("G0")) && !Z.isEmpty();
		blankMove    = (G.contains("G1") || G.contains("G0")) && (!X.isEmpty() || !Y.isEmpty()) && Z.isEmpty() && E.isEmpty();
	}

	public Line(String line) {
		this.line = line;
	}

	public String  G               = "";
	public String  X               = "";
	public String  Y               = "";
	public String  Z               = "";
	public String  E               = "";
	public String  F               = "";
	public String  line;
	public boolean isMoveLine      = false;
	public boolean isExtrudingLine = false;
	public boolean primeRetract    = false;
	public boolean hasZ            = false;
	public boolean blankMove       = false;

	public Double xV() {
		if (!X.isEmpty()) {
			String num = X.replace("X", "");
			return Double.parseDouble(num);
		}
		return null;
	}

	public Double yV() {
		if (!Y.isEmpty()) {
			String num = Y.replace("Y", "");
			return Double.parseDouble(num);
		}
		return null;
	}

}
