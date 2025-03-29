package eu.larkc.plugin.transform;

import org.openrdf.model.URI;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;

/**
 * <p>
 * Generated LarKC plug-in skeleton for
 * <code>eu.larkc.plugin.transform.TestTransformer</code>. Use this class as an
 * entry point for your plug-in development.
 * </p>
 */
public class TestTransformer extends Plugin {

	/**
	 * Constructor.
	 * 
	 * @param pluginUri
	 *            a URI representing the plug-in type, e.g.
	 *            <code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public TestTransformer(URI pluginUri) {
		super(pluginUri);
	}

	/**
	 * Called on plug-in initialization. The plug-in instances are initialized
	 * on workflow initialization.
	 * 
	 * @param workflowDescription
	 *            set of statements containing plug-in specific information
	 *            which might be needed for initialization (e.g. plug-in
	 *            parameters).
	 */
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		logger.info("TestTransformer initialized.");
	}

	/**
	 * Called on plug-in invocation. The actual "work" should be done in this
	 * method.
	 * 
	 * @param input
	 *            a set of statements containing the input for this plug-in
	 * 
	 * @return a set of statements containing the output of this plug-in
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info("TestTransformer invokeInternal called ...");
		return input;
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow
	 * deletion. Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
	}
}
