package org.dase.synthetic;
/*
Written by sarker.
Written at 9/28/18.
*/

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dase.Utility.Monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Subrelation {

    private static String NS = "http://www.dase.org/synthetic#";

    OntModel model;
    private Monitor monitor;
    private String savingPath;
    private static int kg_no = 0;

    public Subrelation(Monitor monitor, String savingPath) {
        this.monitor = monitor;
        this.savingPath = savingPath;
        initModel();
    }

    private void initModel() {
        model = ModelFactory.createOntologyModel();
        model.setStrictMode(true);
    }

    private void generateSubClass(int limit) {
        ArrayList<OntClass> types = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            OntClass ontClass = model.createClass(NS + "class_"+kg_no+"_" + i);
            types.add(ontClass);
        }

        // add subclass relation
        for (int i = 0; i < limit - 1; i++) {
            model.add(types.get(i), RDFS.subClassOf, types.get(i + 1));
            //types.get(i).addSubClass(types.get(i + 1));
        }

        // make type
        Individual individual = model.createIndividual(NS + "individual1", types.get(0));
        model.add(individual, RDF.type, types.get(0));

        // save model
        saveModel(this.savingPath);
        //types.forEach(ontClass -> {
//            System.out.println("class: "+ ontClass);
//        });
    }

    private void generateSubProperty(int limit) {
        ArrayList<Property> properties = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Property property = model.createProperty(NS + "property_"+kg_no+"_" + i);
            properties.add(property);
        }

        // add subProperty relation
        for (int i = 0; i < limit - 1; i++) {
            model.add(properties.get(i), RDFS.subPropertyOf, properties.get(i + 1));
        }

        // make individual
        Individual subject = model.createIndividual(NS + "individual1", properties.get(0));
        Individual object = model.createIndividual(NS + "individual1", properties.get(0));
        model.add(subject, properties.get(0), object);

        // create type
        OntClass typeD = model.createClass(NS + "Domain1");
        OntClass typeR = model.createClass(NS + "Range1");
        // add domain/range
        model.add(properties.get(limit - 1), RDFS.domain, typeD);
        model.add(properties.get(limit - 1), RDFS.range, typeR);

        // save model
        saveModel(this.savingPath);
    }


    private void saveModel(String path) {
        try {
            if (!path.endsWith(".rdf")) {
                path = path + ".rdf";
            }

            File file = new File(path);

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            model.write(bufferedWriter, "RDF/XML");
            bufferedWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void generateSynthetic(int limit, SyntheticType... syntheticTypeSet) {
        for (SyntheticType type : syntheticTypeSet) {
            if (type.equals(SyntheticType.Class_SubSumption)) {
                generateSubClass(limit);
            } else if (type.equals(SyntheticType.Property_SubSumption)) {
                generateSubProperty(limit);
            }
        }
    }

    public static void main(String[] args) {


        for(kg_no=0;kg_no<5;kg_no++) {
            String saveTo = "/home/sarker/Workspaces/Project Knowledge Graph/data/input ontologies/Input kG synthetic/training/subProperty_"+kg_no+".rdf";
            Subrelation sb = new Subrelation(null, saveTo);
            sb.generateSynthetic(30, SyntheticType.Property_SubSumption);


            saveTo = "/home/sarker/Workspaces/Project Knowledge Graph/data/input ontologies/Input kG synthetic/training/subClass_"+kg_no+".rdf";
            sb = new Subrelation(null, saveTo);
            sb.generateSynthetic(30, SyntheticType.Class_SubSumption);
        }
    }
}
