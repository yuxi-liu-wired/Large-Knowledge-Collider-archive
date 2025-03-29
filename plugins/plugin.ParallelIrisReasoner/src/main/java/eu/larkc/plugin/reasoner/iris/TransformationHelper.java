package eu.larkc.plugin.reasoner.iris;

import java.util.List;
import java.util.UUID;

import org.deri.iris.api.basics.IAtom;
import org.deri.iris.storage.IRelation;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import cascading.tuple.Tuple;
import eu.larkc.plugin.reasoner.ParallelIrisReasonerConstants;

/**
 * Helper class to transform IRIS objects (Terms, Tuples, Relations) to RDF
 * statemtents (openRDF object model).
 * 
 * @author Iker Larizgoitia, Christoph Fuchs
 * 
 */
public class TransformationHelper {

	/**
	 * Creates a RDF statement from an IRIS relation. Resulting statements have
	 * the form <code>_:bNode urn:larkc.iris.result Relation</code>.
	 * 
	 * @param iRelation
	 *            the relation
	 * @return the relation as a statement
	 */
	public static Statement createStatementFromIRISConstruct(
			IRelation iRelation) {
		Statement statement = new StatementImpl(new BNodeImpl("result"),
				new URIImpl(ParallelIrisReasonerConstants.RESULT), ValueFactoryImpl
						.getInstance().createLiteral(iRelation.toString()));
		return statement;
	}
	
	/**
	 * Creates a RDF statement from an IRIS relation. Resulting statements have
	 * the form <code>_:bNode urn:larkc.iris.result Relation</code>.
	 * 
	 * @param iAtom
	 *            the atom
	 * @return the relation as a statement
	 */
	public static Statement createStatementFromIRISConstruct(
			IAtom iAtom) {
		Statement statement = new StatementImpl(new BNodeImpl("result"),
				new URIImpl(ParallelIrisReasonerConstants.RESULT), ValueFactoryImpl
						.getInstance().createLiteral(iAtom.toString()));
		return statement;
	}
	
	public static Statement createStatementFromHadoopTuple(Tuple tuple) {
		List<Object> tupleElements = Tuple.elements(tuple);
		
		// First tuple element is always the predicate
		Object rawPredicate = tupleElements.get(0);
		URIImpl predicate = new URIImpl(rawPredicate.toString());
		
		// All other tuple elements are variable bindings
		// If the predicate has an arity of 3, the first tuple element
		// represents the predicate itself, the other 2 elements represent
		// the variable bindings of e.g. ?x and ?y
		Object rawObject = tupleElements.get(1);
		Value object = new LiteralImpl(rawObject.toString());

		// Create a blank node which represents the hadoop tuple and identify it using a UUID
		BNodeImpl bNodeImpl = new BNodeImpl(UUID.randomUUID().toString());
		
		Statement statement = new StatementImpl(bNodeImpl, predicate, object);
		return statement;
	}

}
