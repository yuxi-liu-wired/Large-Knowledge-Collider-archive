package eu.larkc.plugin.connection;

import org.openrdf.repository.RepositoryException;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.workflow.IllegalWorkflowGraphException;
import eu.larkc.core.data.workflow.MultiplePluginParametersException;
import eu.larkc.core.executor.Executor;
import eu.larkc.core.executor.ExecutorException;

/**
 * This class implements a connection to the platform to ease the creation and
 * execution of a workflow for the user.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class PlatformConnector {

	public static SetOfStatements runWorkflow(
			SetOfStatements workflowDescription, SetOfStatements query)
			throws IllegalWorkflowGraphException,
			MultiplePluginParametersException, RepositoryException, ExecutorException {
		Executor executor;
		executor = new Executor(workflowDescription);
		executor.execute(query);
		return executor.getNextResults();
	}

	public static SetOfStatements runWorkflow(String workflowDescription,
			SetOfStatements query) {
		// TODO implement
		return null;
	}

}
