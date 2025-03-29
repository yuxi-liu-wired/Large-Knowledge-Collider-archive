package eu.larkc.plugin.reason.CRIONReasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * <p/>
 * Change an incoherent ontology into an inconsitent ontology.  
 * </p>
 *
 * @author Jun Fang and ZhiSheng Huang
 * Department of computer Science, Vrije Universiteit Amsterdam, The Netherlands
 * jun.fang.nwpu@gmail.com, huang@cs.vu.nl
 *
 *
 * @version 1.0
 * @Date: 2011-01-05
 *
 */


public class IncoherentToInconsistent {
	
	private OWLOntologyManager manager;
	private Reasoner reasoner;

	
	
	public IncoherentToInconsistent() {
		super();
		this.manager = OWLManager.createOWLOntologyManager();
		this.reasoner = new Reasoner(manager);
	}
	
	/**
	 * Transform an incoherent ontology into an incosistent ontology
	 * @param ont
	 * @return
	 */
	public OWLOntology tranform(OWLOntology ont) {
		reasoner.loadOntology(ont);
		if(reasoner.getInconsistentClasses().isEmpty()) {
			if(reasoner.isConsistent()) {
				// the given ontology is coherent and consistent, we cannot contrust inconsistent ontology, return null
				return null;
			} else {
				// the given ontology is coherent but inconsistent, return itself 
				return ont;
			}
		} else {
			// the given ontology is incoherent, construct incosistent ontology by adding atomic concept assertion
			return tranformByAtomicConceptAssertion(ont);
		}
		
	}
	
	/**
	 * Transform an incoherent ontology into an inconsistent ontology by adding concept assertion for every atomic concept.
	 * 
	 * @param ont
	 * @return
	 */
	private OWLOntology tranformByAtomicConceptAssertion(OWLOntology ont) {
		Set<OWLClass> concepts = ont.getReferencedClasses();
		OWLDataFactory factory = manager.getOWLDataFactory();
//		String ns = ont.getURI().toString();
		Set<OWLLogicalAxiom> addedAxioms = new HashSet<OWLLogicalAxiom>();
		
		for(OWLClass con:concepts) {
//			if(con.getIndividuals(ont).isEmpty()) {
//				// only add concept assertion to the concept without concept assertion
//				OWLIndividual ind = factory.getOWLIndividual(ont.getURI().create("the_"+con.toString()));
//				addedAxioms.add(factory.getOWLClassAssertionAxiom(ind, con));
//			}
			OWLIndividual ind = factory.getOWLIndividual(ont.getURI().create("the_"+con.toString()));
			addedAxioms.add(factory.getOWLClassAssertionAxiom(ind, con));
		}
		if(!addedAxioms.isEmpty()) {
			this.addAxiomstoOnt(ont, addedAxioms);
		}
		
		return ont;
	}
	
	
	/**
	 * Add a set of OWLLogical axioms into an ontology.
	 * 
	 * @param ont
	 * @param s
	 */
	private void addAxiomstoOnt (OWLOntology ont, Set<OWLLogicalAxiom> s) {
		
			
		Iterator<OWLLogicalAxiom> iter = s.iterator();
		List<AddAxiom> addedList = new ArrayList<AddAxiom>();
		
		while(iter.hasNext()) {
			AddAxiom addedAxiom = new AddAxiom(ont, iter.next());
			addedList.add(addedAxiom);
		}
		
		try {
			manager.applyChanges(addedList);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
	}

}
