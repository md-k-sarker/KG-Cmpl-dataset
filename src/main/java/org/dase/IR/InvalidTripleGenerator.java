package org.dase.IR;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.dase.Utility.Monitor;
import org.dase.Utility.Util;

import java.util.ArrayList;
import java.util.Random;

public class InvalidTripleGenerator {

    private int randomSeed;
    private Random randomIndex;
    private Random randomType;
    private Random randomEntity;
    private Random randomC;
    private Random randomY;
    private int triplesNeeded;
    // private ArrayList<Statement> originalStatements;
    private int generatedTripleCounter;
    private int totalStatements;
    private Monitor monitor;
    private boolean canGenerateMore;
    private long invalidTriplesGenAttemptedRandom;
    private long invalidTriplesGenAttemptedTricky;
    private long invalidTriplesGenAttemptedTypeChnage;

    public InvalidTripleGenerator(Monitor monitor, int randomSeed, int triplesNeeded) {
        this.randomSeed = randomSeed;
        this.randomIndex = new Random(this.randomSeed);
        this.randomType = new Random(this.randomSeed);
        this.randomEntity = new Random(this.randomSeed);
        this.randomC = new Random(this.randomSeed);
        this.randomY = new Random(this.randomSeed);
        this.triplesNeeded = triplesNeeded;
        this.generatedTripleCounter = 0;
        this.totalStatements = SharedDataHolder.baseStatementsAfterReasoning.size();
        this.monitor = monitor;
        this.canGenerateMore = true;
        this.invalidTriplesGenAttemptedRandom = 0;
        this.invalidTriplesGenAttemptedTricky = 0;
        this.invalidTriplesGenAttemptedTypeChnage = 0;
    }

    /**
     * Whether more invalid generation is possible
     *
     * @return
     */
    private boolean canGenerateMoreInvalid() {
        return canGenerateMore;
    }

    private int generateRandomNumber(int bound) {
        return randomIndex.nextInt(bound);
    }

    /**
     * generate a single triple.
     * Take any 3 entity and make it a statement
     */
    private Statement generateRandomTripleByIndex() {
        int subjectIndex = generateRandomNumber(SharedDataHolder.subjectsAfterReasoning.size());
        int predicateIndex = generateRandomNumber(SharedDataHolder.predicatesAfterReasoning.size());
        int objectIndex = generateRandomNumber(SharedDataHolder.objectsAfterReasoning.size());

        Resource subject = SharedDataHolder.subjectsAfterReasoning.get(subjectIndex); // get the subject
        Property predicate = SharedDataHolder.predicatesAfterReasoning.get(predicateIndex);  // get the predicate
        RDFNode object = SharedDataHolder.objectsAfterReasoning.get(objectIndex); // get the object

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

//        int indvChooseAttempted = 0;
//
//        // change individual
//        Individual indv = null;
//        while (null == indv && indvChooseAttempted < SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size()) {
//            // choose a triple first
//            int sIndex = generateRandomNumber(SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size());
//            stmt = SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.get(sIndex);
//            // not all subject are individuals
//            if (stmt.getSubject().canAs(Individual.class) && stmt.getObject().canAs(OntClass.class)) {
//                indv = (Individual) stmt.getSubject().as(Individual.class);
//            }
//            indvChooseAttempted++;
//        }
//
//        indvChooseAttempted = 0;
//        Individual chosenIndv = null;
//        while (chosenIndv == null && indvChooseAttempted < SharedDataHolder.individualsInBaseAfterReasoning.size()) {
//            int indivIndex = generateRandomNumber(SharedDataHolder.individualsInBaseAfterReasoning.size());
//            Individual tmpIndv = SharedDataHolder.individualsInBaseAfterReasoning.get(indivIndex);
//            // Bug: TODO: java.lang.ClassCastException: org.apache.jena.rdf.model.impl.ResourceImpl cannot be cast to org.apache.jena.ontology.OntClass
//            //Puzzled!!! TODO: Dont know why object of rdfTypeStatementsAfterReasoningArrayList is not convertable to ontClass.
//            // Added check stmt.getObject().canAs(OntClass.class) and .as(OntClass.class)
//            if (!tmpIndv.equals(indv) && !isType(tmpIndv, (OntClass) stmt.getObject().as(OntClass.class))) {
//                chosenIndv = tmpIndv;
//            }
//            indvChooseAttempted++;
//        }
//
//        if (null != chosenIndv)
//            newStmt = ResourceFactory.createStatement(chosenIndv, stmt.getPredicate(), stmt.getObject());

        return newStmt;
    }

    /**
     * @formatter:off taken triple :a rdf:type :classA
     * 1. take other types (all types excluding classA) from ontology.
     * 2. pick randomly any one from the types.
     * make: stmt = a rdf:type :chosenType
     * 3. if stmt not exist in ontology
     * add it to invalid ones.
     * @formatter:on
     */
    private Statement generateTripleByChangingClass() {

        Statement stmt = null;
        Statement newStmt = null;

        int clazChooseAttempted = 0;

//        OntClass initialClaz = null;
//        while (null == initialClaz && clazChooseAttempted < SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size()) {
//            int sIndex = generateRandomNumber(SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size());
//            stmt = SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.get(sIndex);
//            if (stmt.getSubject().canAs(Individual.class) && stmt.getObject().canAs(OntClass.class)) {
//                initialClaz = (OntClass) stmt.getObject().as(OntClass.class);
//            }
//            clazChooseAttempted++;
//        }
//
//        clazChooseAttempted = 0;
//        // change ontclass
//        OntClass chosenClaz = null;
//        while (chosenClaz == null && clazChooseAttempted < SharedDataHolder.atomicConceptsInBaseAfterReasoning.size()) {
//            int clazIndex = generateRandomNumber(SharedDataHolder.atomicConceptsInBaseAfterReasoning.size());
//            OntClass tmpClaz = SharedDataHolder.atomicConceptsInBaseAfterReasoning.get(clazIndex);
//            if (!tmpClaz.equals(initialClaz) && !isType((Individual) stmt.getSubject().as(Individual.class), initialClaz)) {
//                chosenClaz = tmpClaz;
//            }
//            clazChooseAttempted++;
//        }
//        if (null != chosenClaz)
//            newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), chosenClaz);
        return newStmt;
    }


    /**
     * @return
     * @formatter:off Steps:
     * 1.       take any triple        s p o. (from the completion K’)
     * 2.       randomly select one of s, p, or o (let’s call that x)
     * 3.       retrieve all statements x rdf:type C from K’
     * 4.       randomly select one of the C’s (C_ALL) retrieved in 3. Call that D
     * 5.       retrieve all statements y rdf:type D from K’
     * 6.       randomly select one of the y’s (Y_ALL) (call that y’)
     * 7.       Replace x by y'in the  s p o triple. If this is *not* in K’, then add this to the new triples. If it is in K’, then loop back to 6.
     * @formatter:on
     */
    private Statement generateTripleToMimicInferredOnes() {
        Statement stmt = null;
        Statement newStmt = null;
        int attemptCounter = 0;

        // step 1
        int tripleIndex = generateRandomNumber(SharedDataHolder.inferredStatements.size());
        stmt = SharedDataHolder.inferredStatementsArrayList.get(tripleIndex);

        // step 2
        int entityIndex = randomEntity.nextInt(3);
        // selected entity from the selected triples
        Resource X = null;
        if (entityIndex == 0) {
            //subject
            X = stmt.getSubject();
        } else if (entityIndex == 1) {
            // predicate
            X = stmt.getPredicate();
        } else {
            // object
            attemptCounter = 0;
            while (null == X && attemptCounter < SharedDataHolder.inferredStatements.size()) {
                System.out.println("\tAttempted while2: " + attemptCounter);
                if (stmt.getObject().canAs(Resource.class)) {
                    X = stmt.getObject().as(Resource.class);
                } else {
                    stmt = SharedDataHolder.inferredStatementsArrayList.get(generateRandomNumber(SharedDataHolder.inferredStatements.size()));
                }
                attemptCounter++;
            }
        }

        // step 3
        ArrayList<Statement> statementsPhase1 = new ArrayList<>();
        ArrayList<RDFNode> C_ALL = new ArrayList<>();
        for (Statement eachStatement : SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList) {
            if (eachStatement.getSubject().equals(X)) {
                statementsPhase1.add(eachStatement);
                C_ALL.add(eachStatement.getObject());
            }
        }

        if(C_ALL.size()>0) {
            // step 4
            RDFNode D = C_ALL.get(randomC.nextInt(C_ALL.size()));

            // step 5
            ArrayList<Statement> statementsPhase2 = new ArrayList<>();
            ArrayList<Resource> Y_ALL = new ArrayList<>();
            SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList.forEach(statement -> {
                if (statement.getObject().equals(D)) {
                    statementsPhase2.add(statement);
                    Y_ALL.add(statement.getSubject());
                }
            });

            if(Y_ALL.size() > 0) {
                attemptCounter = 0;
                while (null == newStmt && attemptCounter < Y_ALL.size()) {
                    //System.out.println("\tAttempted while3: " + attemptCounter);
                    // step 6
                    Resource subjectYSelected = Y_ALL.get(randomY.nextInt(Y_ALL.size()));
                    Statement tmpStmt = ResourceFactory.createStatement(subjectYSelected, stmt.getPredicate(), stmt.getObject());
                    if (!SharedDataHolder.inferredStatements.contains(tmpStmt) && !SharedDataHolder.invalidinferredStatements.contains(tmpStmt)) {
                        newStmt = tmpStmt;
                    }
                    attemptCounter++;
                }
            }
        }

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

    /**
     * If this statement already exist or already generated.
     *
     * @param stmt
     * @return
     */
    private boolean isExistInInferred(Statement stmt) {
        if ((SharedDataHolder.inferredStatements.contains(stmt)) || SharedDataHolder.invalidinferredStatements.contains(stmt))
            return true;
        else return false;
    }


//    private void convertStatementsSetToList() {
//        originalStatements = new ArrayList<>();
//        SharedDataHolder.baseStatementsAfterReasoning.parallelStream().forEach(eachStatement -> {
//            originalStatements.add(eachStatement);
//        });
//    }

    /**
     * Generate invalid triples randomly
     *
     * @return
     */
    public boolean generateInvalidTriplesRandom() {

        try {
            while (this.generatedTripleCounter < this.triplesNeeded && invalidTriplesGenAttemptedRandom < SharedDataHolder.totalPermutationPossible) {
                System.out.println("Attemped while1: " + invalidTriplesGenAttemptedRandom);

                Statement newStmt = null;

                // generate fully random
                newStmt = generateRandomTripleByIndex();


                if (!(null == newStmt))
                    this.invalidTriplesGenAttemptedRandom++;
                //monitor.writeMessage(" stmt type: " + tripleType);
                if (!(null == newStmt) && !(isExist(newStmt))) {

                    SharedDataHolder.fullRandomTriplesInInvalid++;

                    SharedDataHolder.invalidinferredStatements.add(newStmt);
                    SharedDataHolder.invalidOntModel.add(newStmt);
                    this.generatedTripleCounter++;
                }
                //monitor.writeMessage("Gen. no. " + this.generatedTripleCounter + " invalid triple: " + newStmt.toString());
            }
            return true;
        } catch (Exception ex) {
            monitor.displayMessage(Util.getStackTraceAsString(ex), true);
            return false;
        }
    }

    /**
     * Generate tricky ones by changing x with y [ref: Pascal]
     *
     * @return
     */
    public boolean generateInvalidTriplesTricky() {
        try {

            while (this.generatedTripleCounter < this.triplesNeeded && invalidTriplesGenAttemptedTricky < SharedDataHolder.totalPermutationPossible) {
                System.out.println("Attemped while1: " + invalidTriplesGenAttemptedTricky);
                Statement newStmt = generateTripleToMimicInferredOnes();

                if (null != newStmt)
                    this.invalidTriplesGenAttemptedTricky++;
                if (!(null == newStmt) ) {
                    // && !(isExistInInferred(newStmt)) this condition is already checked in generateTripleToMimicInferredOnes()
                    SharedDataHolder.trickyTriplesInInvalid++;

                    SharedDataHolder.invalidinferredStatements.add(newStmt);
                    SharedDataHolder.invalidOntModel.add(newStmt);
                    this.generatedTripleCounter++;
                }
            }
            return true;
        } catch (Exception ex) {
            monitor.displayMessage(Util.getStackTraceAsString(ex), true);
            return false;
        }
    }

    /**
     * Type change: change class and individual
     *
     * @return
     */
    public boolean generateInvalidTriplesTrickyTypeChange() {

        try {
            while (this.generatedTripleCounter < this.triplesNeeded && invalidTriplesGenAttemptedTypeChnage < SharedDataHolder.totalPermutationPossible) {
                System.out.println("Attemped: " + invalidTriplesGenAttemptedTypeChnage);


                Statement newStmt = null;

                int tripleType = randomType.nextInt(2);
                if (tripleType == 0) {
                    // generate by changing individual
                    /**
                     * Extracting individuals is taking too much time
                     */
                    // some-times individuals may not exist in the kb. TODO: have to check that.
//                    if (SharedDataHolder.individualsInBaseAfterReasoning.size() < ConfigParams.noOfIndividualsMustHaveForChangingIndividuals) {
//                        // generate by changing class
//                        newStmt = generateTripleByChangingClass();
//                    } else {
//                        newStmt = generateTripleByChangingIndividual();
//                    }
                } else {
                    // generate by changing class
                    newStmt = generateTripleByChangingClass();
                }

                if (null != newStmt)
                    this.invalidTriplesGenAttemptedTypeChnage++;
                if (!(null == newStmt) && !(isExist(newStmt))) {
                    if (tripleType == 0) {
//                        if (SharedDataHolder.individualsInBaseAfterReasoning.size() < ConfigParams.noOfIndividualsMustHaveForChangingIndividuals) {
//                            SharedDataHolder.classChangeTriplesInInvalid++;
//                        } else {
//                            SharedDataHolder.individualChnageTriplesInInvalid++;
//                        }
                    } else {
                        SharedDataHolder.classChangeTriplesInInvalid++;
                    }
                    SharedDataHolder.invalidinferredStatements.add(newStmt);
                    SharedDataHolder.invalidOntModel.add(newStmt);
                    this.generatedTripleCounter++;
                }
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
