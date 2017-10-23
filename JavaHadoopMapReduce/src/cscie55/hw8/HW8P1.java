package cscie55.hw8;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

/** This HW8P1 class is the proposed solution to HW8 Problem 1.
 * It is a relatively minor change to HW7 Problem 3, DocWordIndex.
 * HW8P1 keeps track, for each url, the tags associated with that url
 * from the provided collection of json files. Ultimately, the reducer
 * produces, for each url, the unique set, or union, of those tags.
 *  @author John Quinn, CSCI E-55, Fall 2015
 */
public class HW8P1 extends Configured implements Tool 
{
	/** This main method simply executes the class with the given arguments.
	 * @param args[] are the inputPath and outputPath, as explained for the run method
	 * @throws Exception is the error thrown in the event of an abnormal program termination
	 */
    public static void main(String args[]) throws Exception 
    {
	int res = ToolRunner.run(new HW8P1(), args);
	System.exit(res);
    }

    /**
	 * This in previousKeyWord allows for the tracking of the previousKeyWord 
	 * through the inner class/object PreviousKeyWord to know when to change
	 * the key, the url
	 */
    public static PreviousKeyWord previousKeyword = new PreviousKeyWord();
    
    /**
	 * This allTags arraylist of strings collects, for each url,
	 * the tags associated with each instance of that url in the input
	 * data set
	 */
    static List<String> allTags = new ArrayList<String>();
    
    /**
	 * This sllTagsString hashset collects, for each url,
	 * the string of the elements of tags by URL
	 */
    static Set<String> allTagsString = new HashSet<String>();
    
    /**
	 * This string oldPreviousKeyWord is the concatenated string
	 * of the previous keyWords
	 */
    static String oldPreviousKeyWord = "";
    
    /** This run method provides the details for Configured and for Exception
	 * @param args[] are the inputPath and outputPath, which are entered from the command line when run
	 * @throws Exception is the error thrown in the event of an abnormal program termination
	 */
    public int run(String[] args) throws Exception {
	Path inputPath = new Path(args[0]);
	Path outputPath = new Path(args[1]);

	Configuration conf = getConf();
	Job job = new Job(conf, this.getClass().toString());

	FileInputFormat.setInputPaths(job, inputPath);
	FileOutputFormat.setOutputPath(job, outputPath);

	job.setJobName("HW8P");
	job.setJarByClass(HW8P1.class);
	job.setInputFormatClass(TextInputFormat.class);
	job.setOutputFormatClass(TextOutputFormat.class);
	job.setMapOutputKeyClass(Text.class);
	job.setMapOutputValueClass(Text.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(Text.class);

	job.setMapperClass(Map.class);
	job.setReducerClass(Reduce.class);

	return job.waitForCompletion(true) ? 0 : 1;
    }

    /** This HW8P1$$Map inner class reads the inputPath and writes, with
      * each url and, for each such url, the concatenated string of tags for it
	  * @param LongWritable is the byte offset in inputPath of the first character of the line being read
	  * @param Text is the entire line read from the inputPath
	  * @param Text is the url read and written
	  * @param Text is the concatenation of the tags for that url
	  */
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
	private final static IntWritable one = new IntWritable(1);
	private Text word = new Text();

	/** This map method does the detail mapping explained for the Map inner class.  It writes,
     * for each url, the concatenated string of tags for such url
	 * @param key is the byte offset in inputPath of the first character of the line being read
	 * @param value is the concatenated string of tags for that url
	 * @param context provides the naming-object bindings for IO
	 * @throws IOException is thrown in the event of an adverse event during reading or writing
	 * @throws InterruptedExeption is thrown in the event the Mapper is forestalled adversely
	 */
    @Override
	public void map(LongWritable key, Text value,
			Mapper.Context context) throws IOException, InterruptedException {
	String line = value.toString();
	if(!line.isEmpty())
	{	
		Link urlLink = Link.parse(line);
		String url = urlLink.url();
		List <String> tags = urlLink.tags();
		System.out.println("URL is " + url);
		System.out.flush();
		StringTokenizer tokenizer = new StringTokenizer(line);
		
		/** tagString is the concatenation of the tags for a url, separated
		 * by a comma, for clarity in output
		 */
		String tagString="";
		for (String s : tags)
		{
			tagString += s + ",";
		}
		context.write(new Text(url), new Text(tagString));
	}
	}
    }

    /** This HW8P1$Reduce inner class accepts the key-value pairs from the Mapper
     * class and writes, for each url, the set of unique (union of) tags for that url
	 * @param Text url
	 * @param Text is concatenated string of tags
	 * @param Text is url
	 * @param Text is the set of unique (union) of tags for each URL
	 */
    public static class Reduce extends Reducer<Text, Text, Text, Text> {
    
    /**
     * This private boolean seenAlready flag allows to know, for context.write, whether
     * the tag for that url has been seen already
     */	
    private boolean seenAlready;
    
    /** This reduce method does the detail reducing explained for the Reduce inner class.  It writes, 
     * for each url the set of unique (union) tags for that url
	 * @param key is the url
	 * @param values is concatenated string of the url's tags
	 * @param context provides the naming-object bindings for IO
	 * @throws IOException is thrown in the event of an adverse event during reading or writing
	 * @throws InterruptedExeption is thrown in the event the Reducer is forestalled adversely
	 */
    @Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    
    /**
     * This keyWord string is merely the convrsion of the Text-cast url to allow for
     * the more-flexible string manipulation
     */
    String keyWord = key.toString();
    
    /**
     * This oldPreviousKeyWord string is the last url seen
     */
    oldPreviousKeyWord=previousKeyword.call();
    if(!keyWord.equals(previousKeyword.call()))
    {
    	previousKeyword.set(keyWord);
        seenAlready=false;
    }
    else
    {
    	seenAlready=true;
    }
    if(!seenAlready)
    {
    	if(!"".equals(oldPreviousKeyWord))
    	{
    		cleanup(context);
    	}
    	allTags = new ArrayList<String>();
    	allTagsString=new HashSet<String>();
    }
    for (Text value : values)
	{
    	/**
    	 * This valueToArray splits the tags as delimited by commas
    	 */
		String [] valueToArray = value.toString().split(",");
		/**
		 * This valueToSet collects those split tags in
		 * this hashset
		 */
		Set<String> valueToSet = new HashSet<String>();
		/**
		 * The below iteration adds each element of the hashshet
		 */
		for(String element:valueToArray)
		{ 
			valueToSet.add(element);
		}

		/**
		 * This addAll method adds only unique elements
		 * of tags to the hashset, in the process,
		 * eliminating duplicative tags for different
		 * occurrences of the url in the data set
		 */
		allTagsString.addAll(valueToSet);
	}
	oldPreviousKeyWord = keyWord;
	}
    
    /**
     * This cleanup method allows for writing with the elimination of exceptions from IDEs, temp files, etc. 
     * @param context the current state and setting of the objects (now for writing)
     * @throws IOException
     * @throws InterruptedException
     */
    public void cleanup(Context context) throws IOException, InterruptedException
    {
    	/**
    	 * This tagOutputString string is the concatenation of the set of unique
    	 * elements, tags, for a url, created by the for loop below it
    	 */
    	String tagOutputString="";
    	for(String element:allTagsString)
    	{ 
    		tagOutputString=tagOutputString+","+element;
    	}

    	context.write(new Text(oldPreviousKeyWord), new Text(tagOutputString.substring(1)));
    }
    }
    
    /**
     * This PreviousKeyWord inner class allows for the HW8Pa$Reduce
     * inner class to check (call) the previousKeyWord see and change (set)
     * it if a new key word has been seen
     * @author John Quinn, CSCIE55, Fall 2015
     */
    public static class PreviousKeyWord
    {
    	private String previousKeyWord="";
    	public String call()
    	{ 
    		return previousKeyWord;
    	}
    	public void set(String keyWord)
    	{
    		previousKeyWord=keyWord;
    	}
    }
    
    /**
     * This Link inner class allows for HW8P and its other inner classes
     * to perform various parse, get, set, and related methods
     * of records, the url, the time stamp, and the tags for that url
     * @author Provided by Teaching Staff of CSCIE55, Fall 2015
     */
    public static class Link implements Serializable
    {
        private String url;
        private Long timestamp;
        private List<String> tags;
        private Link(String url, Long timestamp, List<String> tags) {
            this.url = url;
            this.timestamp = timestamp;
            this.tags = tags;
        }
        public static Link parse(String line)
        {
            int urlTokenEnd = line.indexOf(URL_TOKEN) + URL_TOKEN.length();
            int urlStart = line.indexOf(QUOTE, urlTokenEnd) + 1;
            assert urlStart > urlTokenEnd : String.format("urlTokenEnd: %d, urlStart: %d", urlTokenEnd, urlStart);
            int urlEnd = line.indexOf(QUOTE, urlStart);
            assert urlEnd > urlStart;
            String url = line.substring(urlStart, urlEnd);
            int timestampTokenEnd = line.indexOf(TIMESTAMP_TOKEN, urlEnd) + TIMESTAMP_TOKEN.length();
            int timestampStart = line.indexOf(SPACE, timestampTokenEnd);
            // Get past consecutive spaces
            while (line.charAt(timestampStart) == SPACE) {
                timestampStart++;
            }
            int timestampEnd = line.indexOf(COMMA, timestampStart);
            long timestamp = Long.parseLong(line.substring(timestampStart, timestampEnd));
            int tagsTokenEnd = line.indexOf(TAGS_TOKEN, timestampEnd) + TAGS_TOKEN.length();
            int startQuote;
            int endQuote = tagsTokenEnd;
            List<String> tags = new ArrayList<String>();
            while ((startQuote = line.indexOf(QUOTE, endQuote + 1) + 1) != 0) {
                endQuote = line.indexOf(QUOTE, startQuote);
                if (endQuote < 0 || startQuote < 0) {
                    return null;
                }
                String tag = line.substring(startQuote, endQuote);
                tags.add(tag);
            }
            return new Link(url, timestamp, tags);
        }
        public String url() {
    	return url;
        }
        public List<String> tags() {
    	return tags;
        }
        public String tagString () {
            String stringOfTags = "";
            for (String tag : tags) {
                stringOfTags += tag + ":";
            }
            return stringOfTags;
        }
        public long timestamp() {
            return timestamp;
        }
        private static final String URL_TOKEN = "\"url\"";
        private static final String TIMESTAMP_TOKEN = "\"timestamp\"";
        private static final String TAGS_TOKEN = "\"tags\"";
        private static final char QUOTE = '"';
        private static final char SPACE = ' ';
        private static final char COMMA = ',';
    }
    
}
