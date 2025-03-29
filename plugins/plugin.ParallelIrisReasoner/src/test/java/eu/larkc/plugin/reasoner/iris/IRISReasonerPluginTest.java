package eu.larkc.plugin.reasoner.iris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.workflow.SparqlWorkflowDescription;
import eu.larkc.plugin.pariris.ParallelIrisReasoner;
import eu.larkc.plugin.reasoner.ParallelIrisReasonerConstants;
import eu.larkc.plugin.reasoner.WorkflowUtils;
import eu.larkc.plugin.reasoner.iris.extractor.ParameterHelper;

/**
 * Tests the initialiseInternal and invokeInternal methods of the
 * IRISReasonerPlugin implementation.
 * 
 * @author Iker Larizgoitia, Christoph Fuchs
 * @see ParallelIrisReasoner
 * 
 */
public class IRISReasonerPluginTest {

	ParallelIrisReasoner plugin;

	@Before
	public void setUp() throws Exception {
		plugin = new ParallelIrisReasoner(new URIImpl(
				ParallelIrisReasonerConstants.PLUGIN_URI));
	}

	/**
	 * Tests plug-in initialization without any parameters.
	 */
	@Test
	public void testInitialiseWithNull() {
		plugin.initialise(null);
	}

	/**
	 * Loads a test workflow and makes sure that the IRIS knowledge base was
	 * created (is not null).
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInitialiseInternal() throws Exception {

		SparqlWorkflowDescription workflow = WorkflowUtils
				.prepareWorkflow("/TestWorkflowRecursiveRules.n3");

		plugin.initialise(workflow.getPluginParameters("irisPlugin"));

		assertNotNull(plugin.getRuleBaseURL());
		assertNotNull(plugin.getFactBaseURL());

	}

	/**
	 * Load a test workflow and ask a query. The query is represented as a
	 * string in a file (in this case in /datalog/query). The actual results are
	 * checked via toString.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvokeInternalRecursiveWorkflow() throws Exception {

		SparqlWorkflowDescription workflow = WorkflowUtils
				.prepareWorkflow("/TestWorkflowRecursiveRules.n3");

		plugin.initialise(workflow.getPluginParameters("irisPlugin"));

		SetOfStatements inputParameters = WorkflowUtils
				.createQueryParameter(this.getClass().getResource(
						"/datalog/query"));

		SetOfStatements result = plugin.invoke(inputParameters);
		String resultString = ParameterHelper.extractString(
				ParallelIrisReasonerConstants.RESULT, result);

		assertEquals(
				"http://www.w3.org/2000/01/rdf-schema#p('http://dbpedia.org/ontology/1', 'http://dbpedia.org/ontology/3')",
				resultString);
		assertNotNull(result);

	}

}
