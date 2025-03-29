package eu.larkc.plugin.reasoner.iris.extractor;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.storage.IRelation;

/**
 * Extractor interface to extract facts, rules and queries from various data
 * sources. Implementations of this class should be used to prepare any input
 * for the IRIS reasoner plug-in (the LarKC plug-in named 'IRISReasonerPlugin').
 * 
 * @author Christoph Fuchs
 * 
 */
public interface IRISExtractor {

	/**
	 * Returns all facts this extractor has extracted.
	 * 
	 * @return facts, represented as a Map with an IRIS IPredicate as a key, and
	 *         an IRIS IRelation as value.
	 */
	public Map<IPredicate, IRelation> getFacts();

	/**
	 * Returns all rules this extractor has extracted.
	 * 
	 * @return rules, represented as a List containing IRIS IRule elements.
	 */
	public List<IRule> getRules();

	/**
	 * Returns all queries this extractor has extracted.
	 * 
	 * @return queries, represented as a List containing IRIS IQuery elements.
	 */
	public List<IQuery> getQueries();
}
