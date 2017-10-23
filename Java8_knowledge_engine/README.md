The Knowledge Graph ("KG") is is comprisied of nodes and predicates.  Borrowing from graph theory a predicate is the
ostensible equivalant of an edge.  Since a node can be a subject, object, or both, any such
predicate can be a directed or undirected edge.

The primary purposes of the KG are storage and query for the Semantic Web.   Since all permutations are populated into
the KG it allows for O(1) query performance and, hence, retrival at large scale.  Gieseke, E. (2017).  Knowledge Graph 
Design Document. Cambridge, MA: Harvard University.

The enclosed package includes the Java 8 source code, compiled (.class) files, and allfiles needed to run and 
understand the package itself.  Within the /src folder are input, output, and explanatory files as well.  

From within the directory into which you download this package, it should run directly.   Otherwise, you may need
to re-compile it.  If so, the command javac cscie97/asn1/knowledge/engine/*.java cscie97/asn1/test/*.java
should do the trick.

Once compiled, the command java -cp . cscie97.asn1.test.TestDriver inputTriples.nt inputQueries.txt
should populate the KG with the data in that .nt file and perform queries from that .txt file, producing
the expected outuput.
