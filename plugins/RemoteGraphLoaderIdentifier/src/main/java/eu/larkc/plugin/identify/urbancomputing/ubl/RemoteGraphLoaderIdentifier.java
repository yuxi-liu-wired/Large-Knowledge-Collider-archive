package eu.larkc.plugin.identify.urbancomputing.ubl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;
import eu.larkc.urbancomputing.LarKCUtilities;

/**
 * An Identifier plugin to load a remote RDF graph (in a known location)
 * 
 * @author Daniele Dell'Aglio, Luka Bradesko, Emanuele Della Valle
 * Ported to 2.0 by Bas Groenewoud & Chris Dijkshoorn
 * Ported to 2.5 by Daniele Dell'Aglio
 */
public class RemoteGraphLoaderIdentifier extends Plugin {
	private static final URI graphNameProp = new URIImpl("http://larkc.cefriel.it/ontologies/urbancomputing#graphName");
	private static final URI locationProp = new URIImpl("http://larkc.cefriel.it/ontologies/urbancomputing#location");
	
//	//only first time when called, return results (anytime b.)
//	private boolean once = false;
	
	private URI graphName;
	private String location;

	public RemoteGraphLoaderIdentifier(URI pluginName) {
		super(pluginName);
	}
	
	@Override
	protected SetOfStatements cacheLookup(SetOfStatements input) {
		if(DataFactory.INSTANCE.createRdfStoreConnection().search(null, null, null, graphName, null).hasNext()){
			logger.debug("Cache enabled!");
			ArrayList<Statement> l=new ArrayList<Statement>();
			l.add(new StatementImpl(new BNodeImpl(UUID.randomUUID()+""), RDFConstants.DEFAULTOUTPUTNAME, graphName));

			return new SetOfStatementsImpl(l); 
		}
		else{
			logger.debug("Cache disabled!");
			return null;
		}
	}
	
	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		logger.info("RemoteGraphLoaderIdentifier, initialization");
		CloseableIterator<Statement> it = workflowDescription.getStatements();
		while(it.hasNext()){
			Statement s = it.next();
			if(s.getPredicate().equals(graphNameProp))
				graphName = new URIImpl(s.getObject().stringValue());
			if(s.getPredicate().equals(locationProp))
				location = s.getObject().stringValue();
		}
		logger.debug("Location of the graph: {}", location);
		logger.debug("Graph name: {}", graphName);

		logger.info("RemoteGraphLoaderIdentifier system, all green!");
	}

	public SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info("RemoteGraphLoaderIdentifier, fire!");
//		if (once)
//			return null;
//		once = true;

		RdfStoreConnection dataLayer = DataFactory.INSTANCE.createRdfStoreConnection();
		
		//TODO: implement a policy to refresh it sometime!
		//check if Data Layer contains the graph
//		if(dataLayer.executeSelect(new SPARQLQueryImpl("SELECT ?s ?p ?o FROM <"+graphName+"> WHERE{?s ?p ?o} LIMIT 1 ")).iterator().hasNext())
//		{
//			logger.info("Data Layer contains the graph {}!", graphName);
//			RdfGraph rg = dataLayer.createRdfGraph(graphName);
//			return rg;
//		}
//		else{
			RdfGraph ret = null;
			logger.info("Data Layer doesn't contain the graph {}!", graphName);
			//the Data Layer doesn't contain the graph -> load it
			
			try{
				URL uGraph = new URL(location);
				logger.info("Loading {} in the named graph {}", location, graphName);
				try {
					//change to more generic in 2.0
					LarKCUtilities.importRdfGraph(uGraph.openStream(), graphName, LarKCUtilities.RDFXML);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				 ret = (dataLayer.createRdfGraph(graphName));
			} catch (MalformedURLException e) {
				logger.error("Error while loading the RDF file!", e);
			}
//			return ret;	
			
			ArrayList<Statement> l=new ArrayList<Statement>();
			l.add(new StatementImpl(new BNodeImpl(UUID.randomUUID()+""), RDFConstants.DEFAULTOUTPUTNAME, graphName));

			return new SetOfStatementsImpl(l); 

//		}
	}

	public void shutdown() {
	}

}