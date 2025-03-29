//FIXME: Include licensing header
package eu.larkc.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.DataFactoryImpl;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.qos.QoSInformation;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;
import gate.ri.RandomIndexing;
import gate.ri.airhead.RandomIndexingAH;

public class RISearchPlugin extends Plugin {

	public RISearchPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	private static Logger logger = LoggerFactory.getLogger(RISearchPlugin.class);
	
	protected String filePath = "/tmp";
	protected int numberOfSimilarWords = 10;

	private SetOfStatements searchKeywords(Query theQuery) {
		
		if(theQuery instanceof SPARQLQuery) {
			// extract keywords from input query
			Collection<String> keywords = SparqlToKeywords.extractKeywords(theQuery.toString());
			
			try {
				for (String word : keywords) {
					// for each keyword we find similar ones according to RI
					// (similar URIs as well)
					RandomIndexing randomIndexing = new RandomIndexingAH();
					randomIndexing.setFilePath(filePath);
					randomIndexing.setNumberOfSimilarWords(numberOfSimilarWords);
					
					Set<String> similarWords = randomIndexing.findSimilarTerms(
							randomIndexing.getFilePath(), word, randomIndexing.getNumberOfSimilarWords(),
							true);

					if(similarWords != null) {
						List<Statement> allKeywords = new ArrayList<Statement>();
						
						Iterator<String> it = similarWords.iterator();
												
						while(it.hasNext()) {
							String simWord = it.next();
							Statement stmt = new StatementImpl(new BNodeImpl(""), new URIImpl(WorkflowDescriptionPredicates.LARKC), new LiteralImpl(simWord));
							
							allKeywords.add(stmt);
							
							if(logger.isDebugEnabled()) {
								logger.debug(simWord);
								logger.debug(stmt.toString());
							}
						}
						return new SetOfStatementsImpl(allKeywords);
					}
					
					return null;
				}
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
		}
		else {
			//TODO: Throw exception
		}
		
		return null;
	}

	public URI getIdentifier() {
		return new URIImpl("urn:" + this.getClass().getName());
	}

	public QoSInformation getQoSInformation() {
		return new QoSInformation() {};
	}
	
	//For internal testing purposes only
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		logger.info("Starting RI Search...");
		
		RISearchPlugin ri = new RISearchPlugin(new URIImpl("urn:eu.larkc.plugin.RISearchPlugin"));
		ri.filePath = "/HLRS/LarkC/WP5/development/trunk/sample_data/lld1-docs-vectors.sspace";
	
		String sparqlQuery = "SELECT ?s ?p ?o" +
				" WHERE {" +
				"{ ?s ?p ?o . ?s ?p \"asthma\"} }";
		
		logger.info("Using '"+sparqlQuery+"' as input");
		
		SPARQLQuery inputQuery = DataFactoryImpl.INSTANCE.createSPARQLQuery(sparqlQuery);
		
		logger.info("Beginn search...");
		
		SetOfStatements keywords = ri.invoke(inputQuery.toRDF());
		
		if(keywords != null) {
			CloseableIterator<Statement> it = keywords.getStatements();
			
			while (it.hasNext())
				logger.info(it.next().toString());
		}
		
		logger.info("Finished search in " + (System.currentTimeMillis()-startTime) + " ms");
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		if(workflowDescription != null) {
			CloseableIterator<Statement> it = workflowDescription.getStatements();
			
			while (it.hasNext()) {	
				Statement stmt = it.next();
				
				if(stmt != null) {
					if(logger.isDebugEnabled())
						logger.debug(stmt.toString());
					
					if(stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC+"inputPath"))
						filePath = stmt.getObject().stringValue();
					else if(stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC+"nrOfWords"))
						numberOfSimilarWords = Integer.parseInt(stmt.getObject().stringValue());
				}
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Inputpath: " + filePath);
			logger.debug("NrOfWords: " + numberOfSimilarWords);
		}
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// TODO Auto-generated method stub
		return searchKeywords(DataFactory.INSTANCE.createSPARQLQuery(input));
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
}
