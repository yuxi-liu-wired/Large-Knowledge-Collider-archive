package eu.larkc.endpoint.active;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class ActiveEndpointTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ActiveEndpointTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ActiveEndpointTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testActiveEndpoint()
    {
        assertTrue( true );
    }
}
