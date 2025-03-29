package eu.larkc.endpoint.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class TestEndpointTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestEndpointTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestEndpointTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testTestEndpoint()
    {
        assertTrue( true );
    }
}
