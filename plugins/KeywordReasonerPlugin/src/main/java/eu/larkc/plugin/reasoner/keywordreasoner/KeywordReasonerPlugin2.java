package eu.larkc.plugin.reasoner.keywordreasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.DataSet;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.reasoner.keywordreasoner.utils.KRUtils;

import nl.vu.few.krr.larkc.keywordreasoner.Keyword;
import nl.vu.few.krr.larkc.keywordreasoner.KeywordReasoner;

public class KeywordReasonerPlugin2 extends Plugin {

	/* to keep the list of keywords extracted from the file that contains the initial list of keywords */
	private List<String> initialKeywords;
	
	/* the absolute name of the file that contains the initial list of keywords */
	private String keywordsFile = "";
	
	/* the URL of the keywords file */
	private URL keywordsFileURL = null;
	
	/* the absolute name of the file that contains the ontology we want to derive keywords from */
	private String ontologyFile = "";
	
	/* the URL of the file that contains the ontology */
	private URL ontologyFileURL = null;	
	
	/**
	 * the type of ontology we want to use. If 0 keywords will be derived from the Linked Life Data SPARQL 
	 * endpoint; if the value is 1 then the ontology specified by ontologyFile will be used instead. 
	 * */
	private int sourceType = 0;
	
	/* the threshold to use in order to discard candidate keywords when substring matching is used */
	private double threshold = 0.0;
	
	/**
	 * indicates whether the Keyword Reasoner should consider ontology concepts whose names are a superstring
	 * of a given (input) keyword. If true only exact matching between input keywords and concepts is used. 
	 * */
	private boolean useSubstringMatching = false;
	
	/**
	 * indicates whether the Keyword Reasoner should consider only direct classes (true) or all descendants 
	 * (false) of an ontology concept when deriving keywords from an ontology. 
	 * */
	private boolean directSubclasses = false;
	
	/**
	 * the metric used to measure the distance between two terms (an input keyword and an ontology concept).
	 * 1 = Normalized Google distance
	 * 2 = Levenshtein distance 
	 * */
	private int semanticDistanceType = 2;
	
	/* the keyword reasoner */
	private KeywordReasoner reasoner;
	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public KeywordReasonerPlugin2(URI pluginUri) {
		super(pluginUri);
		this.initialKeywords = new ArrayList<String>();
	}
	

	/**
	 * This method initializes the reasoner by reading the plugin's parameters from the workfow 
	 * description.
	 * 
	 * 
	 * @author Zhisheng Huang <huang@cs.vu.nl>
	 * @version 1.0
	 * */
	@Override
	protected void initialiseInternal(SetOfStatements pluginArguments) {
		logger.info("KeywordReasonerPlugin2 initialized.");	
	}
	
	
	/**
	 * This method derives a new set of keywords from a given set of keywords by calling the  
	 * <code>nl.vu.few.krr.larkc.keywordreasoner.KeywordReasoner</code>.
	 * 
	 * @param pluginArguments 
	 * 		A set of statements containing the input for this plug-in. The statements 
	 *		encode knowledge about the initial set of keywords, and other parameters required 
	 *		by the <code>nl.vu.few.krr.larkc.keywordreasoner.KeywordReasoner</code>.  
	 * 
	 * @return a set of statements containing the output of this plug-in which represent the new set of keywords.
	 * 
	 * @author Zhisheng Huang <huang@cs.vu.nl>
	 * @version 1.0
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info("KeywordReasonerPlugin2 is working...");
		
		 SPARQLQuery sparqlQuery = DataFactory.INSTANCE.createSPARQLQuery(input);

		    String original = sparqlQuery.toString();

		    final KeywordQuery query = new KeywordQuery();

		    Pattern p = Pattern.compile("([0-9])\\s+gwas:hasKeyword\\s+\"([^\"]+)\"");
		    Matcher m = p.matcher(original);

		    while(m.find()) {
		      query.keywords.add(m.group(2));
		   //   System.out.print("initial keyword:"+m.group(2));
		    }
		    
		    this.initialKeywords = query.keywords;

		    p = Pattern.compile("substringMatch\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()) {query.useSubstringMatching = Boolean.parseBoolean(m.group(1));
		    this.useSubstringMatching=query.useSubstringMatching;}
		    
		    
		    p = Pattern.compile("directSubclasses\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()) {query.directSubclasses = Boolean.parseBoolean(m.group(1));
		    this.directSubclasses=query.directSubclasses;}
		    
		    p = Pattern.compile("sourceType\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()){ query.sourceType = Integer.parseInt(m.group(1));
		    this.sourceType=query.sourceType;}
		    
		    p = Pattern.compile("threshold\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()) {query.threshold = Double.parseDouble(m.group(1));
		    this.threshold=query.threshold;}
		    
		    p = Pattern.compile("semanticDistance\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()) {query.semanticDistanceType = Integer.parseInt(m.group(1));
		    this.semanticDistanceType=query.semanticDistanceType;}
		  
		    
		    p = Pattern.compile("ontologyFile\\s+\"([^\"]+)\"");
		    m = p.matcher(original);

		    if(m.find()) {query.ontologyFile = m.group(1);
		    this.ontologyFile=query.ontologyFile;
			try {
				this.ontologyFileURL = new URL(this.ontologyFile);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		    }
		    
	
			/* creat a new instance of the KeywordReasoner class */
			this.reasoner = new KeywordReasoner();
			/* set the threshold */
			this.reasoner.setThreshold(query.threshold);
			if (query.sourceType != 0){
				/* load the given ontology into the reasoner */
				this.reasoner.loadOntology(this.ontologyFileURL.toExternalForm());
			}
			

			
			/* compute the set of keywords in group A */
			String keys = this.reasoner.getKeywordsInGroupA(this.initialKeywords, this.directSubclasses, this.useSubstringMatching, this.semanticDistanceType, this.sourceType);
		//	System.out.print("Keys ="+keys);
			
			logger.info("List of keywords (in JSON format): {}",keys);
			
			/* the set of new keywords derived from the input set of keywords and the given ontology(ies) */
			Collection<Keyword> keywords = new ArrayList<Keyword>();
			
			
			/* retrieve the set of derived keywords from the reasoner as a list of <code>nl.vu.few.krr.larkc.keywordreasoner.Keyword</code> objects */
			keywords = this.reasoner.getLastGeneratedKeywordsInGroupA();

		//	System.out.print("Keywords ="+keywords);
			
			
			
			/* free resources */
			this.reasoner.destroyReasoner();
		
	
		/* return the set of keywords as a <code>eu.larkc.core.data.SetOfStatements</code> */
		SetOfStatements results = KRUtils.wrapKeywordsIntoSetOfStatements2(keywords, new URIImpl("http://www.vu.nl/larkc/keywordreasoner/results"));
		
			
		
		   logger.info("KeywordReasoner2 done.");		
		
		return results;
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow deletion.
	 * Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
	}
	
}
