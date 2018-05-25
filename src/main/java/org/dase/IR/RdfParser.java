package org.dase.IR;
/*
Written by sarker.
Written at 5/25/18.
*/

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Parse an rdf file.
 */
public class RdfParser {

    public RdfParser(String rdfPath) {
        // create a model using modelfactory
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        // now read a rdf/xml file
        ontModel.read(rdfPath);

        // iterate over all triples.
        // in jena triples are called statement.
        ontModel.listStatements().forEachRemaining(statement -> {
            System.out.println("\nstatement: "+statement);
            System.out.println("subject: "+statement.getSubject());
            System.out.println("predicate: "+statement.getPredicate());
            System.out.println("object: "+statement.getObject());
        });
    }

    public static void main(String [] args){
        String rdfFilePath = "/Users/sarker/Workspaces/Jetbrains/inductivereasoning/src/main/resources/data/testrdf.rdf";
        RdfParser rdfParser = new RdfParser(rdfFilePath);
    }
}
