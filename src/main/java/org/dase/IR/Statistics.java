package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;

import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    OntModel baseOntModel;
    ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;

    public Statistics(OntModel baseOntModel, ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning){
        this.baseOntModel = baseOntModel;
        this.baseStatementsAfterReasoning = baseStatementsAfterReasoning;
    }

    private void extractClasses(){
        SharedDataHolder.atomicConcepts.addAll(this.baseOntModel.listNamedClasses().toList());
    }

    private void extractIndividuals(){
        SharedDataHolder.individuals.addAll( this.baseOntModel.listIndividuals().toList());
    }


    public void fillStatistics(){
        extractClasses();
        extractIndividuals();

        baseStatementsAfterReasoning.forEach(stmt ->{
            SharedDataHolder.subjects.add(stmt.getSubject());
            SharedDataHolder.predicates.add(stmt.getPredicate());
            SharedDataHolder.objects.add(stmt.getObject());
        });
    }



}
