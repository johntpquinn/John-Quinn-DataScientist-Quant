package cscie97.asn1.knowledge.engine;

import java.io.BufferedReader;
import java.io.*;

/* *
 * Importer reads Triples from an input file (fileName), which supplies the data population/totality.
 * Importer reads each record/line via N-Triple format, tokenizes on whitespace, and then
 *    passes the resultant Triples to the KnowledgeGraph.importTriples() method.
 * Importer imports only valid Triples (i.e., three words/tokens: Subject, Predicate, and Object.)
 * Importer discards invalid Triples, continuing file read processing, but exits on inability to
 *     open or read inputFile at all (i.e., fatal IO exception.)
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public class Importer {
	
/*
 * ImportTripleFile() reads from fileName, tokenizes on whitespace 
 * @param fileName is the source file for all data queried by the QueryEngine class
 * @throws ImportException is the custom exception class for IO wrt fileName for malformed Triples
 * @throws Exception is a serious IO error such that fileName can't be read, resulting in exit
 */
   public static void ImportTripleFile(String fileName) throws ImportException, Exception
   {
        String thisRecord = null;
        try{
            FileReader fileR = new FileReader(fileName);
            @SuppressWarnings("resource") //buffRead is, in fact, closed below, before 1st catch()
			BufferedReader buffRead = new BufferedReader(fileR);
            while ((thisRecord = buffRead.readLine()) != null) {
                thisRecord = thisRecord.trim().replaceAll("\n", "").replace(".", "");
                String[] parts = thisRecord.split(" ");
                // if we have sub, pred, obj, import it
                if (parts.length == 3)
                {
                    KnowledgeGraph.getInstance().importTriple(parts[0], parts[1], parts[2]);
                }
                else if (parts.length <= 1)
                {
                    continue;
                }
                else
                {
                    throw new ImportException("Badly formatted input file.");
                }
            }
            buffRead.close();
            fileR.close();       
        } catch (ImportException exc) {
            System.out.println("Badly formatted triple file " + exc.getCause());
        } catch (Exception exc) {
            System.out.println("Error reading file: " + exc.getCause());
        }
    }
}