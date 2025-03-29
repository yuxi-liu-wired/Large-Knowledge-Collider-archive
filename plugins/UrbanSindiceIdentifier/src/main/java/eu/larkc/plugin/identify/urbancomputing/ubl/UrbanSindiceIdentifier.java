package eu.larkc.plugin.identify.urbancomputing.ubl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.identify.sindice.SindiceTriplePatternIdentifier;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.TriplePattern;
import eu.larkc.core.query.TriplePatternQuery;
import eu.larkc.core.query.TriplePatternQueryImpl;
import eu.larkc.core.util.HttpUtils;
import eu.larkc.core.util.RDFConstants;


/**
 * @author Irene Celino, Daniele Dell'Aglio, Emanuele Della Valle 
 * based on AbstractSindiceIdentifier and SindiceTriplePatternIdentifier 
 * Ported to 2.0 platform by Bas Groenewoud & Chris Dijkshoorn
 */
public class UrbanSindiceIdentifier extends SindiceTriplePatternIdentifier {

	//only first time when called, return results (anytime b.)
	private boolean once = false;
	
	protected static Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	public UrbanSindiceIdentifier(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected void shutdownInternal() {};

	public SetOfStatements invokeInternal(SetOfStatements input) {
		
		logger.debug("Create triples");
		
		if (once) { return null; }
		once = true;
		
		List<Query> allTriplePatterns = new ArrayList<Query>();

		// risorse da mettere nei triple patterns
		Resource monumentVariable = null; 
		URI skosSubject = new URIImpl(
				"http://www.w3.org/2004/02/skos/core#subject");
		Value dbpediaAttraction = new URIImpl(
				"http://dbpedia.org/resource/Category:Visitor_attractions_in_Milan");
		Value dbpediaChurch = new URIImpl(
				"http://dbpedia.org/resource/Category:Churches_in_Milan");
		/*Value dbpediaGarden = new URIImpl(
				"http://dbpedia.org/resource/Category:Gardens_in_Milan");
		Value dbpediaMuseum = new URIImpl(
				"http://dbpedia.org/resource/Category:Museums_in_Milan");
		Value dbpediaPiazza = new URIImpl(
				"http://dbpedia.org/resource/Category:Piazzas_in_Milan");*/
		URI georssPoint = new URIImpl("http://www.georss.org/georss/point");
		Value geoPointVariable = null; 
		
		logger.debug("Created DBPedia links");
		

		//Create triples for monuments (dbpediaAtrraction)
		Collection<TriplePattern> monumentPattern = new HashSet<TriplePattern>();
		monumentPattern.add(new TriplePattern(
				monumentVariable, skosSubject, dbpediaAttraction));
		monumentPattern.add(new TriplePattern(
				monumentVariable, georssPoint, geoPointVariable));
		TriplePatternQuery singleResult = new TriplePatternQueryImpl(monumentPattern);

		
		allTriplePatterns.add(singleResult);
		logger.debug("Added monument patterns");


		//Create triples for churches (dbpediaChurch
		Collection<TriplePattern> churchPattern = new HashSet<TriplePattern>();
		churchPattern.add(new TriplePattern(
				monumentVariable, skosSubject, dbpediaChurch));
		churchPattern.add(new TriplePattern(
				monumentVariable, georssPoint, geoPointVariable));
		singleResult = new TriplePatternQueryImpl(churchPattern);
		allTriplePatterns.add(singleResult);
		
		
		logger.debug("Added church patterns");

		List<RdfGraph> results = new ArrayList<RdfGraph>();

		logger.debug("Start Querying...");
		
		//Retrieve data from sindice
		for (Query singleQuery : allTriplePatterns) {
			logger.debug("Gets into for loop");
			logger.info("+++ processing query "+singleQuery.toString());
			try {
				for (int page = 1; page < NUM_PAGES_PER_REQUEST; ++page) {
					//Creation of invokement of Sindicie REST service  
					String url = makeSindiceQueryString(
							singleQuery.toRDF(),page);
					logger.info("+++ Sindice query: "+url);
					// Invoke the service
					String response = HttpUtils.sendGetRequest(url);
					//Result per page
					Collection<RdfGraph> pageResults = parseResults(response);
					logger.info("+++ Found results: "+pageResults.size());
					//  Add results for selecter
					if (pageResults.size() == 0) {
						break;
					} else {
						results.addAll(pageResults);
					}
				}
			} catch (Exception e) {
			}
		}
		if (results.isEmpty()) {
			return null;
		}
		
		logger.info("Returning " + results.size() + " results");
		
//		List<Statement> statements=new ArrayList<Statement>(10000);
		RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection();
		for (RdfGraph is : results) {
			CloseableIterator<Statement> iter=is.getStatements();
			while (iter.hasNext()){
				Statement s = iter.next();
				con.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), outputGraphName);
				logger.debug("Adding {} to {}",s ,outputGraphName);
//				statements.add(iter.next());
			}
		}
		logger.debug("return results");
		ArrayList<Statement> l=new ArrayList<Statement>();
		l.add(new StatementImpl(new BNodeImpl(UUID.randomUUID()+""), RDFConstants.DEFAULTOUTPUTNAME, outputGraphName));

		return new SetOfStatementsImpl(l); 
//		return DataFactory.INSTANCE.createRdfGraph(statements,outputGraphName);
	}

	public void shutdown() {
	}
}