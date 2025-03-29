package eu.larkc.plugin.sparqlkeyword;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.DataLayerService;
import eu.larkc.core.data.DataSet;
import eu.larkc.core.data.HTTPRemoteGraph;
import eu.larkc.core.data.LabelledGroupOfStatements;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SPARQLEndpoint;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.data.util.SetOfStatementsMerger;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;

public class BaseLineFTSelecter extends SPARQLKeywordPlugin {
	// --- C O N S T A N T S ------------------------------------------------
	public static final URI PARAM_MAX_MOLECULES = 
			new URIImpl(WorkflowDescriptionPredicates.LARKC + "maxMolecules");
	
	// --- P A R A M E T R S ------------------------------------------------
	private int maxMolecules = Integer.MAX_VALUE;
	
	// --- C L A S S  F I E L D S -------------------------------------------
	private int selectedMolecules = 0;
	private int selectedStatements = 0;
	

	public BaseLineFTSelecter(URI pluginName) {
		super(pluginName);
	}
	
	protected int getMaxMolecules() {
		return maxMolecules;
	}
	
	protected void setMaxMolecules(int limit) {
		maxMolecules = limit;
	}
	
	protected void setSelectedMolecules(int count) {
		selectedMolecules = count;
	}
	
	protected int getSelectedMolecules() {
		return selectedMolecules;
	}
	
	protected int getSelectedStatements() {
		return selectedStatements;
	}

	protected void setSelectedStatements(int count) {
		selectedStatements = count;
	}

	protected SPARQLEndpoint getEndpoint(SetOfStatements rdf) {
		if (rdf instanceof DataSet) {
			return ((DataSet)rdf).getSPARQLEndpoint();
		} else if (rdf instanceof LabelledGroupOfStatements) {
			return ((LabelledGroupOfStatements)rdf).getRdfStoreConnection();
		} else {
			return null;
		}
	}

	private void locateMolecules(Value node, SPARQLEndpoint endpoint,
			LabelledGroupOfStatements sos, Set<String> visited, 
			AtomicInteger limit, AtomicInteger count) {
		// check if limit was reached
		if (limit.get() <= 0)
			return;
		if (visited == null) {
			visited = new HashSet<String>();
		}
		// check if this is a blank node that we might have visited already
		if (node instanceof BNode) {
			String id = ((BNode)node).getID();
			if (visited.contains(id)) return;
			// this is a fresh blank node we haven't yet visited
			visited.add(id);
		}
		// search all incoming edges for this node
		Iterator<Statement> iter = filterEndpoint(endpoint, null, null, node);
		while (iter.hasNext()) {
			Statement st = iter.next();
			Resource sourceNode = st.getSubject();
			if (sourceNode instanceof BNode) { 
				// dive recursively into current blank node
				locateMolecules(sourceNode, endpoint, sos, visited, limit, count);
			} else {
				String id = sourceNode.stringValue();
				if (visited.contains(id)) continue;
				visited.add(id);
				if (limit.get() <= 0) continue;
				limit.decrementAndGet();
				// this is not a blank, it should be an URI - select its molecule
				selectMolecule(sourceNode, endpoint, sos, null, count);
			}
		}
	}

	private void selectMolecule(Resource node, SPARQLEndpoint endpoint,
			LabelledGroupOfStatements sos, Set<String> visited, AtomicInteger count) {
		if (visited == null) {
			visited = new HashSet<String>();
		}
		// check if this is a blank node that we might have visited already
		if (node instanceof BNode) {
			String id = ((BNode)node).getID();
			if (visited.contains(id)) return;
			// this is a fresh blank node we haven't yet visited
			visited.add(id);
		}
		// search all outgoing edges for this node
		Iterator<Statement> iter = filterEndpoint(endpoint, node, null, null);
		while (iter.hasNext()) {
			Statement st = iter.next();
			Resource targetNode = st.getSubject();
			if (targetNode instanceof BNode) { 
				// dive recursively into current blank node
				selectMolecule(targetNode, endpoint, sos, visited, count);
			}
			count.incrementAndGet();
			// select statement as part of the molecule
			sos.includeStatement(st);
		}
	}

	private Iterator<Statement> filterEndpoint(SPARQLEndpoint endpoint,
			Resource subj, URI pred, Value obj) {
		if (endpoint instanceof RdfStoreConnection) {
			// filtering a store connection is easier than a general endpoint
			try {
				return ((RdfStoreConnection)endpoint).search(subj, pred, obj, null, null);
			} catch (Exception ex) {
				ex.printStackTrace(); 
			}
		} else {
			// TODO: create and execute a SPARQL query to filter endpoint
		}
		// return empty iterator
		return new Iterator<Statement>() {
			public boolean hasNext() {
				return false;
			}
			public Statement next() {
				return null;
			}
			public void remove() {
			}
		};
	}
	
	public static Collection<String> extractKeywords(String queryString) {
		final Collection<String> keywords = new LinkedList<String>();

		SPARQLParser queryParser = new SPARQLParser();
		ParsedQuery query;
		try {
			query = queryParser.parseQuery(queryString, null);
		} catch (MalformedQueryException e) {
			return keywords;
		}
		try {
			query.getTupleExpr().visit(new QueryModelVisitorBase<Exception>() {
				public void meet(ValueConstant node) {
					collectValue(node.getValue());
				}
				public void meet(Var node) {
					if (node.hasValue()) collectValue(node.getValue());
				}
				public void meetNode(QueryModelNode node) {
					try {
						node.visitChildren(this);
					} catch (Exception ex) {}
				}
				void collectValue(Value v) {
					String str = null;
					if (v instanceof URI) {
						str = ((URI)v).getLocalName();
					} else
						if (v instanceof Literal) {
							str = ((Literal)v).getLabel();
						} else {
							// some value (e.g. b-node) we're not interested in
						}
					// see if we have something to collect
					if (str != null) tokenizeString(str);
				}
				void tokenizeString(String str) {
					char[] chars = (str + ".").toCharArray();
					int start = -1, index = 0;
					for (char ch : chars) {
						if (Character.isLetter(ch)) {
							if (start < 0) start = index;
						} else {
							if (start >= 0) {
								tokenizeWord(new String(chars, start, index - start));
								start = -1;
							}
						}
						index++;
					}
				}
				void tokenizeWord(String word) {
					// additional tokenization pending...
					keywords.add(word.toLowerCase());
				}
			});
		} catch (Exception ex) {
			// ignore exceptions at this level
		}
		return keywords;
	}

	protected String prepareQuery(String word) {
		return "SELECT DISTINCT ?o WHERE { ?s ?p ?o . <" + word + ":> <http://www.ontotext.com/matchIgnoreCase> ?o}";
	}
	
	protected Collection<SPARQLQuery> prepareQueries(Collection<String> terms, SetOfStatements sos) {
		LinkedList<SPARQLQuery> result = new LinkedList<SPARQLQuery>();
		for (String term : terms) {
			String queryString = prepareQuery(term);
			SPARQLQuery query = new SPARQLQueryImpl(queryString);
			// restrict the query to labeled group of statements if needed
			if (sos instanceof LabelledGroupOfStatements) {
				query.setLabelledGroup(((LabelledGroupOfStatements)sos).getLabel());
			}
			result.add(query);
		}
		return result;
	}

	// --- P L U G I N  M E T H O D S ---------------------------------------
	
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		super.initialiseInternal(workflowDescription);
		// read parameters
		String maxMoleculesParam = retrieveParameter(PARAM_MAX_MOLECULES, workflowDescription);
		if (maxMoleculesParam != null)
			setMaxMolecules(Integer.parseInt(maxMoleculesParam));
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		SPARQLQuery inputQuery = getQuery(input);
		
		// transform input into query-able SPARQL endpoint
		SPARQLEndpoint endpoint = getInputEndpoint();
		
		// create a new tripleset to be returned
		LabelledGroupOfStatements sos = getConnection().createLabelledGroupOfStatements();

		// extract keywords from input query
		Collection<String> keywords = extractKeywords(inputQuery.toString());

		// build search queries for those keywords
		Collection<SPARQLQuery> queries = prepareQueries(keywords, input);
		
		// use an atomic integer as a counter object
		int maxMolecules = getMaxMolecules();
		if (maxMolecules <= 0) 
			maxMolecules = Integer.MAX_VALUE;
		AtomicInteger limit = new AtomicInteger(maxMolecules);
		AtomicInteger count = new AtomicInteger();
		
		// for each keyword we build search query for getting the relevant nodes
		try {
			for (SPARQLQuery query : queries) {
				// search for relevant nodes in graph
				CloseableIterator<Binding> it = endpoint.executeSelect(query).iterator();
				while (it.hasNext()) {
					Value node = (Value)it.next().getValues().get(0);
					locateMolecules(node, endpoint, sos, null, limit, count);
				}
			}
		} catch (Exception ex) {
			System.err.println(ex);
			return null;
		}
		// return the number of selected molecules as part of the contract
		setSelectedMolecules(maxMolecules - limit.get());
		setSelectedStatements(count.get());
		
		return sos;
	}

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(BaseLineFTSelecter.class);
		// Instantiate the base-line full text searches selecter
		BaseLineFTSelecter s = new BaseLineFTSelecter(new URIImpl("http://baselineft"));
		
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(
				"SELECT ?s ?p ?o WHERE { ?s ?p ?o FILTER(?s = 'sweet elyse ventana') }"
		);
		s.setMaxMolecules(2);

		// Select a tripleset associated with keywords found in the query
		SetOfStatements sos = new HTTPRemoteGraph(new URIImpl(
				"http://www.ninebynine.org/Software/HaskellRDF/RDF/Harp/test/wine.rdf"));
		new SetOfStatementsMerger().add(sos);
		
		// Do selection
		LabelledGroupOfStatements ts = (LabelledGroupOfStatements) s.invokeInternal(query.toRDF());
		if (ts != null) log.info("tripleset-uri: " + ts.getLabel());
		
		CloseableIterator<Statement> iter = ts.getStatements();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		
		DataLayerService.shutdown();
	}
}
