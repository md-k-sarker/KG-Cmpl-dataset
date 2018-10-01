package org.dase.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public final class ConfigParams {

    private static String configFileName = "config.properties";

    private static Properties prop;
    private static InputStream input;

    // properties needed
    public static String logPath;
    public static String inputOntoRootPath;
    public static String inputOntoPath;
    public static String inputOntoFileNameWithoutExtention;
    public static String outputJsonPath;
    public static String namespace;
    public static int noOfBaseTriples;
    public static int noOfinvalidTriplesNeeded;
    public static int randomSeed;
    public static boolean debug;
    public static boolean batchRun;
    public static String inputOntoFileExtension;
    //public static String validExtentions;


    /**
     * Initiate configParams
     */
    public static void init() {
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
            System.out.println("\t" + k + ": " + v);
        });

        namespace = prop.getProperty("namespace");

        noOfBaseTriples = Integer.valueOf(prop.getProperty("noOfBaseTriples"));
        noOfinvalidTriplesNeeded = Integer.valueOf(prop.getProperty("noOfinvalidTriplesNeeded"));
        randomSeed = Integer.valueOf(prop.getProperty("randomSeed"));
        debug = Boolean.parseBoolean(prop.getProperty("debug"));
        batchRun = Boolean.parseBoolean(prop.getProperty("batchRun"));

        generateLogPath();

        //validExtentions = prop.getProperty("validExtentions");

        if (!batchRun) {
            inputOntoPath = prop.getProperty("file.inputOntology");
            int i = inputOntoPath.lastIndexOf('.');
            if (i > 0) {
                inputOntoFileExtension = inputOntoPath.substring(i + 1);
            }
            inputOntoFileNameWithoutExtention = Paths.get(inputOntoPath).getFileName().toString().replaceFirst("[.][^.]+$", "");
            generateOutputPath();
        } else {
            inputOntoRootPath = prop.getProperty("file.inputOntology");
        }
    }

    public static void setInputOntoPath(String _inputOntoPath) {
        inputOntoPath = _inputOntoPath;
        inputOntoFileNameWithoutExtention = Paths.get(inputOntoPath).getFileName().toString().replaceFirst("[.][^.]+$", "");
    }

    public static void generateOutputPath() {

        String name = Paths.get(inputOntoPath).getFileName().toString().
                replace(".rdf", ".json");
        outputJsonPath = prop.getProperty("path.outputJson") + name;
    }

    public static void generateLogPath() {
        logPath = prop.getProperty("path.outputLogPath") + "log.log";
    }

    // private constructor, no instantiation
    private ConfigParams() {

    }

}
