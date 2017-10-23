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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

/** This HW8P2 class is the proposed solution to HW8 Problem 2.
 * It is a relatively minor change to the provided code for HW6, WordCount.
 * HW8P2 simply sums the number of occurrences of a url in a given date range
 *  @author John Quinn, CSCI E-55, Fall 2015
 */
public class HW8P2 extends Configured implements Tool {

	/** This main method simply executes the class with the given arguments.
	 * @param args[] are the inputPath outputPath, startDate, and endDate.
	 * @throws Exception is the error thrown in the event of an abnormal program termination
	 */
    public static void main(String args[]) throws Exception 
    {
	int res = ToolRunner.run(new HW8P2(), args);
	System.exit(res);
    }

    public int run(String[] args) throws Exception {
	Path inputPath = new Path(args[0]);
	Path outputPath = new Path(args[1]);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
    Date startDate = simpleDateFormat.parse(args[2]);
    Long startmilliseconds = startDate.getTime()/1000;
    Date endDate = simpleDateFormat.parse(args[3]);
    Long endmilliseconds = endDate.getTime()/1000;


	Configuration conf = getConf();
	conf.set("startmilliseconds", startmilliseconds.toString());
	conf.set("endmilliseconds", endmilliseconds.toString());
	Job job = new Job(conf, this.getClass().toString());

	FileInputFormat.setInputPaths(job, inputPath);
	FileOutputFormat.setOutputPath(job, outputPath);

	job.setJobName("HW8P2");
	job.setJarByClass(HW8P2.class);
	job.setInputFormatClass(TextInputFormat.class);
	job.setOutputFormatClass(TextOutputFormat.class);
	job.setMapOutputKeyClass(Text.class);
	job.setMapOutputValueClass(IntWritable.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(IntWritable.class);

	job.setMapperClass(Map.class);
	job.setReducerClass(Reduce.class);

	return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
	private final static IntWritable one = new IntWritable(1);
	private Text word = new Text();

	/** This HW8P2$$Map inner class accepts the inputPath, startDate, and endDate, reads
	  * from that inputPath and writes, with each occurrence of that url in the data set, a 1 
	  * @param LongWritable is the byte offset in inputPath of the first character of the line being read
	  * @param Text is the entire line read from the inputPath
	  * @param Text is the url read and written
	  * @param Text is the 1 for each occurrence of that url
	  */
    @Override
	public void map(LongWritable key, Text value,
			Mapper.Context context) throws IOException, InterruptedException 
    {
    Configuration conf = context.getConfiguration();
    Long startmilliseconds = Long.parseLong(conf.get("startmilliseconds"));
    Long endmilliseconds = Long.parseLong(conf.get("endmilliseconds"));
    String line = value.toString();
	if(!line.isEmpty())
	{	
		Link urlLink = Link.parse(line);
		String url = urlLink.url();
		Long timeStamp = urlLink.timestamp();
		if((startmilliseconds <= timeStamp) && (timeStamp <= endmilliseconds))
		{
			    context.write(new Text(url), one);
		}
	}	
	}
    }
    
    /** This HW8P2$Reduce inner class accepts the key-value pairs from the Mapper
     * class and writes, for each url, the count (sum) of the occrrences of that url
     * @param Text url
	 * @param IntWritable each 1
	 * @param Text is url
	 * @param Text is count (sum) of all the occurrences of that url
	 */
    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> 
    {

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, 
    InterruptedException 
    {
    	int sum = 0;
    	for (IntWritable value : values) 
    	{
    	    sum += value.get();
    	}
    	context.write(key, new IntWritable(sum));
    }
    }
    
    /**
     * This Link inner class allows for HW8P2 and its other inner classes
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
