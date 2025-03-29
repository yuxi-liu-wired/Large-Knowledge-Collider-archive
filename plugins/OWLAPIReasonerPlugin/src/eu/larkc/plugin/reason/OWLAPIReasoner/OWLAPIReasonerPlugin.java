package eu.larkc.plugin.reason.OWLAPIReasoner;



	import java.net.MalformedURLException;
	import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

	import org.openrdf.model.Statement;
	import org.openrdf.model.URI;
	import org.openrdf.model.Value;
	import org.openrdf.model.impl.URIImpl;




	import eu.larkc.core.data.CloseableIterator;
	import eu.larkc.core.data.DataFactory;
	import eu.larkc.core.data.RdfGraph;
	import eu.larkc.core.data.SetOfStatements;
	import eu.larkc.core.data.SetOfStatementsImpl;
	import eu.larkc.core.data.VariableBinding;
	import eu.larkc.core.query.Query;
	import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;


	
	public class OWLAPIReasonerPlugin extends Plugin {
		
		private SPARQLQuery theQuery;
		
		private SetOfStatements theStatements;
		
		public OWLAPIReasonerPlugin(URI pluginName) {
			super(pluginName);
			// TODO Auto-generated constructor stub
		}
		
	   public SetOfStatements owlapiReasoning(Query query, SetOfStatements statements) {
			
			SetOfStatements result = new SetOfStatementsImpl();
			
			if (!(query instanceof SPARQLQuery))
			{
				System.out.println(query.toString() +" is not a correct SPARQL query");
				
				return result;
			
			}

            SPARQLQuery sq = (SPARQLQuery) query;

             OWLAPIReasoner reasoner = new OWLAPIReasoner();
             
             VariableBinding vb;
			
			if (sq.isSelect()){
				
			System.out.println("get the  sparql selection result ... for "+sq);
				
				vb = reasoner.sparqlSelect(sq, statements);
								
				printVariableBindings(vb);
				
				result = vb.toRDF(new SetOfStatementsImpl());
				
				
			}
			
			
			return result;
			
			
	   }
	   
	   
	   public static void printVariableBindings(VariableBinding bindings) {
			int k = 0;
			System.out.println("\nVariable Bindings");
			System.out
					.println("==============================================================");

			int numCols = bindings.getVariables().size();
			int[] colWidth = new int[numCols];
			for (int col = 0; col < numCols; ++col) {
				colWidth[col] = bindings.getVariables().get(col).length();
			}

			List<String[]> formatted = new ArrayList<String[]>();
			Iterator<VariableBinding.Binding> bindingIterator = bindings.iterator();
			while (bindingIterator.hasNext()) {
				VariableBinding.Binding binding = bindingIterator.next();

				String[] row = new String[numCols];
				for (int col = 0; col < numCols; ++col) {
					String value = binding.getValues().get(col).toString();
					int len = value.length();
					if (len > colWidth[col]) {
						colWidth[col] = len;
					}
					row[col] = value;
				}
				formatted.add(row);
			}

			System.out.print("|");
			for (int col = 0; col < numCols; ++col) {
				System.out.print(toString(bindings.getVariables().get(col),
						colWidth[col])
						+ "|");
			}
			System.out.println();

			System.out.print("|");
			for (int col = 0; col < numCols; ++col) {
				System.out.print(line(colWidth[col]) + "|");
			}
			System.out.println();

			Iterator<String[]> it = formatted.iterator();
			while (it.hasNext()) {
				k++;
				String[] row = it.next();
				System.out.print("|");
				for (int col = 0; col < numCols; ++col) {
					System.out.print(toString(row[col], colWidth[col]) + "|");
				}
				System.out.println();
			}
			System.out.println("\n # Bindings: " + k);
			System.out.println();
		}   
	  
	   
	   public static String toString(Object object, int length) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(object.toString());
			while (buffer.length() < length) {
				buffer.append(' ');
			}
			return buffer.toString();
		}

		public static String line(int length) {
			StringBuilder buffer = new StringBuilder();
			while (buffer.length() < length) {
				buffer.append('-');
			}
			return buffer.toString();
		}
	   
	   
	   
	   protected String retrieveParameter(String param) {
			return retrieveParameter(param, getPluginParameters());
		}
		
		protected String retrieveParameter(String param, SetOfStatements pluginParams) {
			CloseableIterator<Statement> params = pluginParams.getStatements();
			while (params.hasNext()) {
				Statement stmt = params.next();
				if (stmt.getPredicate().toString().equals(param)) {
					Value value = stmt.getObject();
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
			String dataSource = retrieveParameter(datasourceURI);
			
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
			
			return(owlapiReasoning(this.theQuery, this.theStatements));
			
		
		}

		@Override
		protected void shutdownInternal() {
			// TODO Auto-generated method stub
			
		}



		
		

	}
