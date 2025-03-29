package eu.larkc.plugin.reasoner.iris.extractor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

import eu.larkc.core.data.workflow.SparqlWorkflowDescription;
import eu.larkc.plugin.reasoner.ParallelIrisReasonerConstants;
import eu.larkc.plugin.reasoner.WorkflowUtils;

/**
 * Tests helper class to extract parameters.
 * 
 * @author Iker Larizgoitia
 * @see ParameterHelper
 * 
 */
public class ParameterHelperTest {

	/**
	 * Loads a workflow, get's the plug-in parameters for "irisPlugin" and tests
	 * if the extracted parameter FACT_BASE ends with "factbase" and is thus
	 * correct.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExtractFactbaseURL() throws Exception {
		SparqlWorkflowDescription workflow = WorkflowUtils
				.prepareWorkflow("/TestWorkflowRecursiveRules.n3");

		URL factbase = ParameterHelper.extractURL(ParallelIrisReasonerConstants.FACT_BASE,
				workflow.getPluginParameters("irisPlugin"));

		assertNotNull(factbase);
		assertTrue(factbase.toString().endsWith("recursive.nt"));
	}

	/**
	 * Loads a workflow, get's the plug-in parameters for "irisPlugin" and tests
	 * if the extracted parameter RULE_BASE ends with "rulebase" and is thus
	 * correct.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExtractRulebaseURL() throws Exception {
		SparqlWorkflowDescription workflow = WorkflowUtils
				.prepareWorkflow("/TestWorkflowRecursiveRules.n3");

		URL factbase = ParameterHelper.extractURL(ParallelIrisReasonerConstants.RULE_BASE,
				workflow.getPluginParameters("irisPlugin"));

		assertNotNull(factbase);
		assertTrue(factbase.toString().endsWith("recursive.xml"));
	}

}
