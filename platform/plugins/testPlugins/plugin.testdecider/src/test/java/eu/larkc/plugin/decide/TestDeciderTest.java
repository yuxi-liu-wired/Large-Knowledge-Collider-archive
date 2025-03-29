package eu.larkc.plugin.decide;

import java.util.ArrayList;

import org.openrdf.model.Statement;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.util.RDFConstants;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class TestDeciderTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDeciderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestDeciderTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testMyPlugin()
    {
    	TestDecider td = new TestDecider(new URIImpl("urn:eu.larkc.plugin.decider.TestDecider"));
    	td.initialise(null);
    	
    	Statement stmt1 = new StatementImpl(new BNodeImpl("test"), RDFConstants.RDF_TYPE,
				RDFConstants.LARKC_SPARQLQUERY);
    	
    	ArrayList<Statement> stmts = new ArrayList<Statement>();
    	stmts.add(stmt1);
		SetOfStatements sos = new SetOfStatementsImpl(stmts);
    	SetOfStatements result = td.invoke(sos);
    	
        assertEquals(sos, result);
    }
}
