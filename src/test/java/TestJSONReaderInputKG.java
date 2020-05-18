/*
Written by sarker.
Written at 5/12/20.
*/

import org.dase.Utility.ConfigParams;
import org.dase.Utility.JSONReaderInputKG;
import org.dase.Utility.Monitor;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class TestJSONReaderInputKG {


    String inputJsonPath = "/Users/sarker/Documents/data/monireh_kg/input_bosch_data/intelligent_data_0.json";
    private static String logPath = "/Users/sarker/Documents/data/monireh_kg/log.txt";
    Monitor monitor;
    private static PrintStream printStream;


    public TestJSONReaderInputKG(String logPath) throws FileNotFoundException {
        // global log file to write
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(logPath));
        printStream = new PrintStream(bos);

        monitor = new Monitor(printStream);
    }

    public void testloadInputStatementsFromJson() {
        JSONReaderInputKG jsonReaderInputKG = new JSONReaderInputKG();
        jsonReaderInputKG.loadInputStatementsFromJson(inputJsonPath, monitor);

    }

    public static void main(String[] args) {
        try {
            TestJSONReaderInputKG testJSONReaderInputKG = new TestJSONReaderInputKG(logPath);
            testJSONReaderInputKG.testloadInputStatementsFromJson();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
