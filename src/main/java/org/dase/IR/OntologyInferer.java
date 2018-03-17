package org.dase.IR;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.io.*;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import org.dase.Utility.JSONMaker;
import org.dase.Utility.Monitor;

public class OntologyInferer {

    Monitor monitor;
	private OntModel baseOntModel;
	private OntModel infOntModel;
	static OntModel invalidInfOntModel;
	static KeySetView<Statement, Boolean> baseStatements;
	static KeySetView<Statement, Boolean> inferredStatements;
	static String inputOntologyFile = "/Users/sarker/Mega_Cloud/Inductive Reasoning/input ontologies/owl format/lubm-univ-bench.owl";
	static String inferredValidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/inferred ontologies/";
	static String invalidOntologyPath = "/Users/sarker/Mega_Cloud/Inductive Reasoning/invalid ontologies/";

	public OntologyInferer(OntModel baseOntModel, Monitor monitor) {
        this.baseOntModel = baseOntModel;
        this.monitor = monitor;
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


	public KeySetView<Statement, Boolean> extractBaseStatements(){
        baseStatements = ConcurrentHashMap.newKeySet();
        inferredStatements = ConcurrentHashMap.newKeySet();

        // list the statements in the Model
        StmtIterator iter = this.baseOntModel.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement(); // get next statement
            baseStatements.add(stmt);
        }

        return  baseStatements;
    }

	public KeySetView<Statement, Boolean> extractInferredStatements(KeySetView<Statement, Boolean> baseStatements) throws IOException {
		/**
		 * https://jena.apache.org/documentation/ontology/
		 * 
		 * Jena does not have a means for distinguishing inferred statements from those
		 * statements asserted into the base model
		 */

        StmtIterator iter = this.baseOntModel.listStatements();

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

        monitor.displayMessage("base size: " + baseStatements.size(), true);
        monitor.displayMessage("infer size: " + inferredStatements.size(), true);

		return inferredStatements;
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

    /**
     * Save as owl file
     * @throws IOException
     */
	public void saveInferred() throws IOException {
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

		// generateNoiseTriples();
	}

}
