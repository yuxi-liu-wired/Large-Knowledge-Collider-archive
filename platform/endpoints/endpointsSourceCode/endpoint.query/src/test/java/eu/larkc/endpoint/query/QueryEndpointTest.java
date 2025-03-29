package eu.larkc.endpoint.query;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class QueryEndpointTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public QueryEndpointTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( QueryEndpointTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testQueryEndpoint()
    {
        assertTrue( true );
    }
}
