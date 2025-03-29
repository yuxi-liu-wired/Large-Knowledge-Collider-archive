package eu.larkc.plugin.reason.PIONReasoner;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class PIONSimpleTest {

	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String ontologyString = "madcow.owl";
		URI phsicalURI = URI.create("file:/e:/working/ontologies/CRIONDatasets/" + ontologyString);
		OWLOntology ontology = null;
		
		try {
			ontology = manager.loadOntologyFromPhysicalURI(phsicalURI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		

		PelletOptions.USE_TRACING = true;
		Reasoner reasoner = new Reasoner( manager );
		reasoner.loadOntology( ontology );
		
		Set<OWLClass> unsatisfiableConcepts = reasoner.getInconsistentClasses();
		Iterator<OWLClass> cIter = unsatisfiableConcepts.iterator();
		System.out.println("Original unsatisfiable concepts:");
		while(cIter.hasNext()) {
			System.out.println(cIter.next());
		}

		Set<OWLAxiom> queries = new HashSet<OWLAxiom>();
		Set<Set<OWLAxiom>> allJus = new HashSet<Set<OWLAxiom>>();
		

		cIter = unsatisfiableConcepts.iterator();
		while(cIter.hasNext()) {
			OWLClass c = cIter.next();
			reasoner.getKB().setDoExplanation( true );
			reasoner.isSatisfiable(c);
			Set<OWLAxiom> exp = reasoner.getExplanation();
			allJus.add(exp);
			queries.addAll(exp);
		}
		

		
		PIONReasoner nReasoner = new PIONReasoner(ontology);
		Iterator<OWLAxiom> iter = queries.iterator();
		OWLAxiom a = null;
		while(iter.hasNext()) {
			a = iter.next();
			if(nReasoner.entail(a)==EntailmentResultEnum.Accepted) {
				System.out.println(ontologyString+ " nonstandardly implies "+a);
			}
		}
		
		queries.remove(a);
		if(nReasoner.entail(queries)==EntailmentResultEnum.Accepted) {
			System.out.println(ontologyString+ " nonstandardly implies the query set.");
		}
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		String ns = "http://cohse.semanticweb.org/ontologies/people#";
		OWLClass sub = factory.getOWLClass(URI.create(ns+"mad+cow"));
		OWLClass sup = factory.getOWLClass(URI.create(ns+"vegetarian"));
		OWLDescription des = factory.getOWLObjectComplementOf(sup);
		OWLAxiom axiom = factory.getOWLSubClassAxiom(sub, des);
		
		if(nReasoner.entail(axiom)==EntailmentResultEnum.Accepted) {
			System.out.println(ontologyString+ " nonstandardly implies "+ axiom);
		}

	}
}
