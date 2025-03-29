package eu.larkc.endpoint.push;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class PushEndpointTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PushEndpointTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PushEndpointTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testPushEndpoint()
    {
        assertTrue( true );
    }
}
