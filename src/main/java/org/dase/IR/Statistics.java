package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;
import org.dase.Utility.Monitor;

import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    private OntModel baseOntModel;
    private Monitor monitor;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements;
    private ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;

    public Statistics(Monitor monitor, OntModel baseOntModel, ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements, ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements) {
        this.monitor = monitor;
        this.baseOntModel = baseOntModel;
        this.baseStatements = baseStatements;
        this.inferredStatements = inferredStatements;
        this.baseStatementsAfterReasoning = ConcurrentHashMap.newKeySet();
    }

    // https://dzone.com/articles/jena-listing-all-classes-and
    // https://stackoverflow.com/questions/5639265/using-jena-api-getting-all-the-classes-from-owl-file
    private void extractClasses() {
        SharedDataHolder.atomicConcepts.addAll(this.baseOntModel.listNamedClasses().toList());
    }

    private void extractIndividuals() {
        SharedDataHolder.individuals.addAll(this.baseOntModel.listIndividuals().toList());
    }


    public void preFillStatistics() {

        //  baseStatementsAfterReasoning = baseStatements+baseStatementsAfterReasoning;

        SharedDataHolder.baseStatements = this.baseStatements;
        SharedDataHolder.inferredStatements = this.inferredStatements;


        extractClasses();
        monitor.displayMessage("Total atomic concepts: "+ SharedDataHolder.atomicConcepts.size(), true);
        extractIndividuals();
        monitor.displayMessage("Total individuals: "+ SharedDataHolder.individuals.size(), true);

        baseStatementsAfterReasoning.addAll(this.baseStatements);
        baseStatementsAfterReasoning.addAll(this.inferredStatements);
        SharedDataHolder.baseStatementsAfterReasoning = baseStatementsAfterReasoning;

        monitor.displayMessage("Total base triples: "+ SharedDataHolder.baseStatements.size(), true);
        monitor.displayMessage("Total inferred triples: "+ SharedDataHolder.inferredStatements.size(), true);
        monitor.displayMessage("Total base triples(original+inferred): "+ SharedDataHolder.baseStatementsAfterReasoning.size(), true);

        this.baseStatementsAfterReasoning.forEach(stmt -> {
            SharedDataHolder.baseStatementsAfterReasoningArrayList.add(stmt);
            if (stmt.getPredicate().toString().equals(SharedDataHolder.rdfTypeAsString)) {
                SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.add(stmt);
            }
            SharedDataHolder.subjects.add(stmt.getSubject());
            SharedDataHolder.predicates.add(stmt.getPredicate());
            SharedDataHolder.objects.add(stmt.getObject());
        });


        monitor.displayMessage("Total rdf:type triples: "+ SharedDataHolder.rdfTypeStatementsAfterReasoningArrayList.size(), true);

        monitor.displayMessage("Total subject: "+ SharedDataHolder.subjects.size(), true);
        monitor.displayMessage("Total predicates: "+ SharedDataHolder.predicates.size(), true);
        monitor.displayMessage("Total objects: "+ SharedDataHolder.objects.size(), true);
    }


}
