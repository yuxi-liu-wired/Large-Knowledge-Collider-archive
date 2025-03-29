package eu.larkc.plugin.pariris;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.compiler.ParserException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.reasoner.ParallelIrisReasonerConstants;
import eu.larkc.plugin.reasoner.iris.TransformationHelper;
import eu.larkc.plugin.reasoner.iris.extractor.IRISDatalogExtractor;
import eu.larkc.plugin.reasoner.iris.extractor.ParameterHelper;

/**
 * <p>
 * LarKC plug-in to use the ParIris implementation with the LarKC platform.
 * </p>
 * 
 * @author Christoph Fuchs
 */
public class ParallelIrisReasoner extends Plugin {

	public static final boolean CALCULATE_BLANK_NODES = false;

	private URL factBaseURL = null;
	private URL ruleBaseURL = null;

	private CascadingEvaluator evaluator;


	// Use our own logger for easier output greping
	private static Logger logger = LoggerFactory
			.getLogger(ParallelIrisReasoner.class);

	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri
	 *            a URI representing the plug-in type, e.g.
	 *            <code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public ParallelIrisReasoner(URI pluginUri) {
		super(pluginUri);
	}

	/**
	 * Called on plug-in initialisation. The plug-in instances are initialised
	 * on workflow initialisation.
	 * 
	 * @param pluginParameters
	 *            set of statements containing plug-in specific information
	 *            which might be needed for initialization (e.g. plug-in
	 *            parameters).
	 */
	@Override
	protected void initialiseInternal(SetOfStatements pluginParameters) {
		ClassLoader previousClassloader = Thread.currentThread().getContextClassLoader();
		try {
			/*
			 * Temporarily set thread context classloader to current classloader
			 * We need to do this since the classloader of the current thread is
			 * unable to find the security module - which will ultimately result
			 * in the following exception: ClassNotFoundException:
			 * org.apache.hadoop.security.UserGroupInformation$HadoopLoginModule
			 */
		    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

			// Initialize the plug-in
			logger.debug("Plug-in parameters: {}", pluginParameters);
	
			// Extract fact- and rule-base URLs if present
			if (pluginParameters != null) {
				factBaseURL = ParameterHelper.extractURL(ParallelIrisReasonerConstants.FACT_BASE,
						pluginParameters);
				ruleBaseURL = ParameterHelper.extractURL(ParallelIrisReasonerConstants.RULE_BASE,
						pluginParameters);
			}
	
			if (factBaseURL != null && ruleBaseURL != null) {
				logger.debug(
						"Initializing cascading evaluator [factbase: {}, rulebase: {}]",
						factBaseURL, ruleBaseURL);
				evaluator = new CascadingEvaluator(factBaseURL, ruleBaseURL);
	
				logger.info("Instance of {} initialized.", ParallelIrisReasonerConstants.PLUGIN_URI);
				logger.info("Fact base URL: {}", factBaseURL);
				logger.info("Rule base URL: {}", ruleBaseURL);
				return;
			}
	
			logger.info("Instance of " + ParallelIrisReasonerConstants.PLUGIN_URI + " initialized. However, either the fact base or the rule base was null. Thus, evaluation will not work properly.");
		} finally {
			// Restore thread context classloader
			Thread.currentThread().setContextClassLoader(previousClassloader);
		}
	}

	/**
	 * Called on plug-in invokation. The actual "work" should be done in this
	 * method.
	 * 
	 * @param input
	 *            a set of statements containing the input for this plug-in
	 * 
	 * @return a set of statements containing the output of this plug-in
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		ClassLoader previousClassloader = Thread.currentThread()
				.getContextClassLoader();
		try {
			/*
			 * Temporarily set thread context classloader to current classloader
			 * We need to do this since the classloader of the current thread is
			 * unable to find the security module - which will ultimately result
			 * in the following exception: ClassNotFoundException:
			 * java.lang.ClassNotFoundException:
			 * cascading.tap.hadoop.MultiInputSplit
			 */
			Thread.currentThread().setContextClassLoader(
					this.getClass().getClassLoader());

			logger.info("Invoked with input statements\n{}", input);
			logger.info("Input statements will be ignored, since query should be present in fact- or rule-base file.");

			// Retrieve the query from the input
			CloseableIterator<Statement> statements = input.getStatements();

			// First, extract every query statement in the form of
			// <?s urn:larkc.pariris.query "?- theQuery(?x)."> and put them in
			// on string (just the query srings) which can be easily parsed
			// later
			StringBuilder allQueries = new StringBuilder();
			while (statements.hasNext()) {
				Statement stmnt = statements.next();
				URI predicate = stmnt.getPredicate();
				String predicateString = predicate.toString();
				if (predicateString.equals(ParallelIrisReasonerConstants.QUERY)
						|| predicateString
								.equals(ParallelIrisReasonerConstants.GENERIC_LARKC_QUERY)) {
					allQueries.append(stmnt.getObject().stringValue());
				}
			}

			// Now parse the long query string to separate IQueries
			IRISDatalogExtractor extractor = null;
			try {
				extractor = new IRISDatalogExtractor(allQueries.toString());
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<IQuery> extractedQueries = extractor.getQueries();
			logger.info("Found {} queries in input statements",
					extractedQueries.size());

			// Evaluate each query
			List<IAtom> resultList = new LinkedList<IAtom>();
			for (IQuery query : extractedQueries) {
				try {
					logger.info("Evaluating query {}", query);
					List<IAtom> evaluation = evaluator.evaluate(query);
					resultList.addAll(evaluation);
				} catch (Exception e1) {
					logger.warn("Unable to evaluate query {}", query);
					e1.printStackTrace();
				}
			}

			try {
				logger.info("Result: {}", resultList);

				RdfGraph g = createGraphFromQueryResults(resultList,
						new URIImpl(
								"http://www.example.com/ParallelIrisReasoner"));

				// Return the graph
				return (SetOfStatements) g;

			} catch (IllegalArgumentException iae) {
				logger.error(iae.getLocalizedMessage());
				iae.printStackTrace();
			}
		} finally {
			// Restore thread context classloader
			Thread.currentThread().setContextClassLoader(previousClassloader);
		}

		// For some reason, the graph was not returned up to now.
		// Evaluation likely failed, thus return null.
		return null;
	}

	/**
	 * Method to create a RDF graph from the result of the IRIS query answers.
	 * It just created statements for each query result in the form of
	 * <code>_:result rdf:value "query output"</code>
	 * 
	 * @param resultList
	 *            list of IRelation objects
	 * @param graphURI
	 *            the URI of the graph (named graph)
	 * @return a {@link RdfGraph}
	 */
	private static RdfGraph createGraphFromQueryResults(
			List<IAtom> resultList, URI graphURI) {
		List<Statement> listOfStatement = new ArrayList<Statement>();

		for (IAtom iAtom : resultList) {
			if (iAtom == null)
				continue;
			
			Statement statement = TransformationHelper
					.createStatementFromIRISConstruct(iAtom);
			listOfStatement.add(statement);
		}

		RdfGraph g = DataFactory.INSTANCE.createRdfGraph(listOfStatement,
				graphURI);
		return g;
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow
	 * deletion. Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
		// Nothing to clean up for now.
	}

	/**
	 * Getter. Returns the base URL.
	 * 
	 * @return rule base URL.
	 */
	public URL getRuleBaseURL() {
		return this.ruleBaseURL;
	}

	/**
	 * Getter. Returns the fact base URL.
	 * 
	 * @return fact base URL.
	 */
	public URL getFactBaseURL() {
		return this.factBaseURL;
	}
}
