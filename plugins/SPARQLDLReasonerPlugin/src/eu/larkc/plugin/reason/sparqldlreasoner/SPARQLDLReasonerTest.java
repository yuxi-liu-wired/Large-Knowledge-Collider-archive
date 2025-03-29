package eu.larkc.plugin.reason.sparqldlreasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.query.SPARQLQuery;

public class SPARQLDLReasonerTest {
	public static void main(String[] args) {
		String ontologyFileName = "http://wasp.cs.vu.nl/larkc/ontology/university0-0.owl";
		URL url = null;
		
		try {
			url = new URL(ontologyFileName);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		String query = "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
			+ "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"
			+ "SELECT ?X " + "WHERE {" + "?X rdf:type ub:FullProfessor .}";// +
		
		
//		String query1 = "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//			+ "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"
//			+ "PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
//			+ "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"
//			+ "PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"
//			+ "SELECT ?X  ?Y1 ?Y2  ?Y3  \n"
//			+ "WHERE {"
//			+ "?X rdf:type ub:FullProfessor .\n"
//			+ "?X ub:worksFor <http://www.Department0.University0.edu> .\n"
//			+ "	?X ub:name ?Y1 .\n"
//			+ "	?X ub:emailAddress ?Y2 .\n"
//			+ "	?X ub:telephone ?Y3}";
		
		
		RdfGraph graph = DataFactory.INSTANCE.createRemoteRdfGraph(new URIImpl(
				url.toString()));
		
	//	System.out.println("Graph=" + graph.toString());
	//	URI uri = graph.getName();
	//	System.out.println("uri=" + uri.toString());
		
		
	   URI pluginURI = new URIImpl(KRConstants.PLUGIN_URI);
	
		
		SPARQLDLReasonerPlugin re = new SPARQLDLReasonerPlugin(pluginURI);
		
		SPARQLQuery thequery = DataFactory.INSTANCE.createSPARQLQuery(query);
		
		SetOfStatements answer = re.sparqldlReasoning(thequery, graph);
		
		printVariableBinding(answer);
		
	}
	
	private static void printVariableBinding(SetOfStatements data)
	{
		VariableBinding  vb = DataFactory.INSTANCE.createVariableBinding(data);
		
		Iterator<VariableBinding.Binding> vit = vb.iterator();
        

		while (vit.hasNext()) {

			Binding bin = vit.next();
			System.out.println(bin.getValues().toString());

		}

		
	}
	
}
