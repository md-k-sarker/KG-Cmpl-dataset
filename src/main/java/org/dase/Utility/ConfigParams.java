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
    public static String confFilePath;
    public static String ontoPath;
    public static String logPath;
    public static String dllearnerResultPath;
    public static String miniDlLearnerResultPath;
    public static String posIndiPath;
    public static String negIndiPath;
    public static String namespace;
    public static Double tolerance;
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

        confFilePath = prop.getProperty("path.confFilePath");
        ontoPath = prop.getProperty("path.inputOntology");
        String[] inpPaths = ontoPath.split(File.separator);
        String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
        logPath = prop.getProperty("path.outputLogPath") +"_log" + name;
        dllearnerResultPath = prop.getProperty("path.dllearnerResultPath") +"_result_by_dllearnerResultPath_" + name;
        miniDlLearnerResultPath = prop.getProperty("path.miniDlLearnerResultPath") +"_result_by_miniDlLearnerResultPath_" + name;
        posIndiPath = prop.getProperty("path.posImages");
        negIndiPath = prop.getProperty("path.negImages");
        namespace = prop.getProperty("namespace");
        tolerance = Double.valueOf(prop.getProperty("tolerance"));
        invalidTriplesNeeded = Integer.valueOf(prop.getProperty("invalidTriplesNeeded"));
        randomSeed = Integer.valueOf(prop.getProperty("randomSeed"));
        debug = Boolean.parseBoolean(prop.getProperty("debug"));

    }

    // private constructor, no instantiation
    private ConfigParams() {

    }

}
