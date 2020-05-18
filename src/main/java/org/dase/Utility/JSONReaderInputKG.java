package org.dase.Utility;
/*
Written by sarker.
Written at 5/12/20.
*/

import com.google.gson.Gson;
import org.apache.jena.rdf.model.Statement;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;

/**
 * Load statements from the json file. Monireh's bosch dataset was in this format
 */
public class JSONReaderInputKG {

    /**
     * Internal class to represent the Monireh's bosch dataset. It just includes OriginalAxioms, InvalidAxioms and InferredAxioms
     * without any statistics.
     */
    public class InputKGMapper {
        private List<String> OriginalAxioms;
        private List<String> InferredAxioms;
        private List<String> InvalidAxioms;

        public List<String> getOriginalAxioms() {
            return OriginalAxioms;
        }

        public void setOriginalAxioms(List<String> originalAxioms) {
            OriginalAxioms = originalAxioms;
        }

        public List<String> getInferredAxioms() {
            return InferredAxioms;
        }

        public void setInferredAxioms(List<String> inferredAxioms) {
            InferredAxioms = inferredAxioms;
        }

        public List<String> getInvalidAxioms() {
            return InvalidAxioms;
        }

        public void setInvalidAxioms(List<String> invalidAxioms) {
            InvalidAxioms = invalidAxioms;
        }
    }

    public JSONReaderInputKG() {

    }

    /**
     * Load statements from the json file. Monireh's bosch dataset was in this format
     *
     *  Example of single input axiom: :QD0S8qkwdX4GsyxgOImLcQ :text I have been in this gym since last year, and believe me, it has been a great experience. \nCoaches Xavier and Stacey really take their work seriously guiding...
     * @param inputJsonFile
     * @param monitor
     * @return
     */
    public HashSet<Statement> loadInputStatementsFromJson(String inputJsonFile, Monitor monitor) {
        HashSet<Statement> inputStatements = new HashSet<>();

        Gson gson = new Gson();

        try (Reader reader = new FileReader(inputJsonFile)) {

            // Convert JSON File to Java Object
            InputKGMapper inputKGMapper = gson.fromJson(reader, InputKGMapper.class);

            // print inputKGMapper
            System.out.println(inputKGMapper.getOriginalAxioms().size());

        } catch (IOException e) {
            e.printStackTrace();
            monitor.stop("Error in reading the input json file: ", true);
        }

        return inputStatements;
    }
}
