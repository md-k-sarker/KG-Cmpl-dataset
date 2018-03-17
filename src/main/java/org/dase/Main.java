package org.dase;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.dase.IR.InvalidTripleGenerator;
import org.dase.IR.OntologyInferer;
import org.dase.IR.SharedDataHolder;
import org.dase.IR.Statistics;
import org.dase.Utility.ConfigParams;
import org.dase.Utility.Monitor;
import org.dase.Utility.Util;

public class Main {

    private static PrintStream printStream;
    private static Monitor programMonitor;

    static OntModel baseOntModel;
    static OntModel infOntModel;
    static OntModel invalidInfOntModel;
    static KeySetView<Statement, Boolean> baseStatements;
    static KeySetView<Statement, Boolean> inferredStatements;
    static KeySetView<Statement, Boolean> invalidinferredStatements;
    static String inputOntologyFile = "/home/sarker/MegaCloud/Inductive Reasoning/input ontologies/owl format/Input Knowledge Graph/lubm-univ-bench.owl";
    static String inferredValidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/inferred ontologies/";
    static String invalidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/invalid ontologies/";


    private static OntModel loadInput(String inputOntologyFile, Monitor monitor) {

        baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        baseOntModel.setStrictMode(true);

        baseOntModel.read("file:" + inputOntologyFile);
        monitor.displayMessage("Ontology: "+ inputOntologyFile + " loaded", true );
        monitor.displayMessage("Profile: " + baseOntModel.getProfile(),true);
        monitor.displayMessage("Size of node/statement:" + baseOntModel.getGraph().size(),true);

        return baseOntModel;

//        baseOntModel.listStatements().forEachRemaining(stmt -> {
//            System.out.println("predicate: " + stmt.getPredicate().toString());
//        });

    }

    private static void doOps(Monitor monitor) {
        baseOntModel = loadInput(null, monitor);

        OntologyInferer inferer = new OntologyInferer(baseOntModel, monitor);
        KeySetView<Statement, Boolean> baseStatements = inferer.extractBaseStatements();
        monitor.displayMessage("Infering statements by rdfs reasoner...", true);
        try {
            KeySetView<Statement, Boolean> inferredStatements = inferer.extractInferredStatements(baseStatements);
        } catch (IOException e) {
            monitor.stopSystem(Util.getStackTraceAsString(e), true);
        }
        monitor.displayMessage("Infering statements by rdfs reasoner finished.", true);

        Statistics stat = new Statistics(monitor, baseOntModel,baseStatements,inferredStatements);
        monitor.displayMessage("Filling stattistics...", true);
        stat.preFillStatistics();
        monitor.displayMessage("Filling stattistics finished", true);

        int invalidTriplesNeeded = Math.min(ConfigParams.invalidTriplesNeeded, SharedDataHolder.baseStatementsAfterReasoning.size());
        invalidTriplesNeeded = Math.min(invalidTriplesNeeded, SharedDataHolder.inferredStatements.size());

        monitor.displayMessage("Generating invalid triples....", true);
        InvalidTripleGenerator invalidTripleGenerator = new InvalidTripleGenerator(monitor,ConfigParams.randomSeed, invalidTriplesNeeded);
        invalidTripleGenerator.generateInvalidTriples();
        monitor.displayMessage("Generating invalid triples finished", true);
    }

    public static void main(String[] args) {

        try {
            // global log file to write
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ConfigParams.logPath));
            printStream = new PrintStream(bos);


            programMonitor = new Monitor(printStream);
            programMonitor.start("Program started", true);
            try {
                doOps(programMonitor);
            } catch (Exception ex) {
                programMonitor.stop("Program crashed", true);
            } finally {
                programMonitor.stop("Program finished", true);
                printStream.close();
            }

            System.out.println("Hellow World");
        } catch (Exception ex) {

        }
    }


}
