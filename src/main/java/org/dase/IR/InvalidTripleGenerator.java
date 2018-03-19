package org.dase.IR;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.dase.Utility.Monitor;
import org.dase.Utility.Util;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class InvalidTripleGenerator {

    private int randomSeed;
    private Random randomIndex;
    private Random randomType;
    private int triplesNeeded;
    // private ArrayList<Statement> originalStatements;
    private int generatedTripleCounter;
    private int totalStatements;
    private Monitor monitor;

    public InvalidTripleGenerator(Monitor monitor, int randomSeed, int triplesNeeded) {
        this.randomSeed = randomSeed;
        randomIndex = new Random(this.randomSeed);
        randomType = new Random(this.randomSeed);
        this.triplesNeeded = triplesNeeded;
        generatedTripleCounter = 0;
        totalStatements = SharedDataHolder.baseStatementsAfterReasoning.size();
        this.monitor = monitor;
    }


    private int generateRandomNumber(int bound) {
        return randomIndex.nextInt(bound);
    }

    /**
     * generate a single triple.
     * Take any 3 entity and make it a statement
     */
    private Statement generateRandomTripleByIndex() {
        int subjectIndex = generateRandomNumber(totalStatements);
        int predicateIndex = generateRandomNumber(totalStatements);
        int objectIndex = generateRandomNumber(totalStatements);

        Resource subject = SharedDataHolder.subjects.get(subjectIndex); // get the subject
        Property predicate = SharedDataHolder.predicates.get(predicateIndex);  // get the predicate
        RDFNode object = SharedDataHolder.objects.get(objectIndex); // get the object

        Statement newStmt = ResourceFactory.createStatement(subject, predicate, object);

        return newStmt;
    }

    /**
     * generate a single triple
     *
     * @formatter:off taken triple :a rdf:type :classA
     * 1. pick randomly any individuals from the ontology
     * 2. make: stmt = otherIndi rdf:type :classA
     * <p>
     * 3. if stmt not exist in ontology
     * add it to invalid ones.
     * @formatter:on
     */
    private Statement generateTripleByChangingIndividual() {

        Statement stmt = null;
        Statement newStmt = null;

        // change individual
        Individual indv = null;
        while (null == indv) {
            int sIndex = generateRandomNumber(SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size());
            stmt = SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.get(sIndex);
            // not all subject are individuals
            if (stmt.getSubject().canAs(Individual.class)) {
                indv = (Individual) stmt.getSubject().as(Individual.class);
            }
        }
        Individual chosenIndv = null;
        while (chosenIndv == null) {
            int indivIndex = generateRandomNumber(SharedDataHolder.individuals.size());
            Individual tmpIndv = SharedDataHolder.individuals.get(indivIndex);
            if (!tmpIndv.equals(indv) && !isType(tmpIndv, (OntClass) stmt.getObject())) {
                chosenIndv = tmpIndv;
            }
        }

        newStmt = ResourceFactory.createStatement(chosenIndv, stmt.getPredicate(), stmt.getObject());


        return newStmt;
    }

    /* @formatter:off
     * taken triple :a rdf:type :classA
     * 1. take other types (all types excluding classA) from ontology.
     * 2. pick randomly any one from the types.
     * make: stmt = a rdf:type :chosenType
     * 3. if stmt not exist in ontology
     * add it to invalid ones.
     * */
    private Statement generateTripleByChangingClass() {

        int sIndex = generateRandomNumber(SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size());
        Statement stmt = SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.get(sIndex);
        Statement newStmt = null;

        // change ontclass
        // need to check wether it returns a class type..
        OntClass claz = (OntClass) stmt.getObject();
        OntClass chosenClaz = null;
        while (chosenClaz == null) {
            int clazIndex = generateRandomNumber(SharedDataHolder.atomicConcepts.size());
            OntClass tmpClaz = SharedDataHolder.atomicConcepts.get(clazIndex);
            if (!tmpClaz.equals(claz)) {
                chosenClaz = tmpClaz;
            }
        }
        newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), chosenClaz);
        return newStmt;
    }

    /**
     * return true if indv rdf:type claz.
     *
     * @param indv
     * @param claz
     * @return
     */
    private boolean isType(Individual indv, OntClass claz) {
        return claz.listInstances().toList().contains(indv);

    }


    /**
     * If this statement already exist or already generated.
     *
     * @param stmt
     * @return
     */
    private boolean isExist(Statement stmt) {
        if ((SharedDataHolder.baseStatementsAfterReasoning.contains(stmt)) || SharedDataHolder.invalidinferredStatements.contains(stmt))
            return true;
        else return false;
    }


//    private void convertStatementsSetToList() {
//        originalStatements = new ArrayList<>();
//        SharedDataHolder.baseStatementsAfterReasoning.parallelStream().forEach(eachStatement -> {
//            originalStatements.add(eachStatement);
//        });
//    }


    public boolean generateInvalidTriples() {

        try {
            while (this.generatedTripleCounter < this.triplesNeeded) {

                int tripleType = randomType.nextInt(2);
                Statement newStmt = null;
                if (tripleType == 0) {
                    // generate fully random
                    newStmt = generateRandomTripleByIndex();

                } else if (tripleType == 1) {
                    // generate by changing individual
                    /**
                     * Extracting individuals is taking too much time
                     */
                    newStmt = generateRandomTripleByIndex();
                } else {
                    // generate by changing class
                    newStmt = generateTripleByChangingClass();
                }
                monitor.writeMessage(" stmt type: " + tripleType);
                if (!(null == newStmt) && !(isExist(newStmt))) {
                    SharedDataHolder.invalidinferredStatements.add(newStmt);
                    this.generatedTripleCounter++;
                }
                monitor.writeMessage("Gen. no. " + this.generatedTripleCounter + " invalid triple: " + newStmt.toString());
            }
            return true;
        } catch (Exception ex) {
            monitor.displayMessage(Util.getStackTraceAsString(ex), true);
            return false;
        }
    }


    /**
     * Old method for generating noise
     */
    private void generateNoiseTriples() {

//        ConcurrentHashMap.KeySetView<Statement, Boolean> invalidinferredStatements = ConcurrentHashMap.newKeySet();
//
//        OntModel validityCheckModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF,
//                this.baseOntModel);
//        validityCheckModel.setStrictMode(true);
//        validityCheckModel.getReasoner().setParameter(ReasonerVocabulary.PROPsetRDFSLevel,
//                ReasonerVocabulary.RDFS_FULL);
//        System.out.println("validityCheckModel size: " + validityCheckModel.size());
//
//        // test
//        ObjectProperty obp = validityCheckModel.createObjectProperty("bar");
//        Resource rsc = validityCheckModel.getResource("xsd:integer");
//        obp.addDomain(rsc);
//        Resource subRsc = validityCheckModel.getResource("instance1");
//        Resource objRsc = validityCheckModel.getResource("25.5^^xsd:decimal");
//        Statement tmpStmt = ResourceFactory.createStatement(subRsc, obp, objRsc);
//        System.out.println("tmp: " + tmpStmt);
//        validityCheckModel.add(tmpStmt);
//        System.out.println("after add validityCheckModel size: " + validityCheckModel.size());
//        ValidityReport validity = validityCheckModel.validate();
//        if (validity.isValid()) {
//            System.out.println("invalid");
//
//            ValidityReport.Report report = validity.getReports().next();
//            System.out.println("report :" + report.getDescription());
//            Triple culprit = (Triple) report.getExtension();
//            System.out.println("culprit: " + culprit);
//        } else {
//            System.out.println("valid");
//        }

        // inferredStatements.stream().forEach(s -> {
        //
        // Statement stmt = ResourceFactory.createStatement(s.getObject().asResource(),
        // s.getPredicate(),
        // s.getSubject());
        //
        // validityCheckModel.add(stmt);
        // ValidityReport validity = validityCheckModel.validate();
        // if (!validity.isValid()) {
        // // adding this statement makes the ontology invalid
        // invalidinferredStatements.add(stmt);
        // System.out.println("This is invalid: "+ stmt);
        // }else {
        // System.out.println("this is valid: "+ stmt);
        // }
        // //validityCheckModel.remove(stmt);
        // });

        // invalidinferredStatements.parallelStream().forEach(s -> {
        // System.out.println(s);
        // });
    }
}
