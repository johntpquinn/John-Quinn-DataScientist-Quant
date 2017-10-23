package cscie97.asn1.knowledge.engine;

/* *
 * This Node class defines a Node object and allows for the 
 *    KnowledgeGraph and/or QueryEngine classes to create a Node
 *    or check its data members and/or for its existence
 * A Node can be an Subject, an Object, or both of the two types
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public class Node {
	String identifier;
	long createDate;
	
	/*
	 * This is the Node constructor
	 * @param id is the Node identifier
	 * @param timestamp is the time of Node's creation
	 */
	public Node(String id, long timestamp)
    {
		if (id != null)
			identifier = id;
		else
			identifier = "?";
		createDate = timestamp;
        }
        
	/*
	 * This getIdentifier method is a getter
	 * @return this.identifier.toLowerCase() is the Node's identifier in lower case
	 */
	public String getIdentifier(){
		return this.identifier.toLowerCase();
	}

	/*
	 * This getCreateDate method is a getter
	 * @return this.createDate is the Node's creation date
	 */
	public long getCreateDate(){
		return this.createDate;
	}
     
	/*
	 * This toString() method is a helper/getter
	 * @return identifier is the Node's identifier
	 */
    public String toString()
    {
    	return identifier;
    }
}
