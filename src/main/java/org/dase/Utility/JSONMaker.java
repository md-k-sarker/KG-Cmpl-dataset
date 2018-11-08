package org.dase.Utility;

//import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.*;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.PrintUtil;

import org.dase.IR.SharedDataHolder;

public class JSONMaker {


	private Monitor monitor;
	private static JsonObject jsonObject;
	OntModel baseOntModel;
	String graphName = "";
	private Map<String, String> prefixMap;
	//private Map<String, String> reversePrefixMap;

	Gson gson;

	public JSONMaker(Monitor monitor, OntModel baseOntModel) {
	    this.monitor = monitor;
        this.baseOntModel = baseOntModel;
		gson = new Gson();
		jsonObject = new JsonObject();
	}

//	private String getNormalizedStatement(Statement stmt) {
//		// System.out.println("namespace:" + stmt.getSubject().getNameSpace());
//		// namespace print the whole name
//
//		String [] split = stmt.getSubject().toString().split("#");
//		String key = split[0];
//		if(!key.endsWith("#")) {
//			key = key+ "#";
//		}
//		System.out.println("subject: "+ stmt.getSubject() );
//		System.out.println("key: "+ key);
//		System.out.println("split[1]: "+ split[1]);
//		System.out.println("reversePrefixMap.get(key): "+ reversePrefixMap.get(key));
//		String subject = reversePrefixMap.get(key) +":"+ stmt.getSubject().getLocalName();
//
//		split = stmt.getPredicate().toString().split("#");
//		key = split[0];
//		if(!key.endsWith("#")) {
//			key = key+ "#";
//		}
//		System.out.println("predicate: "+ stmt.getPredicate());
//		System.out.println("key: "+ key);
//		System.out.println("split[1]: "+ split[1]);
//		System.out.println("reversePrefixMap.get(key): "+ reversePrefixMap.get(key));
//		String predicate = reversePrefixMap.get(key) +":"+ stmt.getPredicate().getLocalName();
//
//		RDFNode objectNode = stmt.getObject();
//		String object = "";
//		if (objectNode instanceof Resource) {
//			split = ((Resource) objectNode).toString().split("#");
//			object = reversePrefixMap.get(split[0] +"#"+ ((Resource) objectNode).getLocalName());
//		} else {
//			// object is a literal
//			object = objectNode.toString();
//		}
//
//		String statement = subject + "," + predicate + "," + object;
//		//System.out.println("statement: "+ statement);
//		return statement;
//	}

	public void makeJSON(String ontoName, String writeTo) throws JsonIOException, IOException {
		
		//reversePrefixMap = new HashMap<>();
		PrintUtil pUtil = new PrintUtil();
		JsonArray inputJA, inferJA, invalidJA;
		
		
		// add ontology name
		graphName = ontoName;

		jsonObject.addProperty("OntologyName", graphName);
		this.monitor.writeMessage("josn size after adding name: " + jsonObject.size());

		// add prefixes
		prefixMap = SharedDataHolder.prefixMap;
//		prefixMap.entrySet().forEach(e->{
//			reversePrefixMap.put(e.getValue(), e.getKey());
//		});
//		String dfPrefix = prefixMap.get("");
//		if(null == dfPrefix)
//		    dfPrefix = "";
//		prefixMap.put("default",dfPrefix);
		pUtil.registerPrefixMap(prefixMap);


        //JsonElement prefix = gson.toJsonTree( prefixMap);
        //this.monitor.writeMessage("prefix: " + prefix.toString());
		jsonObject.add("Prefixes", gson.toJsonTree( prefixMap));
        this.monitor.writeMessage("josn size after adding prefix: " + jsonObject.size());
		//gson.toJson(jsonObject, new FileWriter(jsonOutputFile1));

        jsonObject.add("TotalBase Triples",  gson.toJsonTree(new HashSet<>( SharedDataHolder.baseStatements).size()));
        jsonObject.add("Axiomatic Triples in Base",  gson.toJsonTree(SharedDataHolder.axiomaticTripleCounterInBase));
        jsonObject.add("rdf:TypeTriples in Base",  gson.toJsonTree(SharedDataHolder.rdfTypeStatementsInBaseStatementsArrayList.size()));
        //jsonObject.add("TotalUniqueClasses in baseAfterReasoning",  gson.toJsonTree(new HashSet<>( SharedDataHolder.atomicConceptsInBaseAfterReasoning).size()));
       // jsonObject.add("TotalUniqueIndividuals in baseAfterReasoning",  gson.toJsonTree(new HashSet<>( SharedDataHolder.individualsInBaseAfterReasoning).size()));
        jsonObject.add("TotalUniqueSubjects in base",  gson.toJsonTree(new HashSet<>(SharedDataHolder.subjectsInBase).size()));
        jsonObject.add("TotalUniquePredicates in base",  gson.toJsonTree(new HashSet<>( SharedDataHolder.predicatesInBase).size()));
        jsonObject.add("TotalUniqueObjects in base",  gson.toJsonTree(new HashSet<>( SharedDataHolder.objectsInBase).size()));

        jsonObject.add("TotalInferred Triples",  gson.toJsonTree(new HashSet<>( SharedDataHolder.inferredStatements).size()));
        jsonObject.add("AxiomaticTriples in inferred",  gson.toJsonTree(SharedDataHolder.axiomaticTripleCounterInInferred));
        jsonObject.add("rdf:TypeTriples in inferred",  gson.toJsonTree(SharedDataHolder.rdfTypeStatementsInInferredStatementsArrayList.size()));
        //jsonObject.add("TotalUniqueClasses in inferred",  gson.toJsonTree(new HashSet<>( SharedDataHolder.atomicConceptsInInferred).size()));
        //jsonObject.add("TotalUniqueIndividuals in inferred",  gson.toJsonTree(new HashSet<>( SharedDataHolder.individualsInInferred).size()));
        jsonObject.add("TotalUniqueSubjects in inferred",  gson.toJsonTree(new HashSet<>(SharedDataHolder.subjectsInInferred).size()));
        jsonObject.add("TotalUniquePredicates in inferred",  gson.toJsonTree(new HashSet<>( SharedDataHolder.predicatesInInferred).size()));
        jsonObject.add("TotalUniqueObjects in inferred",  gson.toJsonTree(new HashSet<>( SharedDataHolder.objectsInInferred).size()));

        jsonObject.add("TotalInvalid Triples",  gson.toJsonTree(new HashSet<>(SharedDataHolder.invalidinferredStatements).size()));
        jsonObject.add("AxiomaticTriples in invalid",  gson.toJsonTree(SharedDataHolder.axiomaticTripleCounterInInvalid));
        jsonObject.add("rdf:TypeTriples in invalid",  gson.toJsonTree(SharedDataHolder.rdfTypeStatementsInInvalidArrayList.size()));
		jsonObject.add("fullRandomTriples in invalid",  gson.toJsonTree(SharedDataHolder.fullRandomTriplesInInvalid));
		jsonObject.add("trickyTriplesInInvalid in invalid",  gson.toJsonTree(SharedDataHolder.trickyTriplesInInvalid));
//        jsonObject.add("TotalUniqueClasses in invalid",  gson.toJsonTree(new HashSet<>( SharedDataHolder.atomicConceptsInInvalid).size()));
//        jsonObject.add("TotalUniqueIndividuals in invalid",  gson.toJsonTree(new HashSet<>( SharedDataHolder.individualsInInvalid).size()));
        jsonObject.add("TotalUniqueSubjects in invalid",  gson.toJsonTree(new HashSet<>(SharedDataHolder.subjectsInInvalid).size()));
        jsonObject.add("TotalUniquePredicates in invalid",  gson.toJsonTree(new HashSet<>( SharedDataHolder.predicatesInInvalid).size()));
        jsonObject.add("TotalUniqueObjects in invalid",  gson.toJsonTree(new HashSet<>( SharedDataHolder.objectsInInvalid).size()));

		// add input-axioms of the ontology
		inputJA = new JsonArray();
		SharedDataHolder.baseStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replaceAll("[<|(|>|)|']", "");
            inputJA.add(statement);
        });
        this.monitor.displayMessage("\nOriginalAxioms json size: " + inputJA.size(),true);
		jsonObject.add("OriginalAxioms", inputJA);

		
		// add inferred-axioms of the ontology
		inferJA = new JsonArray();
		SharedDataHolder.inferredStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replaceAll("[<|(|>|)|']", "");
            inferJA.add(statement);
        });
        this.monitor.displayMessage("InfAxioms json size: " + inferJA.size(),true);
		jsonObject.add("InferredAxioms", inferJA);

        // add noise axioms
        invalidJA = new JsonArray();
        SharedDataHolder.invalidinferredStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replaceAll("[<|(|>|)|']", "");
			//this.monitor.writeMessage("Invalid: "+ statement);
            invalidJA.add(statement);
        });
        this.monitor.displayMessage("InvalidInferredAxioms json size: " + invalidJA.size(),true);
        jsonObject.add("InvalidAxioms", invalidJA);



        /**
         * Please close the bufferredwriter/filewriter
         */
        monitor.displayMessage("\nWriting json to: "+writeTo+"\n", true);
        BufferedWriter bw = new BufferedWriter(new FileWriter(writeTo));
		gson.toJson(jsonObject, bw);
		bw.close();

		// free memory
        jsonObject = null;
        gson = null;

	}

    /**
     * Test method
     * @param args
     */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();

		Integer[] ints = new Integer[] { 1, 2, 3, 4 };
		JsonArray ja = new JsonArray();
		ja.add(1);
		ja.add(2);
		ja.add(3);
		ja.add(4);

		JsonObject jo = new JsonObject();
		jo.add("Hellow", new JsonPrimitive(1));
		jo.add("array", ja);

		System.out.print(" " + jo);
	}

}
