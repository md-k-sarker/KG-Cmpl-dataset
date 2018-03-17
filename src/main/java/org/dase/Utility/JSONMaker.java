package org.dase.Utility;

//import static org.hamcrest.CoreMatchers.instanceOf;

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

public class JSONMaker {

	private static String jsonOutputFile = "/Users/sarker/Mega_Cloud/Inductive Reasoning/jsons/raw test.json";
	private static String jsonOutputFile1 = "/Users/sarker/Mega_Cloud/Inductive Reasoning/jsons/lubmafterprefix.json";
	private static String jsonOutputFile2 = "/Users/sarker/Mega_Cloud/Inductive Reasoning/jsons/lubmafterinput.json";
	private static String jsonOutputFile3 = "/Users/sarker/Mega_Cloud/Inductive Reasoning/jsons/lubmafterinfer.json";
	private static String jsonOutputFile4 = "/Users/sarker/Mega_Cloud/Inductive Reasoning/jsons/lubmafterinvalid.json";

	private static JsonObject jsonObject;
	OntModel baseOntModel;
	OntModel infOntModel;
	OntModel invalidInfOntModel;
	String graphName = "";
	private Map<String, String> prefixMap;
	private Map<String, String> reversePrefixMap;

	Gson gson;

	public JSONMaker(OntModel baseOntModel, OntModel infOntModel, OntModel invalidInfOntModel) {
		gson = new Gson();
		jsonObject = new JsonObject();
		this.baseOntModel = baseOntModel;
		this.infOntModel = infOntModel;
		this.invalidInfOntModel = invalidInfOntModel;
		// prefixMap = new HashMap<>();
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

	public void makeJSON() throws JsonIOException, IOException {
		
		reversePrefixMap = new HashMap<>();
		PrintUtil pUtil = new PrintUtil();
		JsonArray inputJA, inferJA, invalidJA;
		
		
		// add ontology name
		graphName = baseOntModel.getNsPrefixURI("");
		if (null == graphName) {
			graphName = "empty";
		}
		jsonObject.addProperty("OntologyName", graphName);
		System.out.println("josn after adding name: " + jsonObject);

		// add prefixes
		prefixMap = baseOntModel.getNsPrefixMap();
		prefixMap.entrySet().forEach(e->{
			reversePrefixMap.put(e.getValue(), e.getKey());
		});
		prefixMap.put("default",prefixMap.get(""));
		pUtil.registerPrefixMap(prefixMap);
		
		String prefix = gson.toJson(prefixMap);
		System.out.println("prefix: " + prefix);
		jsonObject.addProperty("Prefixes", prefix);
		System.out.println("josn after adding prefix: " + jsonObject);
		gson.toJson(jsonObject, new FileWriter(jsonOutputFile1));
		

		// add input-axioms of the ontology
		inputJA = new JsonArray();
		StmtIterator iter = baseOntModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
			//String statement = getNormalizedStatement(stmt);
			//System.out.println("simplified: "+ pUtil.print(stmt));
			inputJA.add(statement);
		}
		System.out.println("OriginalAxioms: " + inputJA);
		jsonObject.add("OriginalAxioms", inputJA);
		gson.toJson(jsonObject, new FileWriter(jsonOutputFile2));

		
		// add inferred-axioms of the ontology
		inferJA = new JsonArray();
		iter = infOntModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
			inferJA.add(statement);
		}
		System.out.println("infAxioms: " + inferJA);
		jsonObject.add("InferredAxioms", inferJA);
		gson.toJson(jsonObject, new FileWriter(jsonOutputFile3));

		// add noise axioms
		invalidJA = new JsonArray();
		iter = invalidInfOntModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			String statement = pUtil.print(stmt).replace("(", "").replace(")", "");
			invalidJA.add(statement);
		}
		/**
		 * Please close the bufferredwriter/filewriter
		 */
		System.out.println("invalidAxioms: " + invalidJA);
		jsonObject.add("InvalidAxioms", invalidJA);
		gson.toJson(jsonObject, new FileWriter(jsonOutputFile4));


		String jsonString = gson.toJson(jsonObject);
		System.out.println("full json: "+ jsonString);
		FileWriter writer = new FileWriter(jsonOutputFile);
		writer.write(jsonString);
		writer.close();
	}

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
