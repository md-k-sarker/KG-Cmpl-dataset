package org.dase.IR;

import org.apache.jena.rdf.model.Statement;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class SharedDataHolder {
    private SharedDataHolder(){

    }

    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatements;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> baseStatementsAfterReasoning;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> inferredStatements;
    public static ConcurrentHashMap.KeySetView<Statement, Boolean> invalidinferredStatements;
}
