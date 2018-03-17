package org.dase.Utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigParams {

    private static String configFileName = "config.properties";

    private static Properties prop;
    private static InputStream input;

    // properties needed
    public static String logPath;
    public static String inputOntoPath;
    public static String outputJsonPath;
    public static String namespace;
    public static int invalidTriplesNeeded;
    public static int randomSeed;
    public static boolean debug;

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

        inputOntoPath = prop.getProperty("file.inputOntology");

        String[] inpPaths = inputOntoPath.split(File.separator);
        String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
        logPath = prop.getProperty("path.outputLogPath") +"_log" + name;

        name = inpPaths[inpPaths.length - 1].replace(".owl", ".json");
        outputJsonPath = prop.getProperty("path.outputJson") + name;

        namespace = prop.getProperty("namespace");

        invalidTriplesNeeded = Integer.valueOf(prop.getProperty("invalidTriplesNeeded"));
        randomSeed = Integer.valueOf(prop.getProperty("randomSeed"));
        debug = Boolean.parseBoolean(prop.getProperty("debug"));

    }

    // private constructor, no instantiation
    private ConfigParams() {

    }

}
