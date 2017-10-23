package cscie97.asn1.knowledge.engine;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;

import cscie97.asn1.knowledge.engine.KnowledgeGraph;

public class QueryEngine {
    
    public static FileReader fileR;
    public static BufferedReader buffRead;
    public static String queryErrMsg;
    public static String fileMsg = "Couldn't find or open file ";
    public static String recordMsg = "Couldn't retrieve record ";
    
    public static void executeQueryFile(String fileName) throws QueryEngineException, IOException, FileNotFoundException
    {
        String fileOpenErrMsg = fileMsg + fileName;
        int recNum = 0;
        try 
        {
            fileR = new FileReader(fileName);
            buffRead = new BufferedReader(fileR);
            String thisRecord;
            while ((thisRecord = buffRead.readLine()) != null && thisRecord.length()!=0) {
                recNum++;
                executeQuery(thisRecord);
            }
            fileR.close();
        }
        catch(QueryEngineException exc)
        {
            System.out.println("Error on line " + recNum + " " + exc.getMessage());
        }
        catch(FileNotFoundException exc)
        {
            System.out.println("Failed to open query file.");
        }
        catch(IOException exc)
        {
            System.out.println("Failed to process query file.");
        }
    }
    
    public static void executeQuery(String query) throws QueryEngineException
    {
        // remove period at end
        query = query.replace(".", "");
        
        int tokenNo = 0;
        StringTokenizer st = new StringTokenizer(query);
        ArrayList<String> words = new ArrayList<String>(3);
        while (st.hasMoreTokens()) 
        {   
            words.add(st.nextToken());
            tokenNo++;
        }
        if (words.size() != 3)
        {
            queryErrMsg = "Can't process query request.  Request must have 3 words";
            throw new QueryEngineException(queryErrMsg);
        }
        else
        {
            System.out.println(query);
            Set<Triple> answers = KnowledgeGraph.getInstance().executeQuery(words.get(0), words.get(1), words.get(2));
            
            if (answers != null)
            {
                //System.out.println(answers.size() + " responses:");
                for (Triple t : answers)
                {
                    System.out.println(t.toString() + ".");
                }
            }
            else
            {
                System.out.println("<null>");
            }
            System.out.println();
        }
        
    }      
}