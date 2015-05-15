package com.tygron.pub.logger;

public class Log {

	private static boolean verbose = false;

	private static void err(String message) {
		System.err.println(message);
	}

	public static void exception(Throwable t, String message) {
		err(message);
		t.printStackTrace();
	}

	public static void info(String message) {
		out(message);
	}

	private static void out(String message) {
		System.out.println(message);
	}

	public static void setVerbose(boolean verbose) {
		Log.verbose = verbose;
	}

	public static void verbose(String message) {
		if (verbose) {
			out(message);
		}
	}

	public static void warning(String message) {
		err(message);
	}
}
