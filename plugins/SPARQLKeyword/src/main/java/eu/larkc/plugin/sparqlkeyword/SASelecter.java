package eu.larkc.plugin.sparqlkeyword;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;

public class SASelecter extends SPARQLKeywordPlugin {
	public static final URI PARAM_CUDA_MODE = 
		new URIImpl(WorkflowDescriptionPredicates.LARKC + "cudaMode");
	private static final String PRIMING_NS = "http://www.ontotext.com/owlim/RDFPriming#";
	
	private String[] translateParameters = new String[]{
			"cudaMode",
			"decayFactor",
			"initialActivation",
			"firingThreshold",
			"filterThreshold",
			"cycles",
			"maxNodesFiredPerCycle",
	};
	
	private boolean cudaMode = false;
	private HashMap<String,String> parameters = new HashMap<String,String>();

	public SASelecter(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		super.initialiseInternal(workflowDescription);
		
		// check if CUDA mode is desired
		String cudaParam = retrieveParameter(PARAM_CUDA_MODE, workflowDescription);
		if (cudaParam != null && Boolean.parseBoolean(cudaParam))
			cudaMode = true;
		
		// translate SA parameters
		for (String param : translateParameters) {
			String value = retrieveParameter(
					new URIImpl(WorkflowDescriptionPredicates.LARKC + param),
					workflowDescription);
			if (value != null) {
				parameters.put(param, value);
			}
		}
	}
	
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		String query = extractQuery(input);
		if (query == null)
			return input;
		
		// enable the spreading activation
		initSA(input);
		
		// configure parameters
		for (String param : parameters.keySet()) {
			setParam(input, param, parameters.get(param));
		}

		// initialize the process by executing the input query
		
		askQuery(input, query);
		
		// spread activation
		spreadActivation(input);
		
		// select whatever is currently active (the primed part of graph)
		SetOfStatements resultSet = 
			getConnection().executeConstruct(
					new SPARQLQueryImpl("construct {?s ?p ?o} {?s ?p ?o}"));
		
		// disable the spreading activation
		exitSA(input);
		
		return resultSet;
	}
		
	private void setParam(SetOfStatements set, String param, String value) {
		askQuery(set, "ASK {<" + PRIMING_NS + param + 
				"> <" + PRIMING_NS + "setParam> \"" + value + "\" }");
	}
	
	private void toggleFeature(SetOfStatements set, String name, boolean state) {
		askQuery(set, "ASK { _:b1 <" + PRIMING_NS + name + 
				"> \"" + state + "\" }");
	}
	
	private void initSA(SetOfStatements set) {
		if (cudaMode) {
			toggleFeature(set, "enableSpreadingCUDA", true);
		} else {
			toggleFeature(set, "enableSpreading", true);
		}
	}
	
	private void exitSA(SetOfStatements set) {
		toggleFeature(set, "enableSpreading", false);
	}
	
	private void spreadActivation(SetOfStatements set) {
		toggleFeature(set, "spreadActivation", true);
	}
	
	private boolean askQuery(SetOfStatements set, String query) {
		return getConnection().executeAsk(new SPARQLQueryImpl(query));
	}
	
	private String extractQuery(SetOfStatements input) {
		SPARQLQuery inputQuery = getQuery(input);
		if (inputQuery == null)
			return null;
		
		String query = inputQuery.toString();
		
		// transform the query verb from SELECT to ASK
		Pattern p = Pattern.compile(
				"^\\s*select\\s+.*(\\{.*\\})[^}]*$", 
				Pattern.CASE_INSENSITIVE);
		String askQuery = p.matcher(query).replaceFirst("ASK $1");
		
		return askQuery; 
	}
}
