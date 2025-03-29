package eu.larkc.plugin.identify;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for your LarKC plug-in.
 */
public class TestRemoteIdentifierTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestRemoteIdentifierTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestRemoteIdentifierTest.class );
    }

    /**
     * Rigorous Test
     */
    public void testMyPlugin()
    {
        assertTrue( true );
    }
}
