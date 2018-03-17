package org.dase.IR;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.Random;

public class InvalidTripleGenerator {

    private int randomSeed;
    private Random randomIndex;
    private Random randomType;
    private int bound;
    private int triplesNeeded;
    private ArrayList<Statement> originalStatements;
    private int generatedTripleCounter;


    public InvalidTripleGenerator(int randomSeed, int triplesNeeded){
        this.randomSeed = randomSeed;
        randomIndex = new Random(this.randomSeed);
        randomType = new Random(this.randomSeed);
        this.bound = SharedDataHolder.baseStatementsAfterReasoning.size();
        this.triplesNeeded = triplesNeeded;
        generatedTripleCounter = 0;
    }


    private int generateRandomNumber(){
      return randomIndex.nextInt(this.bound);
    }

    /**
     * Take any 3 entity and make it a statement
     */
    private void generateRandomTripleByIndex(){
        int subjectIndex = generateRandomNumber();
        int predicateIndex = generateRandomNumber();
        int objectIndex = generateRandomNumber();

        Resource subject = SharedDataHolder.subjects.get(subjectIndex); // get the subject
        Property predicate =  SharedDataHolder.predicates.get(predicateIndex);  // get the predicate
        RDFNode object = SharedDataHolder.objects.get(objectIndex); // get the object

        Statement stmt = ResourceFactory.createStatement(subject,predicate,object);

        if( ! isExist(stmt)){
            SharedDataHolder.invalidinferredStatements.add(stmt);
            this.generatedTripleCounter++;
        }
    }

    /**
     * @formatter:off
     * taken triple :a rdf:type :classA
     * 1. take other types (all types excluding classA) from ontology.
     *  2.
     *      1. pick randomly any one from the types.
     *         make: stmt = a rdf:type :chosenType
     *
     *      2. pick randomly any individuals from the ontology
     *         make: stmt = otherIndi rdf:type :classA
     *
     * 4. if stmt not exist in ontology
     *    add it to invalid ones.
     *  @formatter:on
     */
    private void generateRandomTripleByType(){
        // https://dzone.com/articles/jena-listing-all-classes-and
        // https://stackoverflow.com/questions/5639265/using-jena-api-getting-all-the-classes-from-owl-file
    }

    private void generateInvalidTriple(){

        convertStatementsSetToList();

        while(this.generatedTripleCounter < this.triplesNeeded){
            generateRandomTripleByIndex();
        }
    }

    private boolean isExist(Statement stmt){
            if((SharedDataHolder.baseStatementsAfterReasoning.contains(stmt)) && SharedDataHolder.invalidinferredStatements.contains(stmt))
                return true;
            else return false;
    }


    private void convertStatementsSetToList(){
        originalStatements = new ArrayList<>();
        SharedDataHolder.baseStatementsAfterReasoning.parallelStream().forEach(eachStatement -> {
            originalStatements.add(eachStatement);
        });
    }
}
