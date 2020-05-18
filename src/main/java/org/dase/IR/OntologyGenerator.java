package org.dase.IR;

import java.util.*;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Generate rdfs ontology
 * Generate 
 * @author sarker
 *
 */
public class OntologyGenerator {
	
	String s;
	Integer i;
	/**
	 * Constructor
	 */
	public OntologyGenerator() {
		
	}
	
	
	/**
	 * @formatter:off
	 * all type of possible rdfs triples..
	 * 1. class1 
	 * 
	 * 
	 * list: 
	 * 1. Open list
	 * 		1.1. rdf:Seq
	 * 		1.2. rdf:Bag
	 * 		1.3. rdf:Alt
	 * 2. closed list
	 * 		2.1. rdf:first, rdf:rest, rdf:nil
	 * @formatter:on
	 */
	public static void testTypeOfTriples() {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
		ontModel.setStrictMode(true);
		
		
		// create different types of statements
		
	}
	
	public static void main(String [] args) {

	}

}
