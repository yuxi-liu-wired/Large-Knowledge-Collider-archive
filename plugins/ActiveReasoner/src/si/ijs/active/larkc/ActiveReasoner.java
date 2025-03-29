package si.ijs.active.larkc;

import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.plugin.Plugin;

public class ActiveReasoner extends Plugin{

	public ActiveReasoner(URI _pluginName) {
		super(_pluginName);
	}


	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		System.out.println("REASONER INITIALIZED");
		
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		
		//red - http://www.clker.com/cliparts/a/x/q/L/G/9/delete-icon-th.png
		//green - http://www.clker.com/cliparts/U/b/3/E/T/z/ok-icon-th.png
		
		RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection ();
		
		//////////////test
		
		/*CloseableIterator<Statement> testiter = con.search(null, RDF.URI_SUBEVENTS, null, null, null);
		System.err.println("//////////////////////////////////////////////");
		while (testiter.hasNext()) {
			Statement st = testiter.next();
			System.out.println("SSSSSSSSSSSSSSSSSSSSS: "+st.getSubject() + "  " + st.getPredicate() + "  " + st.getObject());
		}*/
		////////////////
		//add statements to internal store (if nothing else, it removes direct duplicates) and makes it possible to query it
		URI GCGraph = new URIImpl (RDF.CYC_PREFIX+"GCgraph");//add this to graph here due to workaround due to DataLayer
		URI graph=null;
		CloseableIterator<Statement> iter = input.getStatements();
		while (iter.hasNext()) {
			Statement statement = iter.next();
			Resource context = statement.getContext();
			
			if (context == null)
				graph = GCGraph;
			else
				graph=new URIImpl (context.stringValue());
			
			//(_:Trip to Tanzania, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.com/cyc#Travel-RoundTrip)
			//if something is a round trip, then it is an event too
			if (statement.getObject().equals(RDF.URI_ROUNT_TRIP))
				con.addStatement(statement.getSubject(), statement.getPredicate(), RDF.URI_EVENT,graph);
			
			if (!con.search(statement.getSubject(), statement.getPredicate(), statement.getObject(), null, null).hasNext());
				con.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(),graph);//trip, RDFConstants.RDF_TYPE,cycRoundTrip, graph);
		}//end of adding to the store
		
		VariableBinding vb = con
		.executeSelect(new SPARQLQueryImpl(
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
						+ "PREFIX cyc:<http://example.com/cyc#>"
						+ "PREFIX tripit:<http://example.com/TripitPlugin/tripit#>"
						+ "PREFIX time:<http://www.w3.org/2006/time#>"
						+ "SELECT  ?NAME ?P " + "WHERE"
						+ "{ ?P rdf:type cyc:event."
						+ "  ?P cyc:preferredNameString ?NAME.}"));
		CloseableIterator<Binding> iterNames = vb.iterator();
		while (iterNames.hasNext()) {
			Binding b = iterNames.next();
			List<Value> values = b.getValues();
			String name = values.get(0).toString().replaceAll("\"", "");
			
			//CHECK FOR LODGING
			//con.search((BNode)values.get(1), RDF.URI_PROVIDING_LODGING, obj, GCGraph, label)
			BNode bEvent = (BNode)values.get(1);
			if (!con.search(bEvent, RDF.URI_LODGING, null, null, null).hasNext())
			{
				con.addStatement(bEvent, RDF.URI_HAS_ERROR, new LiteralImpl("Lodging information missing!"), graph);
			}
		}
	
	
	
		//amswers the SPARQL query
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
		if (query == null)
			return input;
		
		//if (query != null){
		VariableBinding binding = con.executeSelect(query);
	
		SetOfStatements statements3 = binding.toRDF(new SetOfStatementsImpl());
		
		return statements3;//}
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
	
	

}
