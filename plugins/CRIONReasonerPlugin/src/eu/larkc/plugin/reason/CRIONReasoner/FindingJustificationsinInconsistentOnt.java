package eu.larkc.plugin.reason.CRIONReasoner;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * <p/>
 * Compute justifications in a given inconsistent ontology. 
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

//TODO 1) Run-time performance of finding all justifications by 
// using HST (including two optimization methods) is not good.
// when there are more than 7 justifications, it cannot obtain the answer,
// let alone transportation and economy which has more than 50 justifications.
// There are two possibilities: 1) My implementation is wrong, I need to check it more carefully
// 2) Complexity of it is too big, it cannot used in practice, we need to figure out better method.

public class FindingJustificationsinInconsistentOnt {
	
	private OWLOntologyManager manager;
	private Reasoner reasoner;

	
	
	public FindingJustificationsinInconsistentOnt() {
		super();
		this.manager = OWLManager.createOWLOntologyManager();
		this.reasoner = new Reasoner(manager);
	}

	
	
	/**
	 * 
	 * Compute all justifications for explaining 
	 * inconsistencies in a given ontology.
	 * 
	 * @param ont The inconsistent ontology.
	 * @param justifications all justifications.
	 * @return all justifications
	 */
	public Set<Set<OWLLogicalAxiom>> computingAllJustifications(
			OWLOntology ont) {
		
		Set<Set<OWLLogicalAxiom>> justifications = new HashSet<Set<OWLLogicalAxiom>>();
		Stack<OWLLogicalAxiom> currentPath = new Stack<OWLLogicalAxiom>();
		Set<Set<OWLLogicalAxiom>> allPaths = new HashSet<Set<OWLLogicalAxiom>>();
		
		//compute all justifications by using HST method
		return this.computingAllJustificationsHST(ont, justifications, currentPath, allPaths);
	}
	
	
	
	/**
	 * compute all justifications by using HST method.
	 * 
	 * @param ont
	 * @param justifications
	 * @param currentPath
	 * @param allPaths
	 * @return
	 */
	private Set<Set<OWLLogicalAxiom>> computingAllJustificationsHST(
			OWLOntology ont, Set<Set<OWLLogicalAxiom>> justifications, 
			Stack<OWLLogicalAxiom> currentPath, Set<Set<OWLLogicalAxiom>> allPaths ) {
		
		if(!allPaths.isEmpty()) {
			for(Set<OWLLogicalAxiom> path:allPaths) {
				if(currentPath.containsAll(path)) {
					// Path termination without consistency check
//					System.out.println("{path termination without consistency check}\n");
					return justifications; 
				}
			}
		}
		
		this.reasoner.loadOntology(ont);
		if(this.reasoner.isConsistent()) {
			this.reasoner.unloadOntology(ont);
			// end of search in this path, return
			Set<OWLLogicalAxiom> tempSet = new HashSet<OWLLogicalAxiom>();
			for(OWLLogicalAxiom a: currentPath) {
				tempSet.add(a);
			}
			allPaths.add(tempSet);
			// backtrack path by pop operator
//			currentPath.pop();
//			System.out.println("path termination");
			return justifications;
		}
		this.reasoner.unloadOntology(ont);
		
		Set<OWLLogicalAxiom> currentJustification = new HashSet<OWLLogicalAxiom>();
		if(!justifications.isEmpty()) {
			for(Set<OWLLogicalAxiom> just:justifications) {
				if(!this.isIntesected(just, currentPath)) {
					// justification reuse (save recomputing a justification)
					currentJustification = just;
//					System.out.print("{justification reuse}\n");
					break;
				}
			}
		}
		
		if(currentJustification.isEmpty()) {
			// compute a single justification
			currentJustification = this.computeSingleJustification(ont);
			System.out.print("{Got one justification}\n");
			if(currentJustification.isEmpty()) {
				System.out.println("**********shouldn't get here.");
			}
			justifications.add(currentJustification);
		}
		
		for(OWLLogicalAxiom axiom:currentJustification) {
			// search from current justification
			currentPath.push(axiom);
//			System.out.print(axiom+"------>");
			RemoveAxiom remove = new RemoveAxiom(ont,axiom);
			try {
				this.manager.applyChange(remove);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			this.computingAllJustificationsHST(ont, justifications, currentPath, allPaths);
			currentPath.pop();
//			System.out.print("<------"+axiom);
			AddAxiom add = new AddAxiom(ont,axiom);
			try {
				this.manager.applyChange(add);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
		}
		
		// return all justifications
		return justifications;
	}
	
	
	
	/**
	 * Compute a single justification for a given inconsistent ontology.
	 * @param ont
	 * @return
	 */
	public Set<OWLLogicalAxiom> computeSingleJustification(OWLOntology ont) {
		Set<OWLLogicalAxiom> supportedSet = new HashSet<OWLLogicalAxiom>();
		
		// return the justification computed by using binary search method
		return computeSingleJustificationBinarySearch(supportedSet,ont.getLogicalAxioms(), ont.getURI());
		
		//return the justifcation computing by simply going through all axims
//		return this.computeSingleJustificationSimple(ont.getLogicalAxioms(), ont.getURI());
	}
	
	public Set<OWLLogicalAxiom> computingJustificationContainingAxiomBinarySearch(OWLOntology ont, Set<OWLLogicalAxiom> as) {
		Set<OWLLogicalAxiom> supportedSet = new HashSet<OWLLogicalAxiom>();
		Set<OWLLogicalAxiom> mis = computeSingleJustificationBinarySearch(supportedSet,ont.getLogicalAxioms(), ont.getURI());
		
		Iterator<OWLLogicalAxiom> iter = as.iterator();
		while(iter.hasNext()) {
			if(mis.contains(iter.next())) {
				return mis;
			}
		}
		return null;
	}
	
	
	/**
	 * Compute one justification by using binary search method.
	 * @param supportedSet
	 * @param axiomSet
	 * @param uri
	 * @return
	 */
	public Set<OWLLogicalAxiom> computeSingleJustificationBinarySearch(
			Set<OWLLogicalAxiom> supportedSet, Set<OWLLogicalAxiom> axiomSet, URI uri) {

		// if there are only one axiom in the investigated axiom set, just return it
		if(axiomSet.size()==1) {
			return axiomSet;
		}
		
		// split the axiom set into left halve and right halve
		Set<OWLLogicalAxiom> leftSet = new HashSet<OWLLogicalAxiom>();
		Set<OWLLogicalAxiom> rightSet = new HashSet<OWLLogicalAxiom>();
		int half = axiomSet.size()/2;
		Iterator<OWLLogicalAxiom> iter=axiomSet.iterator();
		while( iter.hasNext()) {
			half--;
			if(half>=0) {
				leftSet.add(iter.next());
			} else {
				rightSet.add(iter.next());
			}
		}
		
		// create an ontology for inconsistency checking
		OWLOntology testedOnt = null;
		try {
			testedOnt = this.manager.createOntology(uri);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		// check if union of supported set and left halve is consistent 
		if(!supportedSet.isEmpty()) {
			this.addAxiomstoOnt(testedOnt, supportedSet);
		}
		this.addAxiomstoOnt(testedOnt, leftSet);
		this.reasoner.loadOntology(testedOnt);
		if(!this.reasoner.isConsistent()){
			this.reasoner.unloadOntology(testedOnt);
			// if it is inconsistent, recursively compute
			this.removeAxiomsfromOnt(testedOnt, leftSet);
			this.removeAxiomsfromOnt(testedOnt, supportedSet);
			return this.computeSingleJustificationBinarySearch(supportedSet, leftSet, uri);
		}
		this.reasoner.unloadOntology(testedOnt);
		this.removeAxiomsfromOnt(testedOnt, leftSet);
		
		// check if union of supported set and right halve is consistent
		this.addAxiomstoOnt(testedOnt, rightSet);
		this.reasoner.loadOntology(testedOnt);
		if(!this.reasoner.isConsistent()) {
			this.reasoner.unloadOntology(testedOnt);
			// if it is inconsistent, recursively compute
			this.removeAxiomsfromOnt(testedOnt, rightSet);
			this.removeAxiomsfromOnt(testedOnt, supportedSet);
			return this.computeSingleJustificationBinarySearch(supportedSet, rightSet, uri);
		}
		this.reasoner.unloadOntology(testedOnt);
		this.removeAxiomsfromOnt(testedOnt, rightSet);
		this.removeAxiomsfromOnt(testedOnt, supportedSet);
		
		// if union of supported set and left halve is consistent, 
		// put right halve into the supportedSet and recursively compute
		supportedSet.addAll(rightSet);
	
		//finding the minimal axiom set of one justification in the current left halve
		Set<OWLLogicalAxiom> minLeftResult = this.computeSingleJustificationBinarySearch(supportedSet, leftSet, uri);
		
		Set<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		result.addAll(minLeftResult);
		
		// if union of supported set and right halve is consistent,
		// put the above computed minimal left result into the supported set and recursively compute
		supportedSet.removeAll(rightSet);
//		supportedSet.addAll(leftSet);
		supportedSet.addAll(minLeftResult);
		result.addAll(this.computeSingleJustificationBinarySearch(supportedSet, rightSet, uri));
		
		// return the union of minimal left part and minimal right part
		return result;
	}
	
	
	
	public Set<OWLLogicalAxiom> computeSingleJustificationSimple(Set<OWLLogicalAxiom> axiomSet, URI uri) {
		
		OWLOntology mis = null;
		try {
			mis = manager.createOntology(uri);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		this.addAxiomstoOnt(mis, axiomSet);
		
		for(OWLLogicalAxiom a:axiomSet) {
			RemoveAxiom remove = new RemoveAxiom(mis,a);
			try {
				manager.applyChange(remove);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			reasoner.loadOntology(mis);
			if(reasoner.isConsistent()) {
				AddAxiom add = new AddAxiom(mis,a);
				try {
					manager.applyChange(add);
				} catch (OWLOntologyChangeException e) {
					e.printStackTrace();
				}
			}
			reasoner.unloadOntology(mis);
		}
		
		return mis.getLogicalAxioms();
		
	}
	
	
	
	/**
	 * Determine if two OWLLogicalAxiom set is intersected.
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean isIntesected(Set<OWLLogicalAxiom> s1, Stack<OWLLogicalAxiom> s2) {
		for(OWLAxiom a:s1) {
			if(s2.contains(a)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * Remove a set of OWLLogical axioms from an ontology.
	 * 
	 * @param ont
	 * @param s
	 */
	private void removeAxiomsfromOnt(OWLOntology ont, Set<OWLLogicalAxiom> s) {
				
		Iterator<OWLLogicalAxiom> iter = s.iterator();
		List<RemoveAxiom> removedList = new ArrayList<RemoveAxiom>();
		
		while(iter.hasNext()) {
			RemoveAxiom removedAxiom = new RemoveAxiom(ont, iter.next());
			removedList.add(removedAxiom);
		}
		
		try {
			manager.applyChanges(removedList);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
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
	
	
	/**
	 * compute a justification which contains a specific axioms set
	 * @param ont
	 * @param as
	 * @return
	 */
	public Set<OWLLogicalAxiom> computingJustificationContainingAxiom(OWLOntology ont, Set<OWLLogicalAxiom> as) {
		Set<Set<OWLLogicalAxiom>> justifications = new HashSet<Set<OWLLogicalAxiom>>();
		Stack<OWLLogicalAxiom> currentPath = new Stack<OWLLogicalAxiom>();
		Set<Set<OWLLogicalAxiom>> allPaths = new HashSet<Set<OWLLogicalAxiom>>();
		
		//compute all justifications by using HST method
		return this.computingJustificationContainingAxiomHST(ont, as, justifications, currentPath, allPaths);
	}
	
	/**
	 * compute a justification which contains a specific axioms set by using a simple pruning method
	 * @param ont
	 * @param as
	 * @return
	 */
	public Set<OWLLogicalAxiom> computingJustificationContainingAxiomSimple(OWLOntology ont, Set<OWLLogicalAxiom> as) {
		OWLOntology mis = null;
		try {
			mis = manager.createOntology(ont.getURI());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Set<OWLLogicalAxiom> axiomSet = ont.getLogicalAxioms();
		this.addAxiomstoOnt(mis, axiomSet);
		
		for(OWLLogicalAxiom a:axiomSet) {
			if(as.contains(a)) {
				continue;
			}
			RemoveAxiom remove = new RemoveAxiom(mis,a);
			try {
				manager.applyChange(remove);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			reasoner.loadOntology(mis);
			if(reasoner.isConsistent()) {
				AddAxiom add = new AddAxiom(mis,a);
				try {
					manager.applyChange(add);
				} catch (OWLOntologyChangeException e) {
					e.printStackTrace();
				}
			}
			reasoner.unloadOntology(mis);
		}
		
		if(mis.getLogicalAxioms().size()==as.size()) {
			return null;
		}
		this.removeAxiomsfromOnt(mis, as);
		
		reasoner.loadOntology(mis);
		if(reasoner.isConsistent()) {
			this.addAxiomstoOnt(mis,as);
		} else {
			return null;
		}
		reasoner.unloadOntology(mis);
		
		return mis.getLogicalAxioms();
	}
	
	/**
	 * compute a justifacation which includes a set of axioms by using HST method
	 * @param ont
	 * @param as
	 * @param justifications
	 * @param currentPath
	 * @param allPaths
	 * @return
	 */
	private Set<OWLLogicalAxiom> computingJustificationContainingAxiomHST(OWLOntology ont, 
			Set<OWLLogicalAxiom> as, Set<Set<OWLLogicalAxiom>> justifications, 
			Stack<OWLLogicalAxiom> currentPath, Set<Set<OWLLogicalAxiom>> allPaths) {
		
		if(!allPaths.isEmpty()) {
			for(Set<OWLLogicalAxiom> path:allPaths) {
				if(currentPath.containsAll(path)) {
					// Path termination without consistency check
//					System.out.println("{path termination without consistency check}");
					return null; 
				}
			}
		}
		
		this.reasoner.loadOntology(ont);
		if(this.reasoner.isConsistent()) {
			this.reasoner.unloadOntology(ont);
			// end of search in this path, return
			Set<OWLLogicalAxiom> tempSet = new HashSet<OWLLogicalAxiom>();
			for(OWLLogicalAxiom axiom: currentPath) {
				tempSet.add(axiom);
			}
			allPaths.add(tempSet);
			// backtrack path by pop operator
//			currentPath.pop();
//			System.out.println("path termination");
			return null;
		}
		this.reasoner.unloadOntology(ont);
		
		Set<OWLLogicalAxiom> currentJustification = new HashSet<OWLLogicalAxiom>();
		if(!justifications.isEmpty()) {
			for(Set<OWLLogicalAxiom> just:justifications) {
				if(!this.isIntesected(just, currentPath)) {
					// justification reuse (save recomputing a justification)
					currentJustification = just;
//					System.out.print("{justification reuse}\n");
					break;
				}
			}
		}
		
		if(currentJustification.isEmpty()) {
			// compute a single justification
			currentJustification = this.computeSingleJustification(ont);
//			System.out.print("{Got one justification}\n");
			if(currentJustification.isEmpty()) {
				System.out.println("**********shouldn't get here.");
			} else {
				if(currentJustification.containsAll(as)) {
					return currentJustification;
				}
			}
			justifications.add(currentJustification);
		}
		
		for(OWLLogicalAxiom axiom:currentJustification) {
			// search from current justification
			currentPath.push(axiom);
//			System.out.print(axiom+"------>");
			RemoveAxiom remove = new RemoveAxiom(ont,axiom);
			try {
				this.manager.applyChange(remove);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			if(justifications.size()>6) {
				return null;
			}
			Set<OWLLogicalAxiom> jus = this.computingJustificationContainingAxiomHST(ont, as, justifications, currentPath, allPaths);

			currentPath.pop();
//			System.out.print("<------"+axiom);
			AddAxiom add = new AddAxiom(ont,axiom);
			try {
				this.manager.applyChange(add);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			if(jus!=null&&!jus.isEmpty()) {
				return jus;
			}
		}
		
		
		return null;
	}

}
	
