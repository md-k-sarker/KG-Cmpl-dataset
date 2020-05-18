package org.dase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.dase.IR.InvalidTripleGenerator;
import org.dase.IR.OntologyInferer;
import org.dase.IR.SharedDataHolder;
import org.dase.IR.Statistics;
import org.dase.Utility.ConfigParams;
import org.dase.Utility.JSONMaker;
import org.dase.Utility.Monitor;
import org.dase.Utility.Util;


/**
 * Main class to run the various parts/options of the program.
 * This class is written as a script file.
 */
public class Main {

    private static PrintStream printStream;
    private static Monitor programMonitor;

    static OntModel baseOntModel;
    static OntModel restrictedBaseOntModel;
    static OntModel baseOntModelWithoutAnnotations;
    static OntModel baseOntModelWithInference;
    //    static OntModel infOntModel;
//    static OntModel invalidInfOntModel;
    static KeySetView<Statement, Boolean> baseStatements;
    static KeySetView<Statement, Boolean> inferredStatements;
//    static KeySetView<Statement, Boolean> invalidinferredStatements;

    static boolean hasMore = false;
    static int SplitIndex = 0;


    private static OntModel loadInputMultipleGraphFromSingleOntology(String inputOntologyFile, Monitor monitor) {

        baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        baseOntModel.setStrictMode(true);

        monitor.displayMessage("Loading input Ontology " + inputOntologyFile + " ........", true);
        baseOntModel.read("file:" + inputOntologyFile);
        monitor.displayMessage("Input Ontology: " + inputOntologyFile + " loaded", true);
        monitor.displayMessage("Profile: " + baseOntModel.getProfile().getLabel(), true);

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

    private static OntModel loadInputSingleKBFromSingleOntology(String inputOntologyFile, Monitor monitor) {

        baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        baseOntModel.setStrictMode(true);

        monitor.displayMessage("Ontology: " + inputOntologyFile + " loading.......", true);
        baseOntModel.read("file:" + inputOntologyFile);
        monitor.displayMessage("Ontology: " + inputOntologyFile + " loaded", true);
        monitor.displayMessage("Profile: " + baseOntModel.getProfile().getLabel(), true);
        monitor.displayMessage("Total No of axioms in base ontology: " + baseOntModel.listStatements().toList().size(), true);

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


        monitor.displayMessage("Total No of axioms in restricted graph/ontology: " + restrictedBaseOntModel.listStatements().toList().size(), true);

        return restrictedBaseOntModel;

    }


    private static ArrayList<OntModel> splitInputOntology(Monitor monitor, OntModel baseOntModel) {
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
        monitor.displayMessage("Splitted input ontology into " + ontModels.size() + " parts.", true);
        return ontModels;
    }

    private static void cleanSharedDataHolder() {
        SharedDataHolder.inferredOntModel = null;
        SharedDataHolder.inferredOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
        SharedDataHolder.invalidOntModel = null;
        SharedDataHolder.invalidOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);

        SharedDataHolder.baseStatements = ConcurrentHashMap.newKeySet();
        SharedDataHolder.baseStatementsAfterReasoning = ConcurrentHashMap.newKeySet();
        SharedDataHolder.baseStatementsAfterReasoningArrayList = new ArrayList<>();
        SharedDataHolder.inferredStatements = ConcurrentHashMap.newKeySet();
        SharedDataHolder.inferredStatementsArrayList = new ArrayList<>();
        SharedDataHolder.invalidinferredStatements = ConcurrentHashMap.newKeySet();

        SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList = new ArrayList<>();
        SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList = new ArrayList<>();
        SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList = new ArrayList<>();
        SharedDataHolder.rdfTypeStatementsInInvalidArrayList = new ArrayList<>();

        SharedDataHolder.atomicConceptsInBaseAfterReasoning = new ArrayList<>();
        SharedDataHolder.atomicConceptsInInferred = new ArrayList<>();
        //SharedDataHolder.atomicConceptsInInvalid = new ArrayList<>();

        SharedDataHolder.individualsInBaseAfterReasoning = new ArrayList<>();
        SharedDataHolder.individualsInInferred = new ArrayList<>();
        //SharedDataHolder.individualsInInvalid = new ArrayList<>();

        SharedDataHolder.subjectsInBase = new ArrayList<>();
        SharedDataHolder.subjectsInInferred = new ArrayList<>();
        SharedDataHolder.subjectsInInvalid = new ArrayList<>();

        SharedDataHolder.predicatesInBase = new ArrayList<>();
        SharedDataHolder.predicatesInInferred = new ArrayList<>();
        SharedDataHolder.predicatesInInvalid = new ArrayList<>();

        SharedDataHolder.objectsInBase = new ArrayList<>();
        SharedDataHolder.objectsInInferred = new ArrayList<>();
        SharedDataHolder.objectsInInvalid = new ArrayList<>();

        // dont clean prefixmap
        //SharedDataHolder.prefixMap = new HashMap<>();
        SharedDataHolder.axiomaticTripleCounterInBase = 0;
        SharedDataHolder.axiomaticTripleCounterInInferred = 0;
        SharedDataHolder.axiomaticTripleCounterInInvalid = 0;

        SharedDataHolder.fullRandomTriplesInInvalid = 0;
        SharedDataHolder.classChangeTriplesInInvalid = 0;
        SharedDataHolder.individualChnageTriplesInInvalid = 0;
        SharedDataHolder.trickyTriplesInInvalid = 0;

        //SharedDataHolder.totalTriplesInBaseKGWithoutAnnotations = 0;
        //SharedDataHolder.totalSplitedModelFromBaseKG = 0;

        // for invalid generation
        SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList = new ArrayList<>();
        SharedDataHolder.subjectsAfterReasoning = new ArrayList<>();
        SharedDataHolder.predicatesAfterReasoning = new ArrayList<>();
        SharedDataHolder.objectsAfterReasoning = new ArrayList<>();

        SharedDataHolder.totalPermutationPossible = 0;

        SharedDataHolder.inference_time_in_milisecond = 0;
        SharedDataHolder.invalid_generation_time_in_milisecond = 0;
        SharedDataHolder.output_writing_in_json_time_in_milisecond = 0;
    }

    /**
     * Prepare invalid generation
     */
    private static void prepareInvalidGeneration() {

        HashSet<Resource> tmpSubjects = new HashSet<>();
        tmpSubjects.addAll(SharedDataHolder.subjectsInBase);
        tmpSubjects.addAll(SharedDataHolder.subjectsInInferred);
        SharedDataHolder.subjectsAfterReasoning.addAll(tmpSubjects);

        HashSet<Property> tmpPredicates = new HashSet<>();
        tmpPredicates.addAll(SharedDataHolder.predicatesInBase);
        tmpPredicates.addAll(SharedDataHolder.predicatesInInferred);
        SharedDataHolder.predicatesAfterReasoning.addAll(tmpPredicates);

        HashSet<RDFNode> tmpObjects = new HashSet<>();
        tmpObjects.addAll(SharedDataHolder.objectsInBase);
        tmpObjects.addAll(SharedDataHolder.objectsInInferred);
        SharedDataHolder.objectsAfterReasoning.addAll(tmpObjects);

        double base = (double) (SharedDataHolder.baseStatementsAfterReasoning.size());
        SharedDataHolder.totalPermutationPossible = (long) Math.pow(base, 3);
    }

    /**
     * Remove annotation axioms
     *
     * @param ontModel
     * @param monitor
     * @return
     */
    private static OntModel removeAnnotations(OntModel ontModel, Monitor monitor) {

        monitor.displayMessage("Ontology size with annotations: " + ontModel.listStatements().toList().size(), true);

        baseOntModelWithoutAnnotations = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        ontModel.listStatements().forEachRemaining(statement -> {
            if (!statement.getPredicate().equals(RDFS.comment)) {
                baseOntModelWithoutAnnotations.add(statement);
            }
        });

        monitor.displayMessage("Ontology size without annotations: " + baseOntModelWithoutAnnotations.listStatements().toList().size(), true);
        return baseOntModelWithoutAnnotations;
    }

    /**
     * @param monitor
     */
    private static void generateInvalid(Monitor monitor, Statistics stat) {
        // to make it equal as inferred axioms.
        int invalidTriplesNeeded = SharedDataHolder.inferredStatements.size();

        // prepare
        prepareInvalidGeneration();

        monitor.displayMessage("Generating invalid triples....", true);
        InvalidTripleGenerator invalidTripleGenerator = new InvalidTripleGenerator(monitor, ConfigParams.randomSeed, invalidTriplesNeeded);
        invalidTripleGenerator.generateInvalidTriplesTricky();
        System.out.println("tricky invalid: " + SharedDataHolder.trickyTriplesInInvalid);
        System.out.println("total invalid: " + SharedDataHolder.invalidinferredStatements.size());
        invalidTripleGenerator.generateInvalidTriplesRandom();
        System.out.println("random invalid: " + SharedDataHolder.trickyTriplesInInvalid);
        System.out.println("total invalid: " + SharedDataHolder.invalidinferredStatements.size());
        monitor.displayMessage("Generating invalid triples finished", true);

        monitor.displayMessage("Filling post statistics...", true);
        stat.postFillStatistics();
        monitor.displayMessage("Filling post statistics finished", true);
    }


    private static void doOpsMultipleGraphFromSingleOntology(Monitor monitor, String inputOntFullPath) {

        //      String[] inpPaths = inputOntFullPath.split(File.separator);
        //   String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
        // name = inpPaths[inpPaths.length - 1].replace(".owl", ".json");
        //String outputJsonPath = ConfigParams.inputOntoPath + name;
        // monitor.displayMessage("Json writing in: "+outputJsonPath, true);

        baseOntModel = loadInputMultipleGraphFromSingleOntology(inputOntFullPath, monitor);
        baseOntModelWithoutAnnotations = removeAnnotations(baseOntModel, monitor);
        ArrayList<OntModel> ontModels = splitInputOntology(monitor, baseOntModelWithoutAnnotations);


        SharedDataHolder.totalTriplesInBaseKGWithoutAnnotations = baseOntModelWithoutAnnotations.listStatements().toList().size();
        SharedDataHolder.totalSplitedModelFromBaseKG = ontModels.size();


        int modelCounter = 0;
        for (OntModel ontModel : ontModels) {

            Long start_time = System.currentTimeMillis();

            cleanSharedDataHolder();

            modelCounter++;

            monitor.displayMessage("\n-------Working with part " + modelCounter + " of " + inputOntFullPath + "----------", true);
            monitor.displayMessage("ontmodel statement size: " + ontModel.listStatements().toList().size(), true);

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

            Long infer_end_time = System.currentTimeMillis();
            SharedDataHolder.inference_time_in_milisecond = infer_end_time - start_time;

            Statistics stat = new Statistics(monitor, ontModel, baseStatements, baseOntModelWithInference, inferredStatements);
            monitor.displayMessage("Filling pre statistics...", true);
            stat.preFillStatistics();
            monitor.displayMessage("Filling pre statistics finished", true);

            // call to gen invalid
            generateInvalid(monitor, stat);

            Long invalid_end_time = System.currentTimeMillis();
            SharedDataHolder.invalid_generation_time_in_milisecond = invalid_end_time - infer_end_time;

            monitor.displayMessage("Counting axiomatic triples...", true);
            stat.countAxiomaticTriples();
            monitor.displayMessage("Counting axiomatic triples finished", true);

            monitor.displayMessage("Counting axiomatic triples...", true);
            stat.fillForOverAllStatistics();
            monitor.displayMessage("Counting axiomatic triples finished", true);

            JSONMaker jsonMaker = new JSONMaker(monitor, ontModel);
            try {
                String jsonPath = ConfigParams.outputJsonPath.replace(".owl", "_" + modelCounter + ".json");
//                String jsonPath = ConfigParams.outputJsonPath.replace(".owl", "_tricky_invalid" + ".json"); // "_" + modelCounter +
                String graphName = SharedDataHolder.ontName;
                if (null == graphName) {
                    graphName = ConfigParams.inputOntoFileNameWithoutExtention; // + "_" + modelCounter;
                }
                jsonMaker.makeJSON(graphName, jsonPath);
            } catch (IOException e) {
                monitor.stopSystem(Util.getStackTraceAsString(e), true);
            }

            Long end_time = System.currentTimeMillis();
            SharedDataHolder.output_writing_in_json_time_in_milisecond = end_time - invalid_end_time;
        }
    }

    private static void doOpsSingleKBFromSingleOntology(Monitor monitor, String inputOntFullPath) {

//            String[] inpPaths = inputOntFullPath.split(File.separator);
//            String name = inpPaths[inpPaths.length - 1].replace(".owl", ".txt");
//            String logPath = ConfigParams.inputOntoPath +"_log" + name;
//
//            name = inpPaths[inpPaths.length - 1].replace(".owl", ".json");
//            String outputJsonPath = ConfigParams.inputOntoPath + name;

        restrictedBaseOntModel = loadInputSingleKBFromSingleOntology(inputOntFullPath, monitor);
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

        // to make it equal with inferred axioms
        int invalidTriplesNeeded = SharedDataHolder.inferredStatements.size();

        monitor.displayMessage("Generating invalid triples....", true);
        InvalidTripleGenerator invalidTripleGenerator = new InvalidTripleGenerator(monitor, ConfigParams.randomSeed, invalidTriplesNeeded);
        invalidTripleGenerator.generateInvalidTriplesTricky();
        invalidTripleGenerator.generateInvalidTriplesRandom();
        monitor.displayMessage("Generating invalid triples finished", true);


//        JSONMaker jsonMaker = new JSONMaker(monitor, restrictedBaseOntModel);
//        try {
//            jsonMaker.makeJSON(SharedDataHolder.ontName, ConfigParams.outputJsonPath);
//        } catch (IOException e) {
//            monitor.stopSystem(Util.getStackTraceAsString(e), true);
//        }

    }

    /**
     * Do the average of the arrays.
     */
    public static JsonObject overAllStatCounter(Gson statGson, JsonObject js) {

        //
        double avgBase = SharedDataHolder.baseTriplesArray.stream().mapToDouble(a -> a).average().getAsDouble();
        js.add("Avg Base Facts", statGson.toJsonTree(avgBase));
        double avgInf = SharedDataHolder.validInferredTriplesArray.stream().mapToDouble(a -> a).average().getAsDouble();
        js.add("Avg Inf Facts", statGson.toJsonTree(avgInf));
        js.add("Avg Invalid Facts", statGson.toJsonTree(SharedDataHolder.invalidInferredTriplesArray.stream().mapToDouble(a -> a).average().getAsDouble()));
        //%
        double axiomaticBasePercent = SharedDataHolder.axiomaticInBaseTriplesArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgBase;
        js.add("Axiomatic in Base", statGson.toJsonTree(axiomaticBasePercent));
        double rdfTypeInBasePercent = SharedDataHolder.rdfTypeTriplesInBaseArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgBase;
        js.add("rdfType in Base", statGson.toJsonTree(rdfTypeInBasePercent));

        double axiomaticInfPercent = SharedDataHolder.axiomaticInInferredTriplesArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgInf;
        js.add("Axiomatic in Inferred", statGson.toJsonTree(axiomaticInfPercent));
        double rdfTypeInInferred = SharedDataHolder.rdfTypeTriplesInInferredArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgInf;
        js.add("rdfType in Inferred", statGson.toJsonTree(rdfTypeInInferred));
        // count from file
        js.add("Axiomatic in Invalid", statGson.toJsonTree(0));


        double avgBaseEntity = SharedDataHolder.entitiesInBaseArray.stream().mapToDouble(a -> a).average().getAsDouble();
        js.add("Entities in Base", statGson.toJsonTree(avgBaseEntity));
        js.add("Classes in Base", statGson.toJsonTree(SharedDataHolder.classesInBaseArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgBaseEntity));
        js.add("Individuals in Base", statGson.toJsonTree(SharedDataHolder.individualsInBaseArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgBaseEntity));
        js.add("Relations in Base", statGson.toJsonTree(SharedDataHolder.relationsInBaseArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgBaseEntity));

        double avgInfEntity = SharedDataHolder.entitiesInInferredArray.stream().mapToDouble(a -> a).average().getAsDouble();
        js.add("Entities in Inferred", statGson.toJsonTree(avgInfEntity));
        js.add("Classes in Inferred", statGson.toJsonTree(SharedDataHolder.classesInInferredArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgInfEntity));
        js.add("Individuals in Inferred", statGson.toJsonTree(SharedDataHolder.individualsInInferredArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgInfEntity));
        js.add("Relations in Inferred", statGson.toJsonTree(SharedDataHolder.relationsInInferredArray.stream().mapToDouble(a -> a).average().getAsDouble() / avgInfEntity));

        return js;
    }

    public static void main(String[] args) {


        ConfigParams.init();

        String statFileJSON = Paths.get(ConfigParams.logPath).toString().replace("log.txt", "stat.json");

        try (BufferedWriter statBufferredWriter = new BufferedWriter(new FileWriter(statFileJSON))) {

            // global log file to write
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ConfigParams.logPath));
            printStream = new PrintStream(bos);

            programMonitor = new Monitor(printStream);
            programMonitor.start("Program started", true);

            Gson statGson = new Gson();
            JsonObject statJsonObject = new JsonObject();
            JsonArray inputOntologiesJA = new JsonArray();

            if (ConfigParams.batchRun) {

                Files.walk(Paths.get(ConfigParams.inputOntoRootPath)).filter(f -> f.toFile().isFile()).
                        filter(f -> f.toFile().getAbsolutePath().endsWith(".owl") && !f.toFile().getAbsolutePath().startsWith(".")).forEach(f -> {
                    programMonitor.displayMessage("\n", true);
                    programMonitor.start("Program running for " + f.toAbsolutePath().toString(), true);
                    ConfigParams.setInputOntoPath(f.toAbsolutePath().toString());
                    ConfigParams.generateOutputPath();
                    doOpsMultipleGraphFromSingleOntology(programMonitor, ConfigParams.inputOntoPath);

                    JsonObject js = new JsonObject();
                    js.add("totalTriplesInBaseKGWithoutAnnotations", statGson.toJsonTree(SharedDataHolder.totalTriplesInBaseKGWithoutAnnotations));
                    js.add("totalSplitedModelFromBaseKG", statGson.toJsonTree(SharedDataHolder.totalSplitedModelFromBaseKG));

                    statJsonObject.add(ConfigParams.inputOntoPath, js);
                    inputOntologiesJA.add(ConfigParams.inputOntoPath);

                });

            } else {
                doOpsMultipleGraphFromSingleOntology(programMonitor, ConfigParams.inputOntoPath);

                JsonObject js = new JsonObject();
                js.add("totalTriplesInBaseKGWithoutAnnotations", statGson.toJsonTree(SharedDataHolder.totalTriplesInBaseKGWithoutAnnotations));
                js.add("totalSplitedModelFromBaseKG", statGson.toJsonTree(SharedDataHolder.totalSplitedModelFromBaseKG));

                statJsonObject.add(ConfigParams.inputOntoPath, js);
                inputOntologiesJA.add(ConfigParams.inputOntoPath);
            }

            statJsonObject.add("Ontologies", inputOntologiesJA);

            JsonObject js = new JsonObject();
            js = overAllStatCounter(statGson, js);
            statJsonObject.add("stat", js);

            statGson.toJson(statJsonObject, statBufferredWriter);

        } catch (Exception ex) {
            ex.printStackTrace();
            programMonitor.displayMessage("Program crashed", true);
            programMonitor.displayMessage(Util.getStackTraceAsString(ex), true);
        } finally {
            programMonitor.stop("Program finished", true);
            printStream.close();
        }
    }
}
