package com.simtechdata.finalgcode.processing;

public class Tracking {

	private Tracking() {

	}

	private static String process;

	public static void setProcess(String process) {
		Tracking.process = process;
	}

	public static String getProcess() {
		return process;
	}

}
