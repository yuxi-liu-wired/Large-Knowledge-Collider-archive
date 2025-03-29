package eu.larkc.plugin.reason.CRIONReasoner;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nl.vu.cs.kr.PION.EntailmentResultEnum;
import nl.vu.cs.kr.PION.NonstandardReasoner;

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


/**
 * <p/>
 * tests CAC method in the inconsistent madcow ontology.
 * </p>
 *
 * @author Jun Fang and ZhiSheng Huang
 * Department of computer Science, Vrije Universiteit Amsterdam, The Netherlands
 * leon.essence@gmail.com, huang@cs.vu.nl
 *
 *
 * @version 1.0
 * @Date: 2010-6-15
 *
 */
public class CACMadCowTest {
	public static void main(String[] args) {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String ontologyString = "madcow.owl";
		URI phsicalURI = URI.create("file:/c:/working/ontologies/CRIONDatasets/" + ontologyString);
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
		
		CRIONReasoner cReasoner = new CRIONReasoner(ontology);
		
		NonstandardReasoner nReasoner = new NonstandardReasoner(ontology);
		Iterator<OWLAxiom> iter = queries.iterator();
		OWLAxiom a = null;
		while(iter.hasNext()) {
			a = iter.next();
			if(nReasoner.entail(a)==EntailmentResultEnum.Accepted) {
				System.out.println(ontologyString+ " nonstandardly implies "+a);
				Set<ContrastiveAnswer> cas = cReasoner.cac(a, nReasoner.getSelectedSubset());
				if(!cas.isEmpty()) {
					System.out.println("contrastive answers for "+ a+" is: ");
					Iterator<ContrastiveAnswer> caIter = cas.iterator();
					while(caIter.hasNext()) {
						System.out.println(caIter.next());
					}
				}
			}
			System.out.println();
		}
		
		queries.remove(a);
		if(nReasoner.entail(queries)==EntailmentResultEnum.Accepted) {
			System.out.println(ontologyString+ " nonstandardly implies the set.");
			Set<ContrastiveAnswer> cas = cReasoner.cac(queries, nReasoner.getSelectedSubset());
			if(!cas.isEmpty()) {
				System.out.println("contrastive answers for the set is: ");
				Iterator<ContrastiveAnswer> caIter = cas.iterator();
				while(caIter.hasNext()) {
					System.out.println(caIter.next());
				}
			} else {
				System.out.println("These doesn't exist contrastive answers.");
			}
		}

		
		OWLDataFactory factory = manager.getOWLDataFactory();
		String ns = "http://cohse.semanticweb.org/ontologies/people#";
		OWLClass sub = factory.getOWLClass(URI.create(ns+"mad+cow"));
		OWLClass sup = factory.getOWLClass(URI.create(ns+"vegetarian"));
		OWLDescription des = factory.getOWLObjectComplementOf(sup);
		OWLAxiom axiom = factory.getOWLSubClassAxiom(sub, des);
		
		if(nReasoner.entail(axiom)==EntailmentResultEnum.Accepted) {
			System.out.println(ontologyString+ " nonstandardly implies "+ axiom);
			Set<ContrastiveAnswer> cas = cReasoner.cac(axiom, nReasoner.getSelectedSubset());
			if(!cas.isEmpty()) {
				System.out.println("contrastive answers for "+ axiom+" is: ");
				Iterator<ContrastiveAnswer> caIter = cas.iterator();
				while(caIter.hasNext()) {
					System.out.println(caIter.next());
				}
			}
		}

		
	}

}
