package eu.larkc.plugin.decider;

import org.openrdf.model.URI;


import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;

/**
 * This plugin enables us to forward a query from the source to multiple plugins.
 * @author Bas Groenewoud and Chris Dijkshoorn
 *
 */
public class SourceSplitter extends Plugin {
	//only first time when called, return results (anytime b.)
	private boolean once = false;

	public SourceSplitter(URI pluginName) {
		super(pluginName);
	}


	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
	}

	public SetOfStatements invokeInternal(SetOfStatements input) {
		System.out.println("SourceSplitter:invokeInternal-start");
		if (once) { return null; }
		once = true;

		logger.debug("Splitting input source!");
		System.out.println("SourceSplitter:invokeInternal-end");
		return input;
	}

	public void shutdownInternal() {
	}

}