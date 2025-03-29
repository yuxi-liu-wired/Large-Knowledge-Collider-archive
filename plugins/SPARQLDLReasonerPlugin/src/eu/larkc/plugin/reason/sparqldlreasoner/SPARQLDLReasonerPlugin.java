package eu.larkc.plugin.reason.sparqldlreasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;



import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBindingValue;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;


public class SPARQLDLReasonerPlugin extends Plugin {
	
	private SPARQLQuery theQuery;
	
	private SetOfStatements theStatements;
	
	public SPARQLDLReasonerPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}
	
   public SetOfStatements sparqldlReasoning(Query theQuery, SetOfStatements statements) {
		
		SetOfStatements result = new SetOfStatementsImpl();
		
		if (!(theQuery instanceof SPARQLQuery))
			return result;
		


		SPARQLQuery sq = (SPARQLQuery) theQuery;
		
		Model m = getModel(statements);
		
		com.hp.hpl.jena.query.Query query = QueryFactory.create(theQuery
				.toString());
		


		// Create a SPARQL-DL query execution for the given query and
		// ontology model
		QueryExecution qe = SparqlDLExecutionFactory.create(query, m);


		
		if (sq.isSelect()){
			
			ResultSet rs = qe.execSelect();
			
	//	System.out.println("Variable binding = "+rs.toString());
			
			VariableBinding vb = convertJenaQueryResult(rs);
			
			result = vb.toRDF(new SetOfStatementsImpl());
		}
		
		
		return result;
		
		
   }
   
   private Value convert(RDFNode value) {
		if (value instanceof com.hp.hpl.jena.rdf.model.Literal) {
			return new LiteralImpl(value.toString());
		} else if (value instanceof com.hp.hpl.jena.rdf.model.Resource) {
			String str = ((com.hp.hpl.jena.rdf.model.Resource) value).getURI();
			if (str != null)
				return new URIImpl(str);
			else {
				str = ((com.hp.hpl.jena.rdf.model.Resource) value).getId()
						.getLabelString();
				return new BNodeImpl(str);
			}
		} else {
			String str = ((com.hp.hpl.jena.rdf.model.Property) value).getURI();
			return new URIImpl(str);
		}
   }
   
   private Model getModel(SetOfStatements statements) {
		OntModel m = ModelFactory
				.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		Model model = ModelFactory.createDefaultModel();
		CloseableIterator<Statement> iter = statements.getStatements();

		while (iter.hasNext()) {
			Statement s = iter.next();
			com.hp.hpl.jena.rdf.model.Resource r = model.createResource(s
					.getSubject().stringValue());
			Property p = model.createProperty(s.getPredicate().stringValue());
			RDFNode n = null;
			if (s.getObject() instanceof Resource) {
				n = model.createResource(s.getObject().stringValue());
			} else if (s.getObject() instanceof Literal) {
				n = model.createLiteral(s.getObject().stringValue());
			}
			com.hp.hpl.jena.rdf.model.Statement js = model.createStatement(r,
					p, n);
			model.add(js);
		}
		m.add(model);
		return m;
	}
   
   
   
   private VariableBinding convertJenaQueryResult(final ResultSet rs) {
		return new VariableBinding() {

			private static final long serialVersionUID = 1;

			@SuppressWarnings("unchecked")
			public List<String> getVariables() {
				List list = rs.getResultVars();
				return list;
			}

			@SuppressWarnings("unchecked")
			public CloseableIterator<Binding> iterator() {
				
				List<Binding> newResult = new ArrayList<Binding>();
				
				Binding currentBindings = null;
				while (rs.hasNext()) {
					final QuerySolution solution = rs.nextSolution();
					final List<Value> result = new ArrayList<Value>();
					Iterator names = solution.varNames();
					while (names.hasNext()) {
						String current = (String) names.next();
						result.add(convert(solution.get(current)));
					}
					currentBindings = new Binding() {
						public List<Value> getValues() {
							return result;
						}
					};
					
					newResult.add(currentBindings);

				}
				return new WrappedIterator(newResult);
			}

			public SetOfStatements toRDF(SetOfStatements data) {
				
				SetOfStatementsImpl s = null;
				if (data instanceof SetOfStatementsImpl == false) {
					s = (SetOfStatementsImpl) data;
				} else {
					
					s = new SetOfStatementsImpl(data.getStatements());
				}
				

				ValueFactory vf = new ValueFactoryImpl();
				s.getData().add(
						new StatementImpl(vf.createBNode(), new URIImpl(
								DataFactory.LARKC_NS + "VariableBinding"),
								new VariableBindingValue(this)));
				return s;
				
			}
			
		};
	}
   
   public class WrappedIterator implements CloseableIterator<Binding> {
		
		List<Binding> res = new ArrayList<Binding>();
		
		Iterator<Binding> iter;

		public WrappedIterator(List<Binding> currentBindings) {
			res = currentBindings;
			iter = res.iterator();
		}

		public void close() {
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public boolean isClosed() {
			return false;
		}

		public Binding next() {
			try {
				return iter.next();
			} finally {
				res = null;
			}
		}

		public void remove() {
		}
	}
   
   protected String retrieveParameter(String param) {
		return retrieveParameter(param, getPluginParameters());
	}
	
	protected String retrieveParameter(String param, SetOfStatements pluginParams) {
		CloseableIterator<Statement> params = pluginParams.getStatements();
		while (params.hasNext()) {
			Statement stmt = params.next();
			String predicateName = stmt.getPredicate().toString();
			
			System.out.println("stmt=" + stmt.toString());
			if (predicateName.equals(param)) {
				Value value = stmt.getObject();
				System.out.println("find value=" + value.toString());
				return value.stringValue();
			}
		}
		return null;
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		// TODO Auto-generated method stub
		
	//    String queryURI = KRConstants.PARAM_URI_QUERY;
		String datasourceURI = KRConstants.PARAM_URI_DATA_SOURCE;
		
		System.out.println("going to get the data source ...");
	//	String query = retrieveParameter(queryURI);
		String dataSource = retrieveParameter(datasourceURI, workflowDescription);
		
		System.out.println("dataSource = "+ dataSource);
		
		URL url=null;
		try {
			url = new URL(dataSource);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		System.out.println("url = "+url.toString());
		
		RdfGraph graph = DataFactory.INSTANCE.createRemoteRdfGraph(new URIImpl(
				url.toString()));
		
		System.out.println("graph = "+graph.toString());
		
		this.theStatements = graph;

		
	}

		
	

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// TODO Auto-generated method stub
	
		
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
		
		if (!(query==null)) {this.theQuery = query;};
		
		return(sparqldlReasoning(this.theQuery, this.theStatements));
		
	
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}



	
	

}
