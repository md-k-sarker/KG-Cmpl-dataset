package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class SharedDataHolder {

    private SharedDataHolder() {

    }

    /**
     * This is exclusive for only inferred axioms. It will not have any base statements. Will contain only inferred axioms.
     */
    public static OntModel inferredOntModel;

    /**
     * This is the model of invalid triples. it may create many inconsistencies.
     */
    public static OntModel invalidOntModel;

    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements;
    /**
     * baseStatementsAfterReasoning = baseStatements + inferredStatements;
     */
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;
    public static ArrayList<Statement> baseStatementsAfterReasoningArrayList;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements;
    public static ArrayList<Statement> inferredStatementsArrayList;

    public static ConcurrentHashMap.KeySetView<Statement, Boolean> invalidinferredStatements;

    public static ArrayList<Statement> rdfTypeStatementsInBaseStatementsArrayList;
    public static ArrayList<Statement> rdfTypeStatementsInInferredStatementsArrayList;

    public static ArrayList<Statement> rdfTypeStatementsInInvalidArrayList;

    // this is taking too much time. it is only needed when we generate invalid using type change
    public static ArrayList<OntClass> atomicConceptsInBaseAfterReasoning;
    public static ArrayList<OntClass> atomicConceptsInInferred;
    //public static ArrayList<OntClass> atomicConceptsInInvalid;

    // this is taking too much time. it is only needed when we generate invalid using type change
    public static ArrayList<Individual> individualsInBaseAfterReasoning;
    public static ArrayList<Individual> individualsInInferred;
    //public static ArrayList<Individual> individualsInInvalid;

    public static ArrayList<Resource> subjectsInBase;
    public static ArrayList<Resource> subjectsInInferred;
    public static ArrayList<Resource> subjectsInInvalid;

    public static ArrayList<Property> predicatesInBase;
    public static ArrayList<Property> predicatesInInvalid;
    public static ArrayList<Property> predicatesInInferred;

    public static ArrayList<RDFNode> objectsInBase;
    public static ArrayList<RDFNode> objectsInInvalid;
    public static ArrayList<RDFNode> objectsInInferred;

    public static Map<String, String> prefixMap;
    public static String ontName;

    public static int axiomaticTripleCounterInBase = 0;
    public static int axiomaticTripleCounterInInferred = 0;
    public static int axiomaticTripleCounterInInvalid = 0;

    public static int fullRandomTriplesInInvalid = 0;
    public static int classChangeTriplesInInvalid = 0;
    public static int individualChnageTriplesInInvalid = 0;
    public static int trickyTriplesInInvalid = 0;

    public static int totalTriplesInBaseKGWithoutAnnotations = 0;
    public static int totalSplitedModelFromBaseKG = 0;

    /**
     * totalPermutationPossible = (base_triple+inferred_triple)^3
     */
    public static long totalPermutationPossible;

    /**
     * For invalid generation
     */
    /**
     * It would be sum of rdfTypeStatementsInBaseStatementsArrayList+rdfTypeStatementsInInferredStatementsArrayList;
     */
    public static ArrayList<Statement> rdfTypeStatementsAfterReasoningArrayList;
    public static ArrayList<Resource> subjectsAfterReasoning;
    public static ArrayList<Property> predicatesAfterReasoning;
    public static ArrayList<RDFNode> objectsAfterReasoning;


    //only for overall statistics:
    public static ArrayList<Integer> baseTriplesArray;
    public static ArrayList<Integer> validInferredTriplesArray;
    public static ArrayList<Integer> invalidInferredTriplesArray;
    public static ArrayList<Integer> axiomaticInBaseTriplesArray;
    public static ArrayList<Integer> axiomaticInInferredTriplesArray;

    public static ArrayList<Integer> entitiesInBaseArray;
    public static ArrayList<Integer> classesInBaseArray;
    public static ArrayList<Integer> individualsInBaseArray;
    public static ArrayList<Integer> relationsInBaseArray;
    public static ArrayList<Integer> subjectsInBaseArray;
    public static ArrayList<Integer> objectsInBaseArray;

    public static ArrayList<Integer> entitiesInInferredArray;
    public static ArrayList<Integer> classesInInferredArray;
    public static ArrayList<Integer> individualsInInferredArray;
    public static ArrayList<Integer> relationsInInferredArray;
    public static ArrayList<Integer> subjectsInInferredArray;
    public static ArrayList<Integer> objectsInInferredArray;

    public static ArrayList<Integer> rdfTypeTriplesInBaseArray;
    public static ArrayList<Integer> rdfTypeTriplesInInferredArray;


    static {

        inferredOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
        invalidOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

        baseStatements = ConcurrentHashMap.newKeySet();
        baseStatementsAfterReasoning = ConcurrentHashMap.newKeySet();
        baseStatementsAfterReasoningArrayList = new ArrayList<>();
        inferredStatements = ConcurrentHashMap.newKeySet();
        inferredStatementsArrayList = new ArrayList<>();
        invalidinferredStatements = ConcurrentHashMap.newKeySet();

        rdfTypeStatementsInBaseStatementsArrayList = new ArrayList<>();
        rdfTypeStatementsInInferredStatementsArrayList = new ArrayList<>();
        rdfTypeStatementsInInvalidArrayList = new ArrayList<>();

        atomicConceptsInBaseAfterReasoning = new ArrayList<>();
        atomicConceptsInInferred = new ArrayList<>();
        //atomicConceptsInInvalid = new ArrayList<>();

        individualsInBaseAfterReasoning = new ArrayList<>();
        individualsInInferred = new ArrayList<>();
        //individualsInInvalid = new ArrayList<>();

        subjectsInBase = new ArrayList<>();
        subjectsInInferred = new ArrayList<>();
        subjectsInInvalid = new ArrayList<>();

        predicatesInBase = new ArrayList<>();
        predicatesInInferred = new ArrayList<>();
        predicatesInInvalid = new ArrayList<>();

        objectsInBase = new ArrayList<>();
        objectsInInferred = new ArrayList<>();
        objectsInInvalid = new ArrayList<>();

        prefixMap = new HashMap<>();

        // for invalid generation
        rdfTypeStatementsAfterReasoningArrayList = new ArrayList<>();
        subjectsAfterReasoning = new ArrayList<>();
        predicatesAfterReasoning = new ArrayList<>();
        objectsAfterReasoning = new ArrayList<>();

        totalPermutationPossible = 0;

        initForOverAllStatistics();
    }

    public static void initForOverAllStatistics() {

        baseTriplesArray = new ArrayList<>();
        validInferredTriplesArray = new ArrayList<>();
        invalidInferredTriplesArray = new ArrayList<>();
        axiomaticInBaseTriplesArray = new ArrayList<>();
        axiomaticInInferredTriplesArray = new ArrayList<>();

        entitiesInBaseArray = new ArrayList<>();
        classesInBaseArray = new ArrayList<>();
        individualsInBaseArray = new ArrayList<>();
        relationsInBaseArray = new ArrayList<>();
        subjectsInBaseArray = new ArrayList<>();
        objectsInBaseArray = new ArrayList<>();

        entitiesInInferredArray = new ArrayList<>();
        classesInInferredArray = new ArrayList<>();
        individualsInInferredArray = new ArrayList<>();
        relationsInInferredArray = new ArrayList<>();
        subjectsInInferredArray = new ArrayList<>();
        objectsInInferredArray = new ArrayList<>();

        rdfTypeTriplesInBaseArray = new ArrayList<>();
        rdfTypeTriplesInInferredArray = new ArrayList<>();
    }

    public static String rdfTypeAsString = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
}
