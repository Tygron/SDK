package com.tygron.pub.logger;

public class Log {

	private static boolean verbose = false;

	public static void setVerbose(boolean verbose) {
		Log.verbose = verbose;
	}

	private static void out(String message) {
		System.out.println(message);
	}

	private static void err(String message) {
		System.err.println(message);
	}

	public static void info(String message) {
		out(message);
	}

	public static void exception(Throwable t, String message) {
		err(message);
		t.printStackTrace();
	}

	public static void verbose(String message) {
		if (verbose) out(message);
	}
}
