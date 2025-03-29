package eu.larkc.plugin.reasoner.keywordreasoner;

import org.openrdf.model.impl.URIImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.workflow.SparqlWorkflowDescription;
import eu.larkc.plugin.reasoner.keywordreasoner.utils.KRUtils;
*/

/**
 * Unit test for your LarKC plug-in.
 */
public class KeywordReasonerTest extends TestCase {
	
		KeywordReasonerPlugin plugin;
		
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public KeywordReasonerTest( String testName ){
        super( testName );
        this.plugin = new KeywordReasonerPlugin(new URIImpl(KRConstants.PLUGIN_URI));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite( KeywordReasonerTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testMyPlugin(){
        assertTrue( true );
    }
    
  	/*
    public void testInvokeInternal() throws Exception {
    	SparqlWorkflowDescription workflow = KRUtils.readWorkflowDefinition("/data/KRTestWorkflow.n3");
    	SetOfStatements inputParameters = workflow.getPluginParameters("krPlugin");  	
    	SetOfStatements result = plugin.invoke(inputParameters);
    	assertNotNull(result);
    }
    */ 
}