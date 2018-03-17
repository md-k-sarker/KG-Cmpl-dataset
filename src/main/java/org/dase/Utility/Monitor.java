package org.dase.Utility;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Monitor {
	private long startTime;

	private final PrintStream out;

	// stop initializing
	public Monitor(PrintStream _printStream) {
		this.out = _printStream;
	}

	/*
	 * http://slf4j.42922.n3.nabble.com/Logging-to-file-with-slf4j-Logger-Where-do
	 * -log-file-go-td46087.html It appears that you have misunderstood the purpose
	 * of SLF4J. If you place slf4j-jdk14-1.5.6.jar then slf4j-api will bind with
	 * java.util.logging. Logback will not be used. Only if you place
	 * logback-core.jar and logback-classic.jar on your class path (but not
	 * slf4j-jdk14-1.5.6.jar) will SLF4J API bind with logback. SLF4J binds with one
	 * and only one underlying logging API (per JVM launch).
	 */
	// public static void safeExit(ErrorCodes.Error errorCode) {
	// stop("Error occurred: Safe Exiting...");
	// }

	public void displayMessage(String message, boolean write) {
		System.out.println(message);
		if (write) {
			out.println(message);
		}
	}

	public void error(String message) {
		System.err.println(message);
	}

	public void start(String message, boolean write) {
		if (message.trim() != "") {
			displayMessage(message, write);
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		startTime = System.currentTimeMillis();
		displayMessage("Start Time: " + dateFormat.format(date), write);
	}

	public void displayMessageWithTime(String message, boolean write) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		Long end = System.currentTimeMillis();
		if (message.trim() != "") {
			displayMessage(message, write);
		}
		displayMessage(" Time Now:" + dateFormat.format(date), write);
		displayMessage("Duration from startTime:" + (end - startTime) / 1000 + " sec", write);
	}

	public void stop(String message, boolean write) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		Long end = System.currentTimeMillis();
		if (message.trim() != "") {
			displayMessage(message, write);
		}
		displayMessage("End Time:" + dateFormat.format(date), write);
		displayMessage("Duration:" + (end - startTime) / 1000 + " sec", write);
	}

	public void stopSystem(String message, boolean write) {
		stop(message, write);
		System.exit(1);
	}
}