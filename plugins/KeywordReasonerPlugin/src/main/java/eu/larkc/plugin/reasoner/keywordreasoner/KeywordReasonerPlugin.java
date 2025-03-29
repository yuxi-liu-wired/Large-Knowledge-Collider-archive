package eu.larkc.plugin.reasoner.keywordreasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.reasoner.keywordreasoner.utils.KRUtils;

import nl.vu.few.krr.larkc.keywordreasoner.Keyword;
import nl.vu.few.krr.larkc.keywordreasoner.KeywordReasoner;

/**
 * This class implements the KeywordReasoner Plugin. The plugin takes as input a list of keywords (words) 
 * and extends/improves this set with new keywords derived from a set of ontologies. The plugin can use 
 * an ontology specified by the user upon plugin invocation to derive new keywords or, alternatively, it 
 * can derive keywords from the ontologies in the Linked Life Data (LLD). In both cases the user must 
 * specify which type of source the plugin must use (either type 1 for a single ontology or type 0 in the 
 * case of the LLD.
 * 
 * The plugin requires a series of parameters to be passed at invocation time. The full list of parameters 
 * is the folowing:
 * 
 * 	<code>urn:larkc.keywordreasoner.keywordsfile</code>:  the file that contains the list of keywords to be 
 * 																												extended (REQUIRED).
 * 	<code>urn:larkc.keywordreasoner.sourcetype</code>:  0 = use LLD (default); 
 * 																											1 = use ontology specified by the ontology 
 * 																											parameter (COMPULSORY).
 * 	<code>urn:larkc.keywordreasoner.ontology</code>: 	the file that contains the ontology (used only when 
 * 																										sourcetype = 1)
 * 	<code>urn:larkc.keywordreasoner.threshold</code>:	the threshold for discarding ontology concepts when 
 * 																										using sub-string matching (default = 0.0). 
 * 																										The lower the threshold the more restrictive the 
 * 																										selection of concepts and the less number of keywords 
 * 																										generated.
 *	<code>urn:larkc.keywordreasoner.substringmatch</code>:	true = use sub-string match (in addition to 
 *																													exact match); 
 *																													false = uses exact match only (default).
 * 	<code>urn:larkc.keywordreasoner.semanticdistance</code>: 	1 = Normalized Google distance; 
 * 																														2 = Levenshtein distance (default)
 * 	<code>urn:larkc.keywordreasoner.directsubclasses</code>:	If false (default) then all descendants of 
 * 																														an ontology concept are candidate keywords; 
 * 																														otherwise only direct sub-classes are 
 * 																														concsidered as candidate keywords.
 * 
 * The plugin's main task is accomplished by calling the services provided by the 
 * <code>nl.vu.few.krr.larkc.keywordreasoner.KeywordReasoner</code>, which is in charge of deriving the 
 * set of keywords. In other words, the plugin wraps the latter providing the required input arguments and 
 * processing its output.    
 * 
 * For more details see <code>http://wiki.larkc.eu/LarkcPlugins/KeywordReasoner</code>
 * 
 * @author Gaston Tagni <g.e.tagni@vu.nl>
 * @version 1.0
 * 
 */
public class KeywordReasonerPlugin extends Plugin {

	/* to keep the list of keywords extracted from the file that contains the initial list of keywords */
	protected List<String> initialKeywords;
	
	/* the absolute name of the file that contains the initial list of keywords */
	protected String keywordsFile = "";
	
	/* the URL of the keywords file */
	private URL keywordsFileURL = null;
	
	/* the absolute name of the file that contains the ontology we want to derive keywords from */
	protected String ontologyFile = "";
	
	/* the URL of the file that contains the ontology */
	protected URL ontologyFileURL = null;	
	
	/**
	 * the type of ontology we want to use. If 0 keywords will be derived from the Linked Life Data SPARQL 
	 * endpoint; if the value is 1 then the ontology specified by ontologyFile will be used instead. 
	 * */
	protected int sourceType = 0;
	
	/* the threshold to use in order to discard candidate keywords when substring matching is used */
	protected double threshold = 0.0;
	
	/**
	 * indicates whether the Keyword Reasoner should consider ontology concepts whose names are a superstring
	 * of a given (input) keyword. If true only exact matching between input keywords and concepts is used. 
	 * */
	protected boolean useSubstringMatching = false;
	
	/**
	 * indicates whether the Keyword Reasoner should consider only direct classes (true) or all descendants 
	 * (false) of an ontology concept when deriving keywords from an ontology. 
	 * */
	protected boolean directSubclasses = false;
	
	/**
	 * the metric used to measure the distance between two terms (an input keyword and an ontology concept).
	 * 1 = Normalized Google distance
	 * 2 = Levenshtein distance 
	 * */
	protected int semanticDistanceType = 2;
	
	/* the keyword reasoner */
	protected KeywordReasoner reasoner;
	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public KeywordReasonerPlugin(URI pluginUri) {
		super(pluginUri);
		this.initialKeywords = new ArrayList<String>();
	}
	

	/**
	 * This method initializes the reasoner by reading the plugin's parameters from the workfow 
	 * description.
	 * 
	 * @param workflowDescription 
	 * 				Contains the plugin's parameters. Valid parameters are:
	 *
	 * 				<code>urn:larkc.keywordreasoner.keywordsfile</code>: the file that contains the list of keywords to process (COMPULSORY)
	 * 				<code>urn:larkc.keywordreasoner.sourcetype</code>: 0 = use LLD (default); 1 = use ontology in specified file (COMPULSORY)
	 *				<code>urn:larkc.keywordreasoner.ontology</code>: the file that contains the ontology to use (when sourcetype = 1)
	 * 				<code>urn:larkc.keywordreasoner.threshold</code>: the threshold for discarding ontology concepts when using sub-string matching (default = 0.0). 
	 * 																													The lower the threshold the more restrictive the selection of concepts and the less number 
	 * 																													of keywords generated.
	 * 				<code>urn:larkc.keywordreasoner.substringmatch</code>: true = use sub-string match; false = don't use it (default).
	 * 				<code>urn:larkc.keywordreasoner.semanticdistance</code>: 1 = Normalized Google distance; 2 = Levenshtein distance (default)
	 * 				<code>urn:larkc.keywordreasoner.directsubclasses</code>: If false (default) then all descendants of a concept are candidate keywords; otherwise 
	 * 			 																													 only direct sub-classes are concsidered as candidate keywords.
	 * 
	 * 				For example, a parameter can be specified as follows:
	 * 																		<code>_:pp1 <urn:larkc.keywordreasoner.sourcetype> "0" .</code>
	 * 	 
	 * @author Gaston Tagni <g.e.tagni@vu.nl>
	 * @version 1.0
	 * */
	@Override
	protected void initialiseInternal(SetOfStatements pluginArguments) {
		CloseableIterator<Statement> statements = pluginArguments.getStatements();
		while (statements.hasNext()){
			Statement st = statements.next();
			String predicateName = st.getPredicate().toString();
			if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_SOURCE_TYPE) == 0){
				this.sourceType = Integer.parseInt(st.getObject().stringValue());
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_ONTOLOGY) == 0){
				this.ontologyFile = st.getObject().stringValue();
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_DIRECT_SUBCLASSES) == 0){
				this.directSubclasses = Boolean.parseBoolean(st.getObject().stringValue());
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_THRESHOLD) == 0){
				this.threshold = Double.parseDouble(st.getObject().stringValue());
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_SUBSTRINGMATCH) == 0){
				this.useSubstringMatching = Boolean.parseBoolean(st.getObject().stringValue());
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_SEMANTIC_DISTANCE) == 0){
				this.semanticDistanceType = Integer.parseInt(st.getObject().stringValue());
			}else if (predicateName.compareToIgnoreCase(KRConstants.PARAM_URI_KEYWORDS_FILE) == 0){
				this.keywordsFile = st.getObject().stringValue();
			}
		}
		logger.info("KeywordReasonerPlugin initialized.");	
	}
	
	/**
	 * This method validates the plugin's parameters. 
	 * 
	 * @return true if all the parameters are valid; false otherwise
	 * 
	 * @author Gaston Tagni <g.e.tagni@vu.nl>
	 * @version 1.0
	 * */
	private boolean validateParameters(){			
		if ( !this.keywordsFile.isEmpty() ){
			try {
				this.keywordsFileURL = new URL(this.keywordsFile);
				/* retrieve the list of initial keywords from the given file */
				this.initialKeywords = KRUtils.readKeywordsFile(this.keywordsFileURL);			
				if (this.sourceType != 0){
					this.ontologyFileURL = new URL(this.ontologyFile);
					if ( !this.ontologyFileURL.toExternalForm().isEmpty() ){
						return true;
					}else{
						logger.error("Ontology file {} not found.",this.ontologyFile);
						return false;
					}
				}
				return true;				
			} catch (MalformedURLException e) {
				logger.error("An error has occurred while processing the given file name.",e);
				return false;
			}
		}else{
			logger.error("Keywords file {} not found.",this.keywordsFile);
			return false;
		}
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
	 * @author Gaston Tagni <g.e.tagni@vu.nl>
	 * @version 1.0
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements pluginArguments) {
		logger.info("KeywordReasonerPlugin is working...");
		
		/* the set of new keywords derived from the input set of keywords and the given ontology(ies) */
		Collection<Keyword> keywords = new ArrayList<Keyword>();
		
		/* read and validate the plugin's parameters */
		if (validateParameters()){
			/* creat a new instance of the KeywordReasoner class */
			this.reasoner = new KeywordReasoner();
			/* set the threshold */
			this.reasoner.setThreshold(this.threshold);
			if (this.sourceType != 0){
				/* load the given ontology into the reasoner */
				this.reasoner.loadOntology(this.ontologyFileURL.toExternalForm());
			}
			
			/* compute the set of keywords in group A */
			String keys = this.reasoner.getKeywordsInGroupA(this.initialKeywords, this.directSubclasses, this.useSubstringMatching, this.semanticDistanceType, this.sourceType);
			logger.info("List of keywords (in JSON format): {}",keys);
			
			/* retrieve the set of derived keywords from the reasoner as a list of <code>nl.vu.few.krr.larkc.keywordreasoner.Keyword</code> objects */
			keywords = this.reasoner.getLastGeneratedKeywordsInGroupA();

			/* free resources */
			this.reasoner.destroyReasoner();
		}
	
		/* return the set of keywords as a <code>eu.larkc.core.data.SetOfStatements</code> */
		SetOfStatements results = KRUtils.wrapKeywordsIntoSetOfStatements2(keywords, new URIImpl("http://www.vu.nl/larkc/keywordreasoner/results"));
		logger.info("KeywordReasoner done.");		
		
		return results;
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow deletion.
	 * Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {}
}