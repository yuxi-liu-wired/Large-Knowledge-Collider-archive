package eu.larkc.plugin.sparqlkeyword;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SPARQLEndpoint;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.util.SetOfStatementsMerger;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;

public abstract class SPARQLKeywordPlugin extends Plugin {

	// --- C O N S T A N T S ------------------------------------------------
	public static final URI PARAM_GRAPH = 
			new URIImpl(WorkflowDescriptionPredicates.LARKC + "graph");
	public static final URI PARAM_ENDPOINT = 
			new URIImpl(WorkflowDescriptionPredicates.LARKC + "endpoint");

	
	private RdfStoreConnection rdfStoreConnection = null;
	
	public SPARQLKeywordPlugin(URI pluginName) {
		super(pluginName);
	}

	protected SPARQLQuery getQuery(SetOfStatements input) {
		CloseableIterator<Statement> iter = input.getStatements();
		while (iter.hasNext()) {
			Statement st = iter.next();
			if (st.getPredicate().equals(WorkflowDescriptionPredicates.PLUGIN_PARAMETER_QUERY)) {
				return DataFactory.INSTANCE.createSPARQLQuery(st.getObject().stringValue());
			}
		}
		return DataFactory.INSTANCE.createSPARQLQuery(input);		
	}
	
	protected RdfStoreConnection getConnection() {
		if (rdfStoreConnection == null) {
			rdfStoreConnection = DataFactory.INSTANCE.createRdfStoreConnection();
		}
		return rdfStoreConnection;
	}
	
	protected String retrieveParameter(URI param) {
		return retrieveParameter(param, getPluginParameters());
	}
	
	protected String retrieveParameter(URI param, SetOfStatements pluginParams) {
		CloseableIterator<Statement> params = pluginParams.getStatements();
		while (params.hasNext()) {
			Statement stmt = params.next();
			if (stmt.getPredicate().equals(param)) {
				Value value = stmt.getObject();
				return value.stringValue();
			}
		}
		return null;
	}
	
	protected SPARQLEndpoint getInputEndpoint() {
		SetOfStatements params = getPluginParameters();
		
		// see if there is a parameter defining graph input
		CloseableIterator<Statement> iter = params.getStatements();
		while (iter.hasNext()) {
			Statement statement = iter.next();
			if (!(statement.getObject() instanceof URI))
				continue;
			URI paramValue = (URI) statement.getObject();
			URI paramName = statement.getPredicate();
			
			// has the user passed a graph parameter
			if (paramName.equals(PARAM_GRAPH)) {
				RdfGraph graph = DataFactory.INSTANCE.createRemoteRdfGraph(paramValue);
				SetOfStatementsMerger merger = new SetOfStatementsMerger();
				merger.add(graph);
				return merger.getRdfStoreConnection();
			}
			// has the user passed an endpoint parameter
			if (paramName.equals(PARAM_ENDPOINT)) {
				return DataFactory.INSTANCE.createSPARQLEndpoint(paramValue);
			}
		}
		
		// work locally otherwise
		return DataFactory.INSTANCE.createRdfStoreConnection();
	}
	
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {

	}

	@Override
	protected void shutdownInternal() {
		
	}
}
