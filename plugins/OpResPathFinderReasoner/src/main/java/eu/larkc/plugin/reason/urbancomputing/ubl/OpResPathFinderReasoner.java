package eu.larkc.plugin.reason.urbancomputing.ubl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

import eu.larkc.core.data.BooleanInformationSetImpl;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.DataSet;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.util.SPARQLQueryExecutor;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.reason.urbancomputing.ubl.support.GraphProcessor;
import eu.larkc.plugin.reason.urbancomputing.ubl.support.NamedWeightedEdge;
import eu.larkc.plugin.reason.urbancomputing.ubl.support.ProcessorShortestPath;

/**
 * Test shortest path reasoner. It reads DataSet and converts graps into the
 * org.jgrapht.Graph, which is the input for Djikstra shortest path algorithm
 * 
 * @author Luka Bradesko, Daniele Dell'Aglio
 * Ported to 2.0 by Bas Groenewoud and Chris Dijkshoorn
 */
public class OpResPathFinderReasoner extends Plugin {
	
	private static final URI QUERY_NAME = new URIImpl("http://someData#query");
	private static final URI DATA_NAME = new URIImpl("http://someData#data");

	protected URI queryGraph;
	protected URI dataGraph;
	
	private GraphProcessor gp;
	private String start, goal;
	//only first time when called, return results (anytime b.)
	@SuppressWarnings("unused")
	private boolean once = false;

	public OpResPathFinderReasoner(URI pluginName) {
		super(pluginName);
	}
	
	@Override
	protected void shutdownInternal() {};

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		logger.info("OpResPathFinderReasoner, initialization");
		queryGraph=getNamedGraphFromParameters(workflowDescription, QUERY_NAME);
		dataGraph=getNamedGraphFromParameters(workflowDescription, DATA_NAME);
		logger.info("OpResPathFinderReasoner system, all green!");
	}

	@SuppressWarnings("unchecked")
	public SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info("OpResPathFinderReasoner, fire!");

		//load all statements into the rdf graph data to create an RDFGraph out of the input
		final RdfStoreConnection connection = DataFactory.INSTANCE.createRdfStoreConnection();
		
		RdfGraph data = null;
		CloseableIterator<Statement> statements = input.getStatements();
		
		while(statements.hasNext()){
			Statement st = statements.next();
			Resource s = st.getSubject();
			URI p = st.getPredicate();
			Value o = st.getObject();
			connection.addStatement(s, p, o, dataGraph);	
		}

		data =  connection.createRdfGraph(dataGraph);
		
		SPARQLQuery query=null;
		
		if (queryGraph != null) {
			query = DataFactory.INSTANCE.createSPARQLQuery(input, queryGraph);
		}
		
		// Did not find a query, search the rest of the arguments
		if (query == null) {
			logger.debug("did not find a query!!");
			query = DataFactory.INSTANCE.createSPARQLQuery(input);
		}
		
		if(query != null) {
			logger.debug("Got query: " + query.toString());
		
			if (query.isConstruct()) {
				return new SPARQLQueryExecutor().executeConstruct(query, input);
			}
			else if(query.isAsk()) {
				new BooleanInformationSetImpl(new SPARQLQueryExecutor().executeAsk(query, input));
			}
			else if(query.isDescribe()) {
				return new SPARQLQueryExecutor().executeConstruct(query, input);
			}
			else if(query.isSelect()) {
				List<URI> lit = new ArrayList<URI>();
				
				//extracting the name of the graph
				CloseableIterator<Statement> it = input.getStatements();
				
				while(it.hasNext()){
					Statement s = it.next();
					
					if(s.getPredicate().equals(RDFConstants.DEFAULTOUTPUTNAME)){
						logger.info("Adding {} the the graph set", s.getObject().stringValue());
						lit.add((URI)s.getObject());
					}
				}
				
				// Set the start and the goal nodes:
				start = "";
				goal = "";
				SPARQLQueryImpl q = (SPARQLQueryImpl) query;

				StatementPatternCollector collector = new StatementPatternCollector();
				q.getParsedQuery().getTupleExpr().visit(collector);
				for (StatementPattern sp : collector.getStatementPatterns()) {
					if (sp.getPredicateVar().getValue().toString().equals(
							"http://www.linkingurbandata.org/onto/ama/pathFrom")) {
						start = sp.getObjectVar().getValue().toString();
					}
					if (sp.getPredicateVar().getValue().toString().equals(
							"http://www.linkingurbandata.org/onto/ama/pathTo")) {
						goal = sp.getObjectVar().getValue().toString();
					}
				}
				logger.info("The start and goal nodes are <{}>,<{}>", start, goal);

				// fill the graph with the data contained in RDF models
				int i = 0;

				Graph<String, NamedWeightedEdge> graph = new DefaultDirectedWeightedGraph<String, NamedWeightedEdge>(
						NamedWeightedEdge.class);

				for (URI g : lit) {
					ArrayList<Resource> nodeList = new ArrayList<Resource>();
					// RdfGraph g = ds.getNamedGraphs().iterator().next();//TODO:
					// for now it takes only one graph and wiuthout checking if data
					// exists

					RdfStoreConnection con = DataFactory.INSTANCE
							.createRdfStoreConnection();
					CloseableIterator<Statement> nodes = con
							.search(
									null,
									RDF.TYPE,
									new URIImpl(
											"http://www.linkingurbandata.org/onto/ama/Node"),
											g, null);
					while (nodes.hasNext()) {
						Statement nodeSt = nodes.next();
						Resource r = nodeSt.getSubject();
						nodeList.add(r);
						graph.addVertex(r.stringValue());
					}
					nodes.close();

					logger.debug("End of node import");

					for (Resource from : nodeList) {
						CloseableIterator<Statement> edges = con
								.search(
										null,
										new URIImpl(
												"http://www.linkingurbandata.org/onto/ama/lFrom"),
												from, g, null);

						while (edges.hasNext()) {
							Statement edgeSt = edges.next();
							Resource e = edgeSt.getSubject();

							Value to = con.search(
											e,
											new URIImpl(
													"http://www.linkingurbandata.org/onto/ama/lTo"),
													null, g, null).next().getObject();

							NamedWeightedEdge edge = graph.addEdge(from
									.stringValue(), to.stringValue());
							if (edge != null) {
								edge.setLabel(e.stringValue());

								String w = con
										.search(
												e,
												new URIImpl(
														"http://www.linkingurbandata.org/onto/ama/length"),
														null, g, null).next()
														.getObject().stringValue();
								Double weight;
								if (w.indexOf("\"^") >= 0)
									weight = Double.parseDouble(w.substring(1, w
											.indexOf("\"^")));
								else
									weight = Double.parseDouble(w);
								((AbstractBaseGraph<String, NamedWeightedEdge>) graph)
								.setEdgeWeight(edge, weight);
								i++;
							}
						}
						edges.close();

					}
				}
				logger.debug("End of link import");
				// search for all nodes

				// end of Conversion

				logger.debug("Reasoning....");
				try {
					gp = new ProcessorShortestPath(graph, start, goal);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// gp.addResultListener(this);
				gp.process();

				logger.debug("END OF SEARCH");
				VariableBinding result = gp.getResult();
				
				SetOfStatements temp = new SetOfStatementsImpl();
				return result.toRDF(temp);
			}
		}
		else {
				//TODO: Throw exception
				logger.debug("No query in the input!");
				return input;
		}
		return input;
	} 

	public void shutdown() {

	}

}