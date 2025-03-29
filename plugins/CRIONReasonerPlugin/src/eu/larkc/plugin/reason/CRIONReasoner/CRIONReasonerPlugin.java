package eu.larkc.plugin.reason.CRIONReasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.net.URI;

import nl.vu.cs.kr.PION.EntailmentResultEnum;
import nl.vu.cs.kr.PION.NonstandardReasoner;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
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

public class CRIONReasonerPlugin extends Plugin {

	private SPARQLQuery theQuery;
	private OWLOntology theOntology;

	
	public CRIONReasonerPlugin(org.openrdf.model.URI pluginName) {
		super(pluginName);
	}

	
	public Set<ContrastiveAnswer> crionReasoning(Query query,
			OWLOntology ontology) {

		Set<ContrastiveAnswer> cas = new HashSet<ContrastiveAnswer>();

		if (!(query instanceof SPARQLQuery)) {
			System.out.println(query.toString()
					+ " is not a correct SPARQL query");

			return cas;

		}

		SPARQLQuery sq = (SPARQLQuery) query;

		OWLAxiom q = getAxiom(sq);

		CRIONReasoner cReasoner = new CRIONReasoner(ontology);
		IncoherentToInconsistent transformer = new IncoherentToInconsistent();
		ontology = transformer.tranform(ontology);
		if(ontology==null) {
			System.out.println("The ontology is consistent");
			return cas;
		}
		NonstandardReasoner nReasoner = new NonstandardReasoner(ontology);
		

		if (nReasoner.entail(q) == EntailmentResultEnum.Accepted) {
			System.out.println(ontology.toString() + " nonstandardly implies "
					+ q);
			cas = cReasoner.cac(q, nReasoner.getSelectedSubset());
			if (!cas.isEmpty()) {
				System.out.println("contrastive answers for " + q + " is: ");
				Iterator<ContrastiveAnswer> caIter = cas.iterator();
				while (caIter.hasNext()) {
					System.out.println(caIter.next());
				}
			} else {
				System.out.println("There are no contrastive answers");
			}
		} else {
			System.out.println(ontology.toString() + " doesn't nonstandardly implies "
					+ q);
		}

		return cas;

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
//			OWLDescription des = factory.getOWLObjectComplementOf(sup);
			axiom = factory.getOWLSubClassAxiom(sub, sup);
		}
		return axiom;
	}

	
	// change the set of contrastive answers to statements
	private SetOfStatements toStatements(Set<ContrastiveAnswer> crionReasoning) {
		SetOfStatements result = null;
		ArrayList<Statement> a = new ArrayList<Statement>();
//		org.openrdf.model.URI subject = new URIImpl(this.theOntology.toString());
//		org.openrdf.model.URI bNode = new URIImpl("http://www.larkc.eu/crion#result");
//		org.openrdf.model.URI contrastivelyEntail = new URIImpl("http://larkc.cs.vu.nl/crion#contrastively entail");
		org.openrdf.model.URI hasAnswer = new URIImpl(KRConstants.PARAM_URI_ANSWER);
		org.openrdf.model.URI hasBut = new URIImpl(KRConstants.PARAM_URI_BUT);
		org.openrdf.model.URI hasAlthough = new URIImpl(KRConstants.PARAM_URI_ALTHOUGH);
		
		if(crionReasoning.isEmpty()||crionReasoning==null) {
			result = new SetOfStatementsImpl();
		} else {
			int i=0;
			for(ContrastiveAnswer ca:crionReasoning) {
				i++;
				Literal yes = ValueFactoryImpl.getInstance().createLiteral("True");
				Set<OWLAxiom> butAxioms = ca.getClarificationAxiomSet();
				Iterator<OWLAxiom> iter = butAxioms.iterator();
				String although = null;
				while(iter.hasNext()) {
					if(although==null) {
						although = iter.next().toString()+" ";
					} else {
						String s = iter.next().toString();
						although = although+s+" ";
					}
				}
				
				Iterator<OWLAxiom> iter1 = ca.getContrastiveAxiomSet().iterator();
				String but = null;
				while(iter1.hasNext()) {
					if(but==null) {
						but = iter1.next().toString()+" ";
					} else {
						String s = iter1.next().toString();					
						but = but+s+" ";
					}
				}
				
				Literal butAnswer = ValueFactoryImpl.getInstance().createLiteral(but);
				Literal althoughAnswer = ValueFactoryImpl.getInstance().createLiteral(although);
				
				org.openrdf.model.URI bNode = new URIImpl("http://www.larkc.eu/crion#result"+i);
				
				Statement stmt1 = new StatementImpl(bNode, hasAnswer, yes);
				Statement stmt2 = new StatementImpl(bNode, hasBut, butAnswer);
				Statement stmt3 = new StatementImpl(bNode, hasAlthough, althoughAnswer);
				a.add(stmt1);
				a.add(stmt2);
				a.add(stmt3);
			}
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
		}
		;

		return (toStatements(crionReasoning(this.theQuery, this.theOntology)));

	}

	
	@Override
	protected void shutdownInternal() {


	}

}
