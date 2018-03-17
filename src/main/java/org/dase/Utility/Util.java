package org.dase.Utility;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
    public static String getStackTraceAsString(Exception e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        return sStackTrace;
    }
}
