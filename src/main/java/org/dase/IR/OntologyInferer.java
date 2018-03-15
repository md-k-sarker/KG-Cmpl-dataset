package org.dase.IR;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.apache.jena.ext.com.google.common.base.Predicates;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import com.google.gson.JsonIOException;

public class OntologyInferer {

	static OntModel baseOntModel;
	static OntModel infOntModel;
	static OntModel invalidInfOntModel;
	static KeySetView<Statement, Boolean> baseStatements;
	static KeySetView<Statement, Boolean> inferredStatements;
	static KeySetView<Statement, Boolean> invalidinferredStatements;
	static String inputOntologyFile = "/Users/sarker/Mega_Cloud/Inductive Reasoning/input ontologies/owl format/lubm-univ-bench.owl";
	static String inferredValidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/inferred ontologies/";
	static String invalidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/invalid ontologies/";

	public OntologyInferer() {

	}

	public static void printStatements() {
		OntModel baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		baseOntModel.setStrictMode(true);

		baseOntModel.read("testOnto.owl");
		// print info
		System.out.println("Profile: " + baseOntModel);
		System.out.println("Profile: " + baseOntModel.getProfile());
		System.out.println("Size of node/statement:" + baseOntModel.getGraph().size());

		// list the statements in the Model
		StmtIterator iter = baseOntModel.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}
	}

	public static void extractInferredStatements() throws IOException {
		/**
		 * https://jena.apache.org/documentation/ontology/
		 * 
		 * Jena does not have a means for distinguishing inferred statements from those
		 * statements asserted into the base model
		 */

		baseStatements = ConcurrentHashMap.newKeySet();
		inferredStatements = ConcurrentHashMap.newKeySet();

		// list the statements in the Model
		StmtIterator iter = baseOntModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			baseStatements.add(stmt);
		}

		// create model for infer
		infOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF, baseOntModel);
		// set rdfs full
		infOntModel.getReasoner().setParameter(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_FULL);

		// list the statements in the Inferred Model
		iter = infOntModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			if (!baseStatements.contains(stmt)) {
				inferredStatements.add(stmt);
				// System.out.println(stmt);
			}
		}

		// infOntModel.getNsPrefixMap().entrySet().stream().forEach(e -> {
		// System.out.println(e.getKey() + " " + e.getValue());
		// });

		System.out.println("base size: " + baseStatements.size());
		System.out.println("infer size: " + inferredStatements.size());

	}

	public static void generateInvalidTriples() {

		invalidinferredStatements = ConcurrentHashMap.newKeySet();

		OntModel validityCheckModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF,
				baseOntModel);
		validityCheckModel.setStrictMode(true);
		validityCheckModel.getReasoner().setParameter(ReasonerVocabulary.PROPsetRDFSLevel,
				ReasonerVocabulary.RDFS_FULL);
		System.out.println("validityCheckModel size: " + validityCheckModel.size());

		// test
		ObjectProperty obp = validityCheckModel.createObjectProperty("bar");
		Resource rsc = validityCheckModel.getResource("xsd:integer");
		obp.addDomain(rsc);
		Resource subRsc = validityCheckModel.getResource("instance1");
		Resource objRsc = validityCheckModel.getResource("25.5^^xsd:decimal");
		Statement tmpStmt = ResourceFactory.createStatement(subRsc, obp, objRsc);
		System.out.println("tmp: " + tmpStmt);
		validityCheckModel.add(tmpStmt);
		System.out.println("after add validityCheckModel size: " + validityCheckModel.size());
		ValidityReport validity = validityCheckModel.validate();
		if (validity.isValid()) {
			System.out.println("invalid");

			Report report = validity.getReports().next();
			System.out.println("report :" + report.getDescription());
			Triple culprit = (Triple) report.getExtension();
			System.out.println("culprit: " + culprit);
		} else {
			System.out.println("valid");
		}

		// inferredStatements.stream().forEach(s -> {
		//
		// Statement stmt = ResourceFactory.createStatement(s.getObject().asResource(),
		// s.getPredicate(),
		// s.getSubject());
		//
		// validityCheckModel.add(stmt);
		// ValidityReport validity = validityCheckModel.validate();
		// if (!validity.isValid()) {
		// // adding this statement makes the ontology invalid
		// invalidinferredStatements.add(stmt);
		// System.out.println("This is invalid: "+ stmt);
		// }else {
		// System.out.println("this is valid: "+ stmt);
		// }
		// //validityCheckModel.remove(stmt);
		// });

		// invalidinferredStatements.parallelStream().forEach(s -> {
		// System.out.println(s);
		// });
	}

	private static void loadInvalid() {

		invalidInfOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		invalidInfOntModel.setStrictMode(true);

		invalidInfOntModel.read("file:" + invalidOntologyPath + "lubm_invalid.owl");

	}

	private static void loadInput() {

		baseOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		baseOntModel.setStrictMode(true);

		baseOntModel.read("file:" + inputOntologyFile);
		System.out.println("Profile: " + baseOntModel.getProfile());
		System.out.println("Size of node/statement:" + baseOntModel.getGraph().size());

	}

	private static void loadInferred() {

		infOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		infOntModel.setStrictMode(true);

		String fileName = inferredValidOntologyPath + "Inferred_lubm-univ-bench.owl";
		System.out.println("fileName: " + fileName);

		infOntModel.read("file:" + fileName);

	}

	private static void saveInferred() throws IOException {
		String[] names = inputOntologyFile.split("/");
		names = names[names.length - 1].split(".owl");

		infOntModel.removeAll();
		BufferedWriter bfw = new BufferedWriter(
				new FileWriter(inferredValidOntologyPath + "Inferred_" + names[0] + ".owl"));
		inferredStatements.stream().forEach(stmt -> {
			infOntModel.add(stmt);
		});
		infOntModel.write(bfw);
		bfw.close();
	}

	public static void main(String[] args) throws IOException {

		/**
		 * Load the input. It is the original schema + generated a box by amit's tool.
		 */
		loadInput();

		// extractInferredStatements();
		// saveInferred();

		/**
		 * Load the rdfs inferred axioms
		 */
		loadInferred();

		/**
		 * Invalid axioms. TODO: Here we need to generate some random....
		 */
		loadInvalid();

		JSONMaker jsonMaker = new JSONMaker(baseOntModel, infOntModel, invalidInfOntModel);
		jsonMaker.makeJSON();

		// generateInvalidTriples();
	}

}
