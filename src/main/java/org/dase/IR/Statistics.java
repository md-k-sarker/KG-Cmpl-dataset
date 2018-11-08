package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;
import org.dase.Utility.Monitor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    private OntModel baseOntModel;
    private OntModel baseOntModelWithInference;
    private Monitor monitor;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;

    public Statistics(Monitor monitor, OntModel baseOntModel, ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements, OntModel baseOntModelWithInference, ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements) {
        this.monitor = monitor;
        this.baseOntModel = baseOntModel;
        this.baseStatements = baseStatements;
        this.baseOntModelWithInference = baseOntModelWithInference;
        this.inferredStatements = inferredStatements;
        this.baseStatementsAfterReasoning = ConcurrentHashMap.newKeySet();
    }

    // https://dzone.com/articles/jena-listing-all-classes-and
    // https://stackoverflow.com/questions/5639265/using-jena-api-getting-all-the-classes-from-owl-file
    private void extractClasses(boolean prefill) {
        if (prefill) {
            monitor.displayMessage("Extracting concepts....", true);
            SharedDataHolder.atomicConceptsInBaseAfterReasoning.addAll(this.baseOntModelWithInference.listClasses().toList());
            SharedDataHolder.atomicConceptsInInferred.addAll(SharedDataHolder.inferredOntModel.listClasses().toList());
            monitor.displayMessage("SharedDataHolder.atomicConceptsInBaseAfterReasoning: "+ SharedDataHolder.atomicConceptsInBaseAfterReasoning.size(), true);
            monitor.displayMessage("SharedDataHolder.atomicConceptsInInferred: "+ SharedDataHolder.atomicConceptsInInferred.size(), true);
            monitor.displayMessage("Extracting concepts finished.", true);
        } else {
            // invalid triples does not make valid graph
            //monitor.displayMessage("Extracting concepts....", true);
            //SharedDataHolder.atomicConceptsInInvalid.addAll(SharedDataHolder.invalidOntModel.listClasses().toList());
            //monitor.displayMessage("Extracting concepts finished.", true);
        }
    }

    private void extractIndividuals(boolean prefill) {
        if (prefill) {
            monitor.displayMessage("Extracting individuals....", true);
            SharedDataHolder.individualsInBaseAfterReasoning.addAll(this.baseOntModelWithInference.listIndividuals().toList());
            SharedDataHolder.individualsInInferred.addAll(SharedDataHolder.inferredOntModel.listIndividuals().toList());
            monitor.displayMessage("SharedDataHolder.individualsInBaseAfterReasoning: "+ SharedDataHolder.individualsInBaseAfterReasoning.size(), true);
            monitor.displayMessage("SharedDataHolder.individualsInInferred: "+ SharedDataHolder.individualsInInferred.size(), true);
            monitor.displayMessage("Extracting individuals finished", true);
        } else {
            // invalid triples does not make valid graph
            //monitor.displayMessage("Extracting individuals....", true);
            // SharedDataHolder.individualsInInvalid.addAll(SharedDataHolder.invalidOntModel.listIndividuals().toList());
            // monitor.displayMessage("Extracting individuals finished", true);
        }
    }

    /**
     * Count for overlapping
     */
    public void countAxiomaticTriples() {
        for (Statement stmt : SharedDataHolder.baseStatements) {
            if (isRDForRDFSNamespace(stmt.getSubject().getNameSpace()) && isRDForRDFSNamespace(stmt.getPredicate().getNameSpace())
                    && isRDForRDFSNamespace(stmt.getObject().toString())) {
                SharedDataHolder.axiomaticTripleCounterInBase++;
            }
        }

        for (Statement stmt : SharedDataHolder.inferredStatements) {
            //monitor.displayMessage("sub: " + stmt.getSubject().getNameSpace(), false);
            //monitor.displayMessage("pre: " + stmt.getPredicate().getNameSpace(), false);
            //monitor.displayMessage("obj: " + stmt.getObject().toString(), false);
            if (isRDForRDFSNamespace(stmt.getSubject().getNameSpace()) && isRDForRDFSNamespace(stmt.getPredicate().getNameSpace())
                    && isRDForRDFSNamespace(stmt.getObject().toString())) {

                SharedDataHolder.axiomaticTripleCounterInInferred++;
            }
        }

        for (Statement stmt : SharedDataHolder.invalidinferredStatements) {

            if (isRDForRDFSNamespace(stmt.getSubject().getNameSpace()) && isRDForRDFSNamespace(stmt.getPredicate().getNameSpace())
                    && isRDForRDFSNamespace(stmt.getObject().toString())) {

                SharedDataHolder.axiomaticTripleCounterInInvalid++;
            }

        }
    }

    public boolean isRDForRDFSNamespace(String str) {

        if (null != str) {
            String lower = str.toLowerCase();

            boolean isIn = false;

            isIn = lower.startsWith("https://www.w3.org/2000/01/rdf-schema#") ||
                    lower.startsWith("https://www.w3.org/1999/02/22-rdf-syntax-ns#");

            isIn = lower.startsWith("http://www.w3.org/2000/01/rdf-schema#") ||
                    lower.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

            //monitor.displayMessage("isIN: " + isIn, false);
            return isIn;
        }
        return false;
    }

    /**
     * Before generating invalid ones
     */
    public void preFillStatistics() {

        monitor.displayMessage("preFillStatistics() started: ", true);

        SharedDataHolder.baseStatements = this.baseStatements;
        SharedDataHolder.inferredStatements = this.inferredStatements;


//        extractClasses(true);
//        monitor.displayMessage("Total atomic concepts in Base: " + SharedDataHolder.atomicConceptsInBaseAfterReasoning.size(), true);
//        monitor.displayMessage("Total atomic concepts in Inferred: " + SharedDataHolder.atomicConceptsInInferred.size(), true);

//        extractIndividuals(true);
//        monitor.displayMessage("Total individuals in Base: " + SharedDataHolder.individualsInBaseAfterReasoning.size(), true);
//        monitor.displayMessage("Total individuals in Inferred: " + SharedDataHolder.individualsInInferred.size(), true);

        /**
         * Extracting individuals is taking too much time
         */
        // extractIndividuals();
        // monitor.displayMessage("Total individuals: "+ SharedDataHolder.individuals.size(), true);

        baseStatementsAfterReasoning.addAll(this.baseStatements);
        baseStatementsAfterReasoning.addAll(this.inferredStatements);
        SharedDataHolder.baseStatementsAfterReasoning = baseStatementsAfterReasoning;

        monitor.displayMessage("Total base triples: " + SharedDataHolder.baseStatements.size(), true);
        monitor.displayMessage("Total inferred triples: " + SharedDataHolder.inferredStatements.size(), true);
        monitor.displayMessage("Total base triples(original+inferred): " + SharedDataHolder.baseStatementsAfterReasoning.size(), true);

        /**
         * Base
         */
        this.baseStatements.forEach(stmt -> {
            SharedDataHolder.baseStatementsAfterReasoningArrayList.add(stmt);

            if (stmt.getPredicate().toString().equals(SharedDataHolder.rdfTypeAsString)) {
                SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList.add(stmt);
            }
            SharedDataHolder.subjectsInBase.add(stmt.getSubject());
            SharedDataHolder.predicatesInBase.add(stmt.getPredicate());
            SharedDataHolder.objectsInBase.add(stmt.getObject());
        });

        /**
         * Inferred
         */
        this.inferredStatements.forEach(stmt -> {
            SharedDataHolder.baseStatementsAfterReasoningArrayList.add(stmt);
            SharedDataHolder.inferredStatementsArrayList.add(stmt);

            if (stmt.getPredicate().toString().equals(SharedDataHolder.rdfTypeAsString)) {
                SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList.add(stmt);
            }
            SharedDataHolder.subjectsInInferred.add(stmt.getSubject());
            SharedDataHolder.predicatesInInferred.add(stmt.getPredicate());
            SharedDataHolder.objectsInInferred.add(stmt.getObject());
        });


        // add to rdf type
        SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.addAll(SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList);
        SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.addAll(SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList);


        monitor.displayMessage("Total rdf:type triples in base: " + SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList.size(), true);
        monitor.displayMessage("Total rdf:type triples in inferred: " + SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList.size(), true);
        monitor.displayMessage("Total rdf:type triples (original+inferred): " + SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size(), true);

        monitor.displayMessage("Total subject in base: " + SharedDataHolder.subjectsInBase.size(), true);
        monitor.displayMessage("Total subject in inferred: " + SharedDataHolder.subjectsInInferred.size(), true);
        monitor.displayMessage("Total predicates in Base: " + SharedDataHolder.predicatesInBase.size(), true);
        monitor.displayMessage("Total predicates in inferred: " + SharedDataHolder.predicatesInInferred.size(), true);
        monitor.displayMessage("Total objects in base: " + SharedDataHolder.objectsInBase.size(), true);
        monitor.displayMessage("Total objects in inferred: " + SharedDataHolder.objectsInInferred.size(), true);


        monitor.displayMessage("preFillStatistics() finished: ", true);
    }

    /**
     * After generating invalid ones
     */
    public void postFillStatistics() {

        monitor.displayMessage("postFillStatistics() started: ", true);

        // can not extract CLasses as model is invalid
        //extractClasses(false);
        //monitor.displayMessage("Total atomic concepts in Invalid: " + SharedDataHolder.atomicConceptsInInvalid.size(), true);

        // can not extract CLasses as model is invalid
        //extractIndividuals(false);
        //monitor.displayMessage("Total individuals in Invalid: " + SharedDataHolder.individualsInInvalid.size(), true);

        monitor.displayMessage("Total invalid triples: " + SharedDataHolder.invalidinferredStatements.size(), true);

        /**
         * Invalid
         */
        SharedDataHolder.invalidinferredStatements.forEach(stmt -> {

            if (stmt.getPredicate().toString().equals(SharedDataHolder.rdfTypeAsString)) {
                SharedDataHolder.rdfTypeStatementsInInvalidArrayList.add(stmt);
            }
            SharedDataHolder.subjectsInInvalid.add(stmt.getSubject());
            SharedDataHolder.predicatesInInvalid.add(stmt.getPredicate());
            SharedDataHolder.objectsInInvalid.add(stmt.getObject());
        });


        monitor.displayMessage("Total rdf:type triples in invalid: " + SharedDataHolder.rdfTypeStatementsInInvalidArrayList.size(), true);

        monitor.displayMessage("Total subject in invalid: " + SharedDataHolder.subjectsInInvalid.size(), true);
        monitor.displayMessage("Total predicates in invalid: " + SharedDataHolder.predicatesInInvalid.size(), true);
        monitor.displayMessage("Total objects in invalid: " + SharedDataHolder.objectsInInvalid.size(), true);


        monitor.displayMessage("preFillStatistics() finished: ", true);
    }

    /**
     * Fill for overall statistics
     */
    public void fillForOverAllStatistics() {

        extractClasses(true);
        extractIndividuals(true);

        SharedDataHolder.baseTriplesArray.add(SharedDataHolder.baseStatements.size());
        SharedDataHolder.validInferredTriplesArray.add(SharedDataHolder.inferredStatements.size());
        SharedDataHolder.invalidInferredTriplesArray.add(SharedDataHolder.invalidinferredStatements.size());
        SharedDataHolder.axiomaticInBaseTriplesArray.add(SharedDataHolder.axiomaticTripleCounterInBase);
        SharedDataHolder.axiomaticInInferredTriplesArray.add(SharedDataHolder.axiomaticTripleCounterInInferred);

        Set<Object> entityInBaseSet = new HashSet<>();
        entityInBaseSet.addAll(SharedDataHolder.subjectsInBase);
        entityInBaseSet.addAll(SharedDataHolder.predicatesInBase);
        entityInBaseSet.addAll(SharedDataHolder.objectsInBase);
        SharedDataHolder.entitiesInBaseArray.add(entityInBaseSet.size());
        SharedDataHolder.classesInBaseArray.add(new HashSet<>(SharedDataHolder.atomicConceptsInBaseAfterReasoning).size());
        SharedDataHolder.individualsInBaseArray.add(new HashSet<>(SharedDataHolder.individualsInBaseAfterReasoning).size());
        SharedDataHolder.relationsInBaseArray.add(new HashSet<>(SharedDataHolder.predicatesInBase).size());
        SharedDataHolder.subjectsInBaseArray.add(new HashSet<>(SharedDataHolder.subjectsInBase).size());
        SharedDataHolder.objectsInBaseArray.add(new HashSet<>(SharedDataHolder.objectsInBase).size());

        Set<Object> entityInInferredSet = new HashSet<>();
        entityInInferredSet.addAll(SharedDataHolder.subjectsInBase);
        entityInInferredSet.addAll(SharedDataHolder.predicatesInBase);
        entityInInferredSet.addAll(SharedDataHolder.objectsInBase);
        SharedDataHolder.entitiesInInferredArray.add(entityInInferredSet.size());
        SharedDataHolder.classesInInferredArray.add(new HashSet<>(SharedDataHolder.atomicConceptsInInferred).size());
        SharedDataHolder.individualsInInferredArray.add(new HashSet<>(SharedDataHolder.individualsInInferred).size());
        SharedDataHolder.relationsInInferredArray.add(new HashSet<>(SharedDataHolder.predicatesInInferred).size());
        SharedDataHolder.subjectsInInferredArray.add(new HashSet<>(SharedDataHolder.subjectsInInferred).size());
        SharedDataHolder.objectsInInferredArray.add(new HashSet<>(SharedDataHolder.objectsInInferred).size());

        SharedDataHolder.rdfTypeTriplesInBaseArray.add(new HashSet<>(SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList).size());
        SharedDataHolder.rdfTypeTriplesInInferredArray.add(new HashSet<>(SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList).size());
    }


}
