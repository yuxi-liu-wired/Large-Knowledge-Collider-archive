package eu.larkc.plugin.hadoop.filterplugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TriplesUtils;

public class FilterMapper extends Mapper<Text, Text, NullWritable, Text> {

	public static final String FILTER_MAPPER_PATTERN = "FilterMapper.Pattern";
	public static final String FILTER_MAPPER_NO_PATTERNS = "FilterMapper.noPatterns";
	private static Logger log = LoggerFactory.getLogger(FilterMapper.class);
	private Text oValue = new Text();
	
	private ArrayList<String[]> patterns;
	
	protected void map(Text key, Text value, Context context) {
		try {
			String nodes[] = TriplesUtils.parseTriple(value.toString(), key.toString());
			
			for (String[] pattern:patterns) {
				if (matchPattern(pattern,nodes)) {
					oValue.set(value);
					context.write(NullWritable.get(), oValue);
					return;
				}
			}
		}
		catch (Exception e) {
			log.warn("Error filtering triple "+value.toString(), e);
		}
	}

	protected void setup(Context context) throws IOException, InterruptedException {
		int noOfPatterns = context.getConfiguration().getInt(FILTER_MAPPER_NO_PATTERNS, 0);
		patterns=new ArrayList<String[]>(noOfPatterns);
		for (int i=0; i< noOfPatterns; i++) {
			String pattern=context.getConfiguration().get(FILTER_MAPPER_PATTERN+i, "");
			try {
				String[] parsedPattern = parseTriple(pattern, "bn",false);
				log.debug("Pattern parsed: " + Arrays.toString(parsedPattern));
				patterns.add(parsedPattern); // Use bnodes instead of variables
			} catch (Exception e) {
				throw new IOException("Invalid pattern: " + pattern,e);
			} 
		}
	}
	
	
	private boolean matchPattern (String[] pattern, String[] value) {
	//	if (log.isDebugEnabled()) 
	//		log.debug("Value " + Arrays.toString(value) + " matches " + Arrays.toString(pattern) + " ?");

		
		if (! ( (pattern[0].startsWith("_") || pattern[0].equals(value[0]))
				&& (pattern[1].startsWith("_") || pattern[1].equals(value[1]))
				&& (pattern[2].startsWith("_") || pattern[2].equals(value[2]))) ) {
				return false;
			}
		
		if (pattern[0] == pattern[1] && value[0].equals(value[0])) {
			return false;
		}
		if (pattern[0] == pattern[2] && value[0].equals(value[2])) {
			return false;
		}
		if (pattern[1] == pattern[2] && value[1].equals(value[2])) {
			return false;
		}

	//	if (log.isDebugEnabled()) 
	//		log.debug("Yes");
		return true;
	}
	
	
	public static String[] parseTriple(String triple, String fileId,
			boolean rewriteBlankNodes) throws Exception {
		String[] values = new String[3];

		// Parse subject
		if (triple.startsWith("<")) {
			values[0] = triple.substring(0, triple.indexOf('>') + 1);
		} else { // Is a bnode
			if (rewriteBlankNodes) {
				values[0] = "_:" + fileId
						+ triple.substring(1, triple.indexOf(' '));
			} else {
				values[0] = triple.substring(0, triple.indexOf(' '));
			}
		}

		triple = triple.substring(triple.indexOf(' ') + 1);
		// Parse predicate. 
		if (triple.startsWith("<")) { // URI
			values[1] = triple.substring(0, triple.indexOf('>') + 1);
		} else if (triple.charAt(0) == '"') { // Literal
			values[1] = triple.substring(0,
					triple.substring(1).indexOf('"') + 2);
			triple = triple.substring(values[2].length(), triple.length());
			values[1] += triple.substring(0, triple.indexOf(' '));
		} else { // Bnode
			if (rewriteBlankNodes) {
				values[1] = "_:" + fileId
						+ triple.substring(1, triple.indexOf(' '));
			} else {
				values[1] = triple.substring(0, triple.indexOf(' '));
			}
		}

		// Parse object
		triple = triple.substring(values[1].length() + 1);
		if (triple.startsWith("<")) { // URI
			values[2] = triple.substring(0, triple.indexOf('>') + 1);
		} else if (triple.charAt(0) == '"') { // Literal
			values[2] = triple.substring(0,
					triple.substring(1).indexOf('"') + 2);
			triple = triple.substring(values[2].length(), triple.length());
			values[2] += triple.substring(0, triple.indexOf(' '));
		} else { // Bnode
			if (rewriteBlankNodes) {
				values[2] = "_:" + fileId
						+ triple.substring(1, triple.indexOf(' '));
			} else {
				values[2] = triple.substring(0, triple.indexOf(' '));
			}
		}
		return values;
	}
	
	
	

	/*
	 * Test pattern parsing
	 */
//	public static void main(String[] args) {
//		String pString="_:-1,http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://xmlns.com/foaf/0.1/Person\n"+
//			"_:-1,http://xmlns.com/foaf/0.1/name,\"Frank van Harmelen\"\n"+
//				"_:-1,http://www.w3.org/1999/02/22-rdf-syntax-ns#type,http://xmlns.com/foaf/0.1/Person\n"+
//				"_:-1,http://xmlns.com/foaf/0.1/knows,_:-3\n"+
//				"_:-1,http://xmlns.com/foaf/0.1/name,_:-3\"";
//		
//		ArrayList<String[]> patterns=new ArrayList<String[]>(5);
//		for (int i=0; i< 5; i++) {
//			String pattern=null;//context.getConfiguration().get(FILTER_MAPPER_PATTERN+i, "");
//			try {
//				patterns.set(i, TriplesUtils.parseTriple(pattern, "")); // Use bnodes instead of variables
//			} catch (Exception e) {
//				throw new IOException("Invalid pattern format",e);
//			} 
//		}
//	}
	
	
/*	private static String[] parsePattern(String pattern)  {
		String[] values = new String[3];
		
		// Parse subject
		if (pattern.startsWith("<")) {
			values[0] = pattern.substring(0, pattern.indexOf('>') + 1);
		} else if (pattern.charAt(0)== '_'){ // Is a bnode
			throw IllegalArgumentException("Can not have blank nodes");
		}
		else { // Is VAR
			values[0]=pattern.substring(0, pattern.indexOf(' '));
		}
		
		if (pattern.startsWith("<")) { // Is URI
			pattern = pattern.substring(pattern.indexOf(' ') + 1);
			values[1] = pattern.substring(0, pattern.indexOf('>') + 1);
		}
		else { // Is VAR
			values[1]=pattern.substring(0, pattern.indexOf(' '));
		}

		
		// Parse object
		pattern = pattern.substring(values[1].length() + 1);
		if (pattern.startsWith("<")) { // URI
			values[2] = pattern.substring(0, pattern.indexOf('>') + 1);
		} else if (pattern.charAt(0) == '"') { // Literal
			values[2] = pattern.substring(0,
					pattern.substring(1).indexOf('"') + 2);
			pattern = pattern.substring(values[2].length(), pattern.length());
			values[2] += pattern.substring(0, pattern.indexOf(' '));
		} else if (pattern.charAt(0)== '_'){ // Bnode
			values[2] = "_:" + fileId
					+ pattern.substring(1, pattern.indexOf(' '));
		}
		else { // is VAR
			values[2]=pattern.substring(0, pattern.indexOf(' '));
		}
		
		return values;
		}*/
}