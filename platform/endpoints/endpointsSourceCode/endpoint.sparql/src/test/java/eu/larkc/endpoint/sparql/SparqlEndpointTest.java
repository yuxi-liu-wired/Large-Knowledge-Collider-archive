package eu.larkc.endpoint.sparql;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class SparqlEndpointTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SparqlEndpointTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SparqlEndpointTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testSparqlEndpoint()
    {
        assertTrue( true );
    }
}
