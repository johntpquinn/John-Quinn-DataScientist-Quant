package cscie97.asn1.knowledge.engine;

/* *
 * This Predicate class defines a Node object and allows for the 
 *    KnowledgeGraph and/or QueryEngine classes to create a Predicate
 *    or check its data members and/or for its existence
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public class Predicate {
	String identifier;
	long createDate;
	
	/*
	 * This is the Predicate constructor
	 * @param id is the Predicate's identifier
	 * @param timestamp is the time of Predicate's creation
	 */
	public Predicate(String id, long timestamp)
	{
		if (id != null)
			identifier = id;
		else
			identifier = "?";
		createDate = timestamp;
	}
    
	/*
	 * This getIdentifier method is a getter
	 * @return this.identifier.toLowerCase() is the Predicate's identifier in lower case
	 */
	public String getIdentifier(){
		return this.identifier.toLowerCase();
	}

	/*
	 * This getCreateDate method is a getter
	 * @return this.createDate is the Predicate's creation date
	 */
	public long getCreateDate(){
		return this.createDate;
	}
    
	/*
	 * This toString() method is a helper/getter
	 * @return identifier is the Predicate's identifier
	 */
	public String toString()
	{
		return identifier;
	}
}
