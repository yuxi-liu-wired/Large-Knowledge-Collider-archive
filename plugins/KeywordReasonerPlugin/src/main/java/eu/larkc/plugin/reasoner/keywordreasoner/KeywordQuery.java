package eu.larkc.plugin.reasoner.keywordreasoner;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class KeywordQuery {
	/**
	 * 
	 */

	public List<String> keywords = new ArrayList<String>();
	
	/* the absolute name of the file that contains the initial list of keywords */
	public String keywordsFile;
	
	/* the URL of the keywords file */
	public URL keywordsFileURL;
	
	/* the absolute name of the file that contains the ontology we want to derive keywords from */
	public String ontologyFile;
	
    public int sourceType;
	
	/* the threshold to use in order to discard candidate keywords when substring matching is used */
	public double threshold;
	
	/**
	 * indicates whether the Keyword Reasoner should consider ontology concepts whose names are a superstring
	 * of a given (input) keyword. If true only exact matching between input keywords and concepts is used. 
	 * */
	public boolean useSubstringMatching;
	
	/**
	 * indicates whether the Keyword Reasoner should consider only direct classes (true) or all descendants 
	 * (false) of an ontology concept when deriving keywords from an ontology. 
	 * */
	public boolean directSubclasses;
	
	public int semanticDistanceType;
	
	public String toString() {
		return keywords + "\n" + ontologyFile + "\n" + sourceType + "\n"
				+ semanticDistanceType + "\n" + directSubclasses + "\n" + threshold;
	}
}
