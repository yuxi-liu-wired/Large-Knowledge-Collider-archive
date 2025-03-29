package eu.larkc.plugin.reason.OWLAPIReasoner;



	import java.net.MalformedURLException;
	import java.net.URL;
	import java.util.Iterator;



import org.openrdf.model.Statement;
	import org.openrdf.model.URI;
	import org.openrdf.model.impl.URIImpl;



import eu.larkc.core.data.CloseableIterator;
	import eu.larkc.core.data.DataFactory;
	import eu.larkc.core.data.RdfGraph;
    import eu.larkc.core.data.SetOfStatements;
	import eu.larkc.core.data.VariableBinding;
    import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.query.SPARQLQuery;


	public class OWLAPIReasonerPluginTest {
		

		
		public static void main(String[] args) {
			
	
			
//			String localDataDirectory="D:/svn-base/LarKCReasoner/example/data/";
//			String localDataDirectory="F:/svn-space/LarKCReasoner/example/data/";
//			String localDataDirectory="E:/eclipse/workspace/plugins/src/eu/larkc/plugin/reason/dig/data/";
//			String localDataDirectory="E:/svn-base/LarKCReasoner/example/data/";
//			String dataFileName = "opjk.rdfs.owl.xml";
//			String dataFileName = "mad_cows.owl";
//			String dataFileName = "wine.rdf";

//			String ontologyFileName = "http://www.cs.vu.nl/~huang/larkc/opjk.rdfs.owl.xml";	
			String ontologyFileName= "http://www.cs.vu.nl/~huang/larkc/ontology/wine.rdf";
//			String ontologyFileName= "http://www.cs.vu.nl/~huang/larkc/ontology/mad_cows.owl";
//			String ontologyFileName = "http://www.mindswap.org/2004/owl/mindswappers#";
//		    String ontologyFileName = "file:////"+ localDataDirectory + dataFileName;

				
			

			

			
		

			
			//to list all subclass of wine
			String query34 ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X \n"+
			"WHERE {"+
		       "?X rdfs:subClassOf wine:Wine.\n"+
			        "}";//+
			
			
			// to list all superclass of Bordeaux
			String query34b ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X \n"+
			"WHERE {"+
		       "wine:Bordeaux rdfs:subClassOf ?X.\n"+
			        "}";//+
			
			// to list all superclass of Bordeaux
			String query34c ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X \n"+
			"WHERE {"+
		       "wine:FrenchWine rdfs:subClassOf ?X.\n"+
			        "}";//+
			
			// to list all superclass pairs of Bordeaux
			String query36 ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X ?Y \n"+
			"WHERE {"+
		       "wine:Bordeaux rdfs:subClassOf ?X.\n"+
		       "?X rdfs:subClassOf wine:Wine.\n"+
		       "?X rdfs:subClassOf ?Y.\n"+
		       "?Y rdf:type owl:Class.\n"+
		       "wine:Bordeaux rdf:type owl:Class.\n"+
			        "}";//+
			
			// to list all superclass pairs of Bordeaux
			String query36b ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X ?Y \n"+
			"WHERE {"+
		       "wine:Bordeaux rdfs:subClassOf ?X.\n"+
		       "?X rdfs:subClassOf wine:Wine.\n"+
		       "?Y rdfs:subClassOf ?X.\n"+
		       "?Y rdf:type owl:Class.\n"+
			        "}";//+
			
			
			//to list all subclass of wine
			String query36c ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X  ?Y\n"+
			"WHERE {"+
		       "?X rdfs:subClassOf wine:Wine.\n"+
		       "?Y rdfs:subClassOf wine:TableWine.\n"+
			        "}";//+

			
			String query37 ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+ 
			"PREFIX wine:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n" +
			"PREFIX food:       <http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#>\n" +
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +  
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>\n"+
			"SELECT ?X ?Y ?Z\n"+
			"WHERE {"+
		       "wine:Bordeaux rdfs:subClassOf ?X.\n"+
		       "?X rdfs:subClassOf ?Y.\n"+
		       "?Y rdfs:subClassOf wine:Wine.\n"+
		       "?Z rdfs:subClassOf ?Y.\n"+
			        "}";//+
			
			String query5 ="PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+ 
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>"+ 
			"PREFIX kb:       <http://cohse.semanticweb.org/ontologies/people#>" + 
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>" + 
			"PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#>"+
			"SELECT ?X ?Y\n"+
			"WHERE {"+
		       "kb:madcow rdfs:subClassOf ?X.\n"+
		       "?X rdfs:subClassOf kb:animal.\n"+
		       "?X rdfs:subClassOf ?Y.\n"+
		       "?Y rdf:type owl:Class.\n"+
		       "kb:madcow rdf:type owl:Class.\n"+
			        "}";//+
			
		
	//		ReasonerTest(ontologyFileName, query34);

	      	

			
			ReasonerTest(ontologyFileName, query36);
			

	      	

			
//	     String ontologyFileName5 ="http://wasp.cs.vu.nl/larkc/ontology/university0-0.owl";
			
			
			
			
		}
		
		private static void ReasonerTest(String ontologyFileName, String query)
		{
			URL url=null;
			try {
				url = new URL(ontologyFileName);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		
		
		
		OWLAPIReasoner reasoner = new OWLAPIReasoner();
		
		
		
		RdfGraph graph = DataFactory.INSTANCE.createRemoteRdfGraph(new URIImpl(
				url.toString()));
		
		
	
		
	
		
		System.out.println("Starting the test ...\n");
//		System.out.println("Make sure that the external DIG reasoner is running at the host: "+ hostname + " and the port: " +port);
		
		System.out.println("The query is: " + query);	
		
		System.out.println("The graph is: " + graph.toString());
		
		SPARQLQuery sparqlQuery = DataFactory.INSTANCE.createSPARQLQuery(query); 
		
	
		 URI pluginURI = new URIImpl(KRConstants.PLUGIN_URI);
			
			
			OWLAPIReasonerPlugin re = new OWLAPIReasonerPlugin(pluginURI);
			
			
			SetOfStatements answer = re.owlapiReasoning(sparqlQuery, graph);
			
			
			
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
