/**
 *
 *
 */
package eu.larkc.plugin.reason.CRIONReasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.vu.cs.kr.PION.EntailmentResultEnum;
import nl.vu.cs.kr.PION.NonstandardReasoner;
import nl.vu.cs.kr.PION.SelectionFunction;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.OWLClass;

/**
 * <p/>
 * Represents the contrastive reasoner. 
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
public class CRIONReasoner {
	private OWLOntology ontology;


	public CRIONReasoner(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}

	
	/**
	 * returns contrastive answers by conjunction (CAC) for an query axiom
	 */
	public Set<ContrastiveAnswer> cac(OWLAxiom axiom, OWLOntology selectedSubset) {
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		axiomSet.add(axiom);
		return cac(axiomSet,selectedSubset);
	}
	
	
	/**
	 * returns contrastive answers by conjunction (CAC) for an query axioms set. 
	 * It extends the selection function of PION.
	 */
	public Set<ContrastiveAnswer> cac(Set<OWLAxiom> original, OWLOntology selectedSubset) {
		NonstandardReasoner nReasoner = new NonstandardReasoner(this.ontology);
		Set<ContrastiveAnswer> cas = new HashSet<ContrastiveAnswer>();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Reasoner classicalReasoner = new Reasoner(manager);
		
		if(selectedSubset==null) {
			try {
				selectedSubset = manager.createOntology(this.ontology.getURI());
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		}
		
		// step1: extent the selected set until it becomes inconsistent
		List<AddAxiom> addedList = new ArrayList<AddAxiom>();
		Iterator<OWLAxiom> iter = original.iterator();
		while(iter.hasNext()) {
			AddAxiom addedAxiom = new AddAxiom(selectedSubset, iter.next());
			addedList.add(addedAxiom);
		}
		
		try {
			manager.applyChanges(addedList);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		classicalReasoner.loadOntology(selectedSubset); 
		SelectionFunction selectionFun = new SelectionFunction(selectedSubset, this.ontology, original);
		
		// consider incoherence and inconsistency
//		while(classicalReasoner.getInconsistentClasses().isEmpty()&&classicalReasoner.isConsistent()) {
		// only consider inconsistency
		while(classicalReasoner.isConsistent()) {
			classicalReasoner.unloadOntology(selectedSubset);		
			selectionFun.setFormerOntology(selectedSubset);
			selectedSubset = selectionFun.select();       // selecting a sub-ontology
			if(selectedSubset == null) { // there does not exist a new relevant sub-ontology
				return cas; 
			} 
			classicalReasoner.loadOntology(selectedSubset); 
		}
		
//		selectionFun.setFormerOntology(selectedSubset);
//		selectedSubset = selectionFun.select();
		
		
		// step2: find a minimal inconsistent set which includes the query set
//		selectedSubset= selectionFun.select();
		FindingJustificationsinInconsistentOnt computeJus = new FindingJustificationsinInconsistentOnt();
		Set<OWLLogicalAxiom> logicalAS = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom a:original) {
			logicalAS.add((OWLLogicalAxiom) a);
		}
		// Calculation of the specific mis by using a complete method
//		Set<OWLLogicalAxiom> mis = computeJus.computingJustificationContainingAxiom(selectedSubset, logicalAS);
		
		// Calculation of the specific mis by using a simple pruning method, which is incomplete
//		Set<OWLLogicalAxiom> mis = computeJus.computingJustificationContainingAxiomSimple(selectedSubset, logicalAS);
		
		// Calculation of the specific mis by using a binary search method, which is much faster
		Set<OWLLogicalAxiom> mis = computeJus.computingJustificationContainingAxiomBinarySearch(selectedSubset, logicalAS);
		
		if(mis==null) {
			return cas;
		}
		
		
		// step3: construct the clarification axiom set and the contrastive axiom set
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		for(OWLLogicalAxiom a:mis) {
			allAxioms.add(a);
		}
		iter = allAxioms.iterator();
		if(allAxioms.size()==original.size()+1) {
			allAxioms.removeAll(original);
			ContrastiveAnswer ca = new ContrastiveAnswer(original,original,allAxioms);
			cas.add(ca);
			return cas;
		}
		while(iter.hasNext()) {
			Set<OWLAxiom> clarification = new HashSet<OWLAxiom>();
			
			clarification.add(iter.next());    // construct the clarification axiom set
			if(nReasoner.entail(clarification)==EntailmentResultEnum.Accepted) { // the ontology nonstandardly imply the clarification
				// construct the contrastive axiom set
				Set<OWLAxiom> contrastive = new HashSet<OWLAxiom>();
				contrastive.addAll(mis);
				contrastive.removeAll(original);
				contrastive.removeAll(clarification);
				if(!contrastive.isEmpty()) {
					if(nReasoner.entail(contrastive)==EntailmentResultEnum.Accepted) { // the ontology nonstandardly imply the contrastive
						ContrastiveAnswer ca = new ContrastiveAnswer(original,clarification,contrastive);
						cas.add(ca);
					}
				}

			}
		}
		
		return cas;
	}


	
	
	/**
	 * returns true if the two sets is intersected
	 */
	private Boolean intersection(Set s1, Set s2) { 
		Set temp = new HashSet();
		temp.addAll(s1);
		temp.retainAll(s2);
		if(temp.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	

	

}
