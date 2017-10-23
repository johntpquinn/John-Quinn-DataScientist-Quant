package cscie97.asn1.knowledge.engine;

import java.sql.Timestamp;
import java.util.*;

/* *
 * KnowledgeGraph is a singleton, allowing for only one instance of what needs not have
 *    variations of it, because it is in essence a database.
 * KnowledgeGraph is also designed in a flyweight pattern, allowing for only one data member
 *    for each of the three elements of any Triple, because the ostensible database
 *    could be quite large.
 * KnowledgeGraph, finally, is designed for O(1) search.   Thus, because any Triple can include
 *    a wild card ("?") for any of its three elements all eight such permutations are built 
 *    into/added to the directed graph whenever a unique entry is added. 
 * The Importer class passes any putative Triple to KnowledgeGraph's importTriple(),
 *    which trims leading and trailing white space and adds the Triple if unique and
 *    properly formed (with three Strings - Subject, Object, and Predicate)
 * @author  John Quinn, CSCI E-97, Spring 2017
 * @version 1.0
 * @since   2017-02-06
 */
public final class KnowledgeGraph {
	
	//designate a singleton
    private static final KnowledgeGraph INSTANCE = new KnowledgeGraph();
    
    //create collective objects to map the three elements of Triples without duplication
    HashMap<String, Triple> tripleMap = new HashMap<String, Triple>();
    HashMap<String, HashSet<Triple>> queryMapSet = new HashMap<String, HashSet<Triple>>();
    HashMap<String, Node> nodeMap = new HashMap<String, Node>();
    HashMap<String, Predicate> predicateMap = new HashMap<String, Predicate>();

    //create a QueryEngine to receive query strings and execute the queries
    QueryEngine engine = new QueryEngine();
        
    /*
     * The Importer class passes any putative Triple to this importTriple() method
     * importTriple adds a timestamp for any addition to the KnowledgeGraph
     * importTriple first checks if the given Triple is unique (i.e., not
     *    already in the graph and, if valid and unique enters it into the graph anew
     * @param subject is the Subject of the Triple
     * @param pred is the Predicate of the Triple
     * @param obj is the Object of the Triple
     * @return t is the Triple created
     */
    public Triple importTriple(String subject, String pred, String obj){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        Triple t = makeTriple(subject, pred, obj);

        Node s = t.getSubject(), sNull = new Node(null, t.getCreateDate());
        Node o = t.getObject(), oNull = new Node(null, t.getCreateDate());
        Predicate p = t.getPredicate(), pNull = new Predicate(null, t.getCreateDate());

        // add identifiers & objects to map
        if (!nodeMap.containsKey(o))
            nodeMap.put(o.getIdentifier(), o);

        if (!nodeMap.containsKey(s))
            nodeMap.put(s.getIdentifier(), s);

        if (!predicateMap.containsKey(p))
            predicateMap.put(p.getIdentifier(), p);

        Triple t1 = new Triple(o, p, s, timestamp.getTime());

        if (!tripleMap.containsKey(t1.getidentifier()))
            tripleMap.put(t1.getidentifier(), t1);

        // create permutations 
        Triple t2 = new Triple(oNull, p, s, timestamp.getTime());
        Triple t3 = new Triple(oNull, pNull, s, timestamp.getTime());
        Triple t4 = new Triple(oNull, pNull, sNull, timestamp.getTime());
        Triple t5 = new Triple(o, pNull, s, timestamp.getTime());
        Triple t6 = new Triple(o, pNull, sNull, timestamp.getTime());
        Triple t7 = new Triple(o, p, sNull, timestamp.getTime());
        Triple t8 = new Triple(oNull, p, sNull, timestamp.getTime());
        
        //add any permutation if unique
        if (!queryMapSet.containsKey(t1.getidentifier()))
        {
            queryMapSet.put(t1.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t1.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t2.getidentifier()))
        {
            queryMapSet.put(t2.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t2.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t3.getidentifier()))
        {
            queryMapSet.put(t3.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t3.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t4.getidentifier()))
        {
            queryMapSet.put(t4.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t4.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t5.getidentifier()))
        {
            queryMapSet.put(t5.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t5.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t6.getidentifier()))
        {
            queryMapSet.put(t6.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t6.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t7.getidentifier()))
        {
            queryMapSet.put(t7.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t7.getidentifier()).add(t1);

        if (!queryMapSet.containsKey(t8.getidentifier()))
        {
            queryMapSet.put(t8.getidentifier(), new HashSet<Triple>());
        }
        queryMapSet.get(t8.getidentifier()).add(t1);
        
        return t;
    }
    
    /*
     * The QueryEngine class passes any putative Triple query to this executeQuery() method
     * executeQuery creates a Triple with the paramaters and checks if it is
     *    in the KnowledgeGraph via the queryMapSet
     * @param subject is the Subject of the Triple
     * @param predicate is the Predicate of the Triple
     * @param obj is the Object of the Triple
     * @return t is the Set of Triple(s) found, if any, or null otherwise (i.e., not found)
     */
    public Set<Triple> executeQuery(String subject, String predicate, String obj )
    {
        Triple t = makeTriple(subject.trim(), predicate.trim(), obj.trim());
        
        if (queryMapSet.containsKey(t.getidentifier()))
        {
            return queryMapSet.get(t.getidentifier());
        }
        else
        {
            return null;
        }
    }
    
    /*
     * This getNode() method is a helper
     * getNode() looks for the existence of a Node in the nodeMap
     * @param identifier is the identifier of the Node sought
     * @return Node is the Node found, if any, or null otherwise (i.e., not found)
     */  
    private Node getNode(String identifier){
        if (nodeMap.containsKey(identifier))
        {
            return nodeMap.get(identifier);
        }
        else if (nodeMap.containsKey(identifier))
        {
            return nodeMap.get(identifier);
        }

        return null;
    }
    
    /*
     * This getPredicate() method is a helper
     * getPredicate() looks for the existence of a Predicate in the predicateMap
     * @param identifier is the identifier of the Predicate sought
     * @return Predicate is the Predicate found, if any, or null otherwise (i.e., not found)
     */  
    private Predicate getPredicate(String identifier){
        if (predicateMap.containsKey(identifier))
        {
            return predicateMap.get(identifier);
        }
        return null;
    }
    
    /*
     * This getTriple() method is a helper
     * getTriple() adds a desired Triple
     * @param subject is the Subject of the Triple sought
     * @param predicate is the Predicate of the Triple sought
     * @param obj is the Object of the Triple sought
     * @return Triple is the Triple to be imported
     */  
    private Triple getTriple(Node subject, Predicate predicate, Node obj){
        return importTriple(subject.getIdentifier(), predicate.getIdentifier(), obj.getIdentifier());
    }
    
    /*
     * This getInstance() method allows access to the unique/singleton KnowledgeGraph
     * @return INSTANCE is the one instance of the unique/singleton KnowledgeGraph
     */  
    public static KnowledgeGraph getInstance(){
        return INSTANCE;
    }
 
    /*
     * This makeTriple() method is a helper
     * makeTriple() creates a desired Triple, trimming whitespace
     * @param subject is the Subject of the Triple sought
     * @param predicate is the Predicate of the Triple sought
     * @param object is the Object of the Triple sought
     * @return Triple is the Triple created
     */  
    private Triple makeTriple(String subject, String predicate, String object)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            
        Node s = null, sNull = null;
        Node o = null, oNull = null;
        Predicate p = null, pNull = null;

        String subjectKey = subject.trim();

        if (subjectKey.equals("?"))
        {
                s = new Node(null, timestamp.getTime());
                sNull = new Node(null, timestamp.getTime());
        }
        else
        {
                s = new Node(subjectKey, timestamp.getTime());
                sNull = new Node(null, timestamp.getTime());
        }

        
        String objectKey = object.trim();

        if (objectKey.equals("?"))
        {
                o = new Node(null, timestamp.getTime());
                oNull = new Node(null, timestamp.getTime());
        }
        else
        {
                o = new Node(objectKey, timestamp.getTime());
                oNull = new Node(null, timestamp.getTime());
        }

        String predicateKey = predicate.trim();

        if (predicateKey.equals("?"))
        {
                p = new Predicate(null, timestamp.getTime());
                pNull = new Predicate(null, timestamp.getTime());
        }
        else
        {
                p = new Predicate(predicateKey, timestamp.getTime());
                pNull = new Predicate(null, timestamp.getTime());
        }
        
        Triple t = new Triple(o, p, s, timestamp.getTime());
        return t;
    }

}