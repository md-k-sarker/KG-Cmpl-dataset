package org.dase.Utility;

//import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.PrintUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.dase.IR.SharedDataHolder;

public class JSONMaker {


	private Monitor monitor;
	private static JsonObject jsonObject;
	OntModel baseOntModel;
	String graphName = "";
	private Map<String, String> prefixMap;
	private Map<String, String> reversePrefixMap;

	Gson gson;

	public JSONMaker(Monitor monitor, OntModel baseOntModel) {
	    this.monitor = monitor;
        this.baseOntModel = baseOntModel;
		gson = new Gson();
		jsonObject = new JsonObject();
	}

	private String getNormalizedStatement(Statement stmt) {
		// System.out.println("namespace:" + stmt.getSubject().getNameSpace());
		// namespace print the whole name
		
		String [] split = stmt.getSubject().toString().split("#");
		String key = split[0];
		if(!key.endsWith("#")) {
			key = key+ "#";
		}
		System.out.println("subject: "+ stmt.getSubject() );
		System.out.println("key: "+ key);
		System.out.println("split[1]: "+ split[1]);
		System.out.println("reversePrefixMap.get(key): "+ reversePrefixMap.get(key));
		String subject = reversePrefixMap.get(key) +":"+ stmt.getSubject().getLocalName();
		
		split = stmt.getPredicate().toString().split("#");
		key = split[0];
		if(!key.endsWith("#")) {
			key = key+ "#";
		}
		System.out.println("predicate: "+ stmt.getPredicate());
		System.out.println("key: "+ key);
		System.out.println("split[1]: "+ split[1]);
		System.out.println("reversePrefixMap.get(key): "+ reversePrefixMap.get(key));
		String predicate = reversePrefixMap.get(key) +":"+ stmt.getPredicate().getLocalName();
		
		RDFNode objectNode = stmt.getObject();
		String object = "";
		if (objectNode instanceof Resource) {
			split = ((Resource) objectNode).toString().split("#");
			object = reversePrefixMap.get(split[0] +"#"+ ((Resource) objectNode).getLocalName());
		} else {
			// object is a literal
			object = objectNode.toString();
		}

		String statement = subject + "," + predicate + "," + object;
		//System.out.println("statement: "+ statement);
		return statement;
	}

	public void makeJSON(String writeTo) throws JsonIOException, IOException {
		
		reversePrefixMap = new HashMap<>();
		PrintUtil pUtil = new PrintUtil();
		JsonArray inputJA, inferJA, invalidJA;
		
		
		// add ontology name
		graphName = baseOntModel.getNsPrefixURI("");
		if (null == graphName) {
			graphName = "empty";
		}
		jsonObject.addProperty("OntologyName", graphName);
		this.monitor.displayMessage("josn after adding name: " + jsonObject, false);

		// add prefixes
		prefixMap = SharedDataHolder.prefixMap;
		prefixMap.entrySet().forEach(e->{
			reversePrefixMap.put(e.getValue(), e.getKey());
		});
		prefixMap.put("default",prefixMap.get(""));
		pUtil.registerPrefixMap(prefixMap);
		
		String prefix = gson.toJson(prefixMap);
        this.monitor.displayMessage("prefix: " + prefix,false);
		jsonObject.addProperty("Prefixes", prefix);
        this.monitor.displayMessage("josn after adding prefix: " + jsonObject, false);
		//gson.toJson(jsonObject, new FileWriter(jsonOutputFile1));
		

		// add input-axioms of the ontology
		inputJA = new JsonArray();
		SharedDataHolder.baseStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
            //String statement = getNormalizedStatement(stmt);
            //System.out.println("simplified: "+ pUtil.print(stmt));
            inputJA.add(statement);
        });

        this.monitor.displayMessage("OriginalAxioms: " + inputJA,false);
		jsonObject.add("OriginalAxioms", inputJA);
		//gson.toJson(jsonObject, new FileWriter(jsonOutputFile2));

		
		// add inferred-axioms of the ontology
		inferJA = new JsonArray();
		SharedDataHolder.inferredStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
            inferJA.add(statement);
        });

        this.monitor.displayMessage("infAxioms: " + inferJA,false);
		jsonObject.add("InferredAxioms", inferJA);
		// gson.toJson(jsonObject, new FileWriter(jsonOutputFile3));

		// add noise axioms
		invalidJA = new JsonArray();
		SharedDataHolder.invalidinferredStatements.forEach(stmt->{
            String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
            invalidJA.add(statement);
        });


        this.monitor.displayMessage("invalidAxioms: " + invalidJA,false);
		jsonObject.add("InvalidAxioms", invalidJA);

        /**
         * Please close the bufferredwriter/filewriter
         */
        BufferedWriter bw = new BufferedWriter(new FileWriter(writeTo));
		gson.toJson(jsonObject, bw);
		bw.close();

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