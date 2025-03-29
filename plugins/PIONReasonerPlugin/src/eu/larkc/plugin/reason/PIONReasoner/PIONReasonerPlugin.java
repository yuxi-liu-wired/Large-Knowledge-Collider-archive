package eu.larkc.plugin.reason.PIONReasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.StatementImpl;


import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;

public class PIONReasonerPlugin extends Plugin {

	private SPARQLQuery theQuery;
	private OWLOntology theOntology;

	
	public PIONReasonerPlugin(org.openrdf.model.URI pluginName) {
		super(pluginName);
	}

	
	public EntailmentResultEnum pionReasoning(OWLAxiom q,
			OWLOntology ontology) {



		PIONReasoner nReasoner = new PIONReasoner(ontology);
		

		if (nReasoner.entail(q) == EntailmentResultEnum.Accepted) {
			System.out.println(ontology.toString() + " nonstandardly implies "
					+ q);
			return EntailmentResultEnum.Accepted;
		} else {
			System.out.println(ontology.toString() + " doesn't nonstandardly implies "
					+ q);
		}

		return EntailmentResultEnum.Undetermined;

	}

	
	// obtains the axiom from the SPARAL query, can only handle simple inclusion axiom like "sub \subclass sup" now
	private OWLAxiom getAxiom(SPARQLQuery sq) {
		OWLAxiom axiom = null;
		com.hp.hpl.jena.query.Query query = QueryFactory.create(sq.toString());
		Element e = query.getQueryPattern();
	    ElementGroup elementGroup = (ElementGroup) e;
	    List<?> elements = elementGroup.getElements();
	    Element first = (Element) elements.get(0);
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    String parts[] = first.toString().split("\\s{1,}");
//	    System.out.println(first);
	    
	    String subString = parts[0].substring(1, parts[0].length()-1);
	    String supString = parts[2].substring(1,parts[2].length()-1);
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass sub = factory.getOWLClass(URI.create(subString));
		OWLClass sup = factory.getOWLClass(URI.create(supString));
		if(parts[1].equals("<http://www.w3.org/2000/01/rdf-schema#subClassOf>")) {
			OWLDescription des = factory.getOWLObjectComplementOf(sup);
			axiom = factory.getOWLSubClassAxiom(sub, sup);
		}
		return axiom;
	}

	
	// change the set of contrastive answers to statements
	private SetOfStatements toStatements(EntailmentResultEnum  answer, OWLAxiom q) {
		SetOfStatements result = null;
		
		ArrayList<Statement> a = new ArrayList<Statement>();
		
		

		 
		org.openrdf.model.URI subject = new URIImpl("http://www.larkc.eu/pion#result");

		org.openrdf.model.URI hasAnswer = new URIImpl(KRConstants.PARAM_URI_ANSWER);

		Literal t = ValueFactoryImpl.getInstance().createLiteral("True");
		Literal f = ValueFactoryImpl.getInstance().createLiteral("False");
	       
	       
		if(answer == EntailmentResultEnum.Accepted) {
			Statement stmt = new StatementImpl(subject, hasAnswer, t);
			a.add(stmt);
		} else {
			Statement stmt = new StatementImpl(subject, hasAnswer, f);
			a.add(stmt);
		}
		
		result = new SetOfStatementsImpl(a);
		
		return result;
	}
	
	
	protected String retrieveParameter(String param) {
		return retrieveParameter(param, getPluginParameters());
	}

	
	protected String retrieveParameter(String param,
			SetOfStatements pluginParams) {
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
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			try {
				this.theOntology = manager.loadOntologyFromPhysicalURI(URI.create(url.toString()));
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			

			
		}

	
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {

		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);

		if (!(query == null)) {
			this.theQuery = query;
		} 		else {
			return new SetOfStatementsImpl();
		}
		;

		SPARQLQuery sq = (SPARQLQuery) query;

		OWLAxiom q = getAxiom(sq);
		
		return (toStatements(pionReasoning(q, this.theOntology),q));

	}

	
	@Override
	protected void shutdownInternal() {


	}

}
