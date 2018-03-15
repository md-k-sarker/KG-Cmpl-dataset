package org.dase;

import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;

public class Main {

	static OntModel baseOntModel;
	static OntModel infOntModel;
	static OntModel invalidInfOntModel;
	static KeySetView<Statement, Boolean> baseStatements;
	static KeySetView<Statement, Boolean> inferredStatements;
	static KeySetView<Statement, Boolean> invalidinferredStatements;
	static String inputOntologyFile = "/Users/sarker/Mega_Cloud/Inductive Reasoning/input ontologies/owl format/lubm-univ-bench.owl";
	static String inferredValidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/inferred ontologies/";
	static String invalidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/invalid ontologies/";
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hellow World");
	}

}
