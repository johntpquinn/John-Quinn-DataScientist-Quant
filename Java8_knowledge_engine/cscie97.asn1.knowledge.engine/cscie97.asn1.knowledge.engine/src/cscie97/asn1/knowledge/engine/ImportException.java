package cscie97.asn1.knowledge.engine;

/* *
 * ImporterException can be thrown by the Importer class
 * Importer imports only valid Triples (i.e., three words/tokens: Subject, Predicate, and Object.)
 * Importer discards invalid Triples, continuing file read processing.
 * As such, ImportException exits on inability to open or read inputFile at 
 *     all (i.e., fatal IO exception) but merely throws error message and continues input
 *     file processing in the event of malformed Triple
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public class ImportException extends Exception {
    public ImportException(String msg)
    {
        super(msg);
    }
}
