package org.dase.IR;

import java.util.*;
import java.io.*;

public final class ConfigParams {

	private static String configFileName = "config.properties";

	private static Properties prop;
	private static InputStream input;

	// properties needed
	public static String ontoPath;
	public static String posIndiPath;
	public static String negIndiPath;
	public static String namespace;
	public static Double tolerance;

	static {
		prop = new Properties();

		input = ConfigParams.class.getClassLoader().getResourceAsStream(configFileName);
		if (input == null) {
			System.out.print("Error reading config file");
			System.exit(-1);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Printing config properties: ");
		// print proeprty values
		prop.forEach((k, v) -> {
			System.out.println(k + ": " + v);
		});

		ontoPath = prop.getProperty("path.inputOntology");
		posIndiPath = prop.getProperty("path.posImages");
		negIndiPath = prop.getProperty("path.negImages");
		namespace = prop.getProperty("namespace");
		tolerance = Double.valueOf(prop.getProperty("tolerance"));

	}

	// private constructor, no instantiation
	private ConfigParams() {

	}

}
