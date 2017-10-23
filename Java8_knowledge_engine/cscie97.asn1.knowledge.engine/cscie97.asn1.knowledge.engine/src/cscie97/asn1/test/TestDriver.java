package cscie97.asn1.test;

import java.io.BufferedReader;
import java.io.*;
import cscie97.asn1.knowledge.engine.*;

/* *
 * The TestDriver class includes the main() method for the combined cscie97.asn1 projects.
 * TestDriver takes in from Command Line Instruction ("CLI")
 *    the file including the entire ostensible data base of all Triples ("tripleFile") and
 *    the file including all Triple queries ("queryFile")
 * TestDriver 1st checks that CLI has provided two arguments, those two file names.
 *    If not, TestDriver throws exception (see below above main())
 *    If so:
 *       TestDriver commences the reading of tripleFile by calling Importer.ImportTripleFile &
 *       TestDriver commences the reading of tripleFile by calling QueryEngine.executeQueryFile.   
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public class TestDriver {
	
	/*
	 * This main() function accepts the names of tripleFile and queryFile.
	 * Then main() checks for 2 arguments.
	 *    If there are 2 arguments, Importer is started with the 1st & QueryEngine with the 2nd
	 *    If not, Exception is thrown.
	 * @throws Exception results in exit, as cscie.asn1 simply can't run with other than
	 *    tripleFile and QueryFile.
	 */      
    public static void main(String[] args) throws Exception {
        String thisRecord = null;

        if (args.length == 2)
        {
            String tripleFile = args[0];
            String queryFile = args[1];
            
            Importer.ImportTripleFile(tripleFile);
            
                
            QueryEngine.executeQueryFile(queryFile);
        }
    }
}

