package cscie97.asn1.knowledge.engine;

/* *
* This Triple class defines a Triple object and allows for the 
*    KnowledgeGraph and/or QueryEngine classes to create a Triple
*    or check its data members and/or for its existence
* @author  John Quinn, CSCI E-97, Spring 2017
* @version 1.0
* @since   2017-02-06
*/
public class Triple {
    Node obj;
    Node sub;
    Predicate p;
    
    String identifier;
    long createDate;
    
    /*
	 * This is the Triple constructor
	 * @param object is the Triple's Object
	 * @param pred is the Triple's Predicate
	 * @param pred is the Triple's Subject
	 * @param timestamp is the time of Triple's creation
	 */ 
    public Triple(Node object, Predicate pred, Node subject, long timestamp)
    {
        obj = object;
        p = pred;
        sub = subject;
        
        createDate = timestamp;
        identifier = sub.getIdentifier() + " " + p.getIdentifier() + " " + obj.getIdentifier();
    }
    
    /*
	 * This getIdentifier method is a getter
	 * @return this.identifier.toLowerCase() is the Triple's identifier
	 */
    public String getidentifier(){
        return identifier;
    }
    
    /*
	 * This getCreateDate method is a getter
	 * @return this.createDate is the Triple's creation date
	 */
    public long getCreateDate(){
        return createDate;
    }
    
    /*
	 * This getObject method is a getter
	 * @return obj is the Triple's Object
	 */
    public Node getObject()
    {
        return obj;
    }
    
    /*
	 * This getSubject method is a getter
	 * @return sub is the Triple's Subject
	 */
    public Node getSubject()
    {
        return sub;
    }
    
    /*
	 * This getPredicate method is a getter
	 * @return p is the Triple's Predicate
	 */
    public Predicate getPredicate()
    {
        return p;
    }
    
    /*
	 * This toString() method is a helper/getter
	 * @return sub.toString() + " " + p.toString() + " " + obj.toString() is
	 *    the Triple is String format with its three elements whitespace delimited
	 */
    public String toString()
    {
        return sub.toString() + " " + p.toString() + " " + obj.toString();
    }
}
