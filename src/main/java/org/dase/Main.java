package org.dase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.dase.IR.InvalidTripleGenerator;
import org.dase.IR.OntologyInferer;
import org.dase.IR.SharedDataHolder;
import org.dase.IR.Statistics;
import org.dase.Utility.ConfigParams;
import org.dase.Utility.JSONMaker;
import org.dase.Utility.Monitor;
import org.dase.Utility.Util;

public class Main {

    private static PrintStream printStream;
    private static Monitor programMonitor;

    static OntModel baseOntModel;
    static OntModel restrictedBaseOntModel;
    static OntModel baseOntModelWithInference;
    //    static OntModel infOntModel;
//    static OntModel invalidInfOntModel;
    static KeySetView<Statement, Boolean> baseStatements;
    static KeySetView<Statement, Boolean> inferredStatements;
//    static KeySetView<Statement, Boolean> invalidinferredStatements;

    static boolean hasMore = false;
    static int SplitIndex = 0;


    private static OntModel loadInput(String inputOntologyFile, Monitor monitor) {

        baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        baseOntModel.setStrictMode(true);

        monitor.displayMessage("Loading input Ontology " + inputOntologyFile + " ........", true);
        baseOntModel.read("file:" + inputOntologyFile);
        monitor.displayMessage("Input Ontology: " + inputOntologyFile + " loaded", true);
        monitor.displayMessage("Profile: " + baseOntModel.getProfile(), true);

        SharedDataHolder.prefixMap = baseOntModel.getNsPrefixMap();
        SharedDataHolder.ontName = baseOntModel.getNsPrefixURI("");

        monitor.displayMessage("Total No of axioms: " + baseOntModel.listStatements().toList().size(), true);


        /**
         * Take around 700 statements only
         */
//        restrictedBaseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
//        restrictedBaseOntModel.setStrictMode(true);
//
//        int counter = 0;
//        ExtendedIterator iterator = baseOntModel.listStatements();
//        while (counter < ConfigParams.noOfBaseTriples) {
//            if (iterator.hasNext()) {
//                restrictedBaseOntModel.add((Statement) iterator.next());
//                counter++;
//            } else {
//                break;
//            }
//        }
//        monitor.displayMessage("Total no. of node/statements: " + restrictedBaseOntModel.getGraph().size(), true);




        return baseOntModel;

    }

    private static ArrayList<OntModel> splitInputOntology(Monitor monitor,OntModel baseOntModel) {
        monitor.displayMessage("Splitting input ontology.... ", true);
        int totalStatements = baseOntModel.listStatements().toList().size();
        ArrayList<Statement> statements = new ArrayList<>();
        statements.addAll(baseOntModel.listStatements().toList());

        ArrayList<OntModel> ontModels = new ArrayList<>();

        int usedStatements = 0;

        while (usedStatements < totalStatements) {

            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
            ontModel.setStrictMode(true);
            int minIndexForLastItem = Math.min(totalStatements - usedStatements, ConfigParams.noOfBaseTriples);
            ontModel.add(statements.subList(usedStatements, usedStatements + minIndexForLastItem));
            usedStatements += minIndexForLastItem;

            ontModels.add(ontModel);
        }
        monitor.displayMessage("Splitted input ontology into "+ontModels.size()+" parts.", true);
        return ontModels;
    }


    private static void doOps(Monitor monitor, String inputOntFullPath) {

        //      String[] inpPaths = inputOntFullPath.split(File.separator);
        //   String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
        // name = inpPaths[inpPaths.length - 1].replace(".owl", ".json");
        //String outputJsonPath = ConfigParams.inputOntoPath + name;
        // monitor.displayMessage("Json writing in: "+outputJsonPath, true);

        baseOntModel = loadInput(inputOntFullPath, monitor);
        ArrayList<OntModel> ontModels = splitInputOntology(monitor,baseOntModel);

        int modelCounter = 0;
        for (OntModel ontModel : ontModels) {
            modelCounter++;

            monitor.displayMessage("\n-------Working with part "+ modelCounter+ " of "+ inputOntFullPath+"----------", true);

            baseOntModelWithInference = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, ontModel);

            OntologyInferer inferer = new OntologyInferer(ontModel, monitor);
            baseStatements = inferer.extractBaseStatements();
            monitor.displayMessage("Inferring statements by rdfs reasoner...", true);
            try {
                inferredStatements = inferer.extractInferredStatements(baseStatements);
            } catch (IOException e) {
                monitor.stopSystem(Util.getStackTraceAsString(e), true);
            }
            monitor.displayMessage("Inferring statements by rdfs reasoner finished.", true);


            Statistics stat = new Statistics(monitor, ontModel, baseStatements, baseOntModelWithInference, inferredStatements);
            monitor.displayMessage("Filling statistics...", true);
            stat.preFillStatistics();
            monitor.displayMessage("Filling statistics finished", true);

            int invalidTriplesNeeded = Math.min(ConfigParams.noOfinvalidTriplesNeeded, SharedDataHolder.baseStatementsAfterReasoning.size());
            invalidTriplesNeeded = Math.min(invalidTriplesNeeded, SharedDataHolder.inferredStatements.size());

            monitor.displayMessage("Generating invalid triples....", true);
            InvalidTripleGenerator invalidTripleGenerator = new InvalidTripleGenerator(monitor, ConfigParams.randomSeed, invalidTriplesNeeded);
            invalidTripleGenerator.generateInvalidTriples();
            monitor.displayMessage("Generating invalid triples finished", true);


            JSONMaker jsonMaker = new JSONMaker(monitor, ontModel);
            try {
                String jsonPath = ConfigParams.outputJsonPath.replace(".json", "_"+modelCounter+".json");
                String graphName = SharedDataHolder.ontName;
                if (null == graphName) {
                    graphName = "empty_"+modelCounter;
                }
                jsonMaker.makeJSON(graphName, jsonPath);
            } catch (IOException e) {
                monitor.stopSystem(Util.getStackTraceAsString(e), true);
            }
        }
    }


    private static OntModel loadInputSingleKBFromSingleOntology(String inputOntologyFile, Monitor monitor) {

        baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        baseOntModel.setStrictMode(true);

        baseOntModel.read("file:" + inputOntologyFile);

        SharedDataHolder.prefixMap = baseOntModel.getNsPrefixMap();
        SharedDataHolder.ontName = baseOntModel.getNsPrefixURI("");

        /**
         * Take around 700 statements only
         */
        restrictedBaseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        restrictedBaseOntModel.setStrictMode(true);

        ExtendedIterator iterator = baseOntModel.listStatements();
        int counter = 0;
        while (counter < ConfigParams.noOfBaseTriples) {
            if (iterator.hasNext()) {
                restrictedBaseOntModel.add((Statement) iterator.next());
                counter++;
            } else {
                break;
            }
        }

        monitor.displayMessage("Ontology: " + inputOntologyFile + " loaded", true);
        monitor.displayMessage("Profile: " + restrictedBaseOntModel.getProfile(), true);
        monitor.displayMessage("Total no. of node/statements: " + restrictedBaseOntModel.getGraph().size(), true);

        return restrictedBaseOntModel;

    }

    private static void doOpsSingleKBFromSingleOntology(Monitor monitor, String inputOntFullPath) {

            String[] inpPaths = inputOntFullPath.split(File.separator);
            String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
            String logPath = ConfigParams.inputOntoPath +"_log" + name;

            name = inpPaths[inpPaths.length - 1].replace(".owl", ".json");
            String outputJsonPath = ConfigParams.inputOntoPath + name;

            restrictedBaseOntModel = loadInput(inputOntFullPath, monitor);
            baseOntModelWithInference = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, restrictedBaseOntModel);

            OntologyInferer inferer = new OntologyInferer(restrictedBaseOntModel, monitor);
            baseStatements = inferer.extractBaseStatements();
            monitor.displayMessage("Inferring statements by rdfs reasoner...", true);
            try {
                inferredStatements = inferer.extractInferredStatements(baseStatements);
            } catch (IOException e) {
                monitor.stopSystem(Util.getStackTraceAsString(e), true);
            }
            monitor.displayMessage("Inferring statements by rdfs reasoner finished.", true);


            Statistics stat = new Statistics(monitor, restrictedBaseOntModel, baseStatements, baseOntModelWithInference, inferredStatements);
            monitor.displayMessage("Filling stattistics...", true);
            stat.preFillStatistics();
            monitor.displayMessage("Filling stattistics finished", true);

            int invalidTriplesNeeded = Math.min(ConfigParams.noOfinvalidTriplesNeeded, SharedDataHolder.baseStatementsAfterReasoning.size());
            invalidTriplesNeeded = Math.min(invalidTriplesNeeded, SharedDataHolder.inferredStatements.size());

            monitor.displayMessage("Generating invalid triples....", true);
            InvalidTripleGenerator invalidTripleGenerator = new InvalidTripleGenerator(monitor, ConfigParams.randomSeed, invalidTriplesNeeded);
            invalidTripleGenerator.generateInvalidTriples();
            monitor.displayMessage("Generating invalid triples finished", true);


            JSONMaker jsonMaker = new JSONMaker(monitor, restrictedBaseOntModel);
            try {
                jsonMaker.makeJSON(SharedDataHolder.ontName, outputJsonPath);
            } catch (IOException e) {
                monitor.stopSystem(Util.getStackTraceAsString(e), true);
            }
    }

    public static void main(String[] args) {

        try {
            // global log file to write
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ConfigParams.logPath));
            printStream = new PrintStream(bos);


            programMonitor = new Monitor(printStream);
            programMonitor.start("Program started", true);
            try {
                // Files.walk(Paths.get(ConfigParams.inputOntoPath)).filter(f -> f.toFile().isFile()).
                //       filter(f -> f.toFile().getAbsolutePath().endsWith(".owl")).forEach(f -> {
                programMonitor.displayMessage("\n", true);
                //   programMonitor.start("Program running for "+f.toAbsolutePath().toString(),  true);
                doOps(programMonitor, ConfigParams.inputOntoPath);
                //  });
            } catch (Exception ex) {
                programMonitor.displayMessage("Program crashed", true);
                programMonitor.displayMessage(Util.getStackTraceAsString(ex), true);
            }
        } catch (Exception ex) {
            programMonitor.displayMessage("Program crashed", true);
            programMonitor.displayMessage(Util.getStackTraceAsString(ex), true);
        } finally {
            programMonitor.stop("Program finished", true);
            printStream.close();

        }
    }


}
