package org.dase.IR;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class SharedDataHolder {
    private SharedDataHolder() {

    }

    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> invalidinferredStatements;
    public static ArrayList<OntClass> atomicConcepts;
    public static ArrayList<Individual> individuals;
    public static ArrayList<Resource> subjects;
    public static ArrayList<Property> predicates;
    public static ArrayList<RDFNode> objects;

    static {
        baseStatements = ConcurrentHashMap.newKeySet();
        baseStatementsAfterReasoning = ConcurrentHashMap.newKeySet();
        inferredStatements = ConcurrentHashMap.newKeySet();
        invalidinferredStatements = ConcurrentHashMap.newKeySet();
        ArrayList<OntClass> atomicConcepts = new ArrayList<>();
        ArrayList<Individual> individuals = new ArrayList<>();
        ArrayList<Resource> subjects = new ArrayList<>();
        ArrayList<Property> predicates = new ArrayList<>();
        ArrayList<RDFNode> objects = new ArrayList<>();
    }
}
