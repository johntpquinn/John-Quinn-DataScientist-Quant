package cscie97.asn1.knowledge.engine;

public class QueryEngineException extends Exception
{


	public QueryEngineException(String queryErrMsg) {
		super (queryErrMsg);
		
	}

	public void printErrMsg()
	{ 
		String message = getMessage();
		System.out.println(message);	    
	}
	
}

