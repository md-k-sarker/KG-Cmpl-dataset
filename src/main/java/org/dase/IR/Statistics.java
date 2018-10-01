package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;
import org.dase.Utility.Monitor;

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
    private void extractClasses() {
        monitor.displayMessage("Extracting concepts....", true);
        SharedDataHolder.atomicConcepts.addAll(this.baseOntModelWithInference.listClasses().toList());
        monitor.displayMessage("Extracting concepts finished.", true);
    }

    private void extractIndividuals() {
        monitor.displayMessage("Extracting individuals....", true);
        SharedDataHolder.individuals.addAll(this.baseOntModelWithInference.listIndividuals().toList());
        monitor.displayMessage("Extracting individuals finished", true);
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

    public void preFillStatistics() {

        monitor.displayMessage("preFillStatistics() started: ", true);

        SharedDataHolder.baseStatements = this.baseStatements;
        SharedDataHolder.inferredStatements = this.inferredStatements;


        extractClasses();
        monitor.displayMessage("Total atomic concepts: " + SharedDataHolder.atomicConcepts.size(), true);
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

        this.baseStatementsAfterReasoning.forEach(stmt -> {
            SharedDataHolder.baseStatementsAfterReasoningArrayList.add(stmt);
            if (stmt.getPredicate().toString().equals(SharedDataHolder.rdfTypeAsString)) {
                SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.add(stmt);
            }
            SharedDataHolder.subjects.add(stmt.getSubject());
            SharedDataHolder.predicates.add(stmt.getPredicate());
            SharedDataHolder.objects.add(stmt.getObject());
        });


        monitor.displayMessage("Total rdf:type triples: " + SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size(), true);

        monitor.displayMessage("Total subject: " + SharedDataHolder.subjects.size(), true);
        monitor.displayMessage("Total predicates: " + SharedDataHolder.predicates.size(), true);
        monitor.displayMessage("Total objects: " + SharedDataHolder.objects.size(), true);


        monitor.displayMessage("preFillStatistics() finished: ", true);
    }


}
