package eu.larkc.plugin.spanningworkflowplugin;

import java.util.ArrayList;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.IllegalWorkflowGraphException;
import eu.larkc.core.data.workflow.MultiplePluginParametersException;
import eu.larkc.core.executor.ExecutorException;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.connection.PlatformConnector;
import eu.larkc.shared.SampleQueries;

/**
 * <p>
 * Generated LarKC plug-in skeleton for
 * <code>eu.larkc.plugin.spanningworkflowplugin.SpanningWorkflowPlugin</code>.
 * Use this class as an entry point for your plug-in development.
 * </p>
 */
public class SpanningWorkflowPlugin extends Plugin {

	private boolean spanWorkflow;

	/**
	 * Constructor.
	 * 
	 * @param pluginUri
	 *            a URI representing the plug-in type, e.g.
	 *            <code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public SpanningWorkflowPlugin(URI pluginUri) {
		super(pluginUri);
		spanWorkflow = false;
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
		CloseableIterator<Statement> statements = workflowDescription
				.getStatements();
		Statement stmt;
		while (statements.hasNext()) {
			stmt = statements.next();
			// find parameter
			String predicateName = stmt.getPredicate().toString();
			String predicateLocalName = stmt.getPredicate().getLocalName();
			if (predicateName
					.equals("http://larkc.eu/schema#constructWorkflow")
					|| ("http://larkc.eu/schema#" + predicateLocalName)
							.equals("http://larkc.eu/schema#constructWorkflow")) {
				if (stmt.getObject().stringValue().equals("true")) {
					spanWorkflow = true;
				}
			}
		}
		logger.debug("Construct Workflow: {}", spanWorkflow);

		System.out.println("SpanningWorkflowPlugin initialized.");
	}

	/**
	 * Called on plug-in invokation. The actual "work" should be done in this
	 * method.
	 * 
	 * @param input
	 *            a set of statements containing the input for this plug-in
	 * 
	 * @return a set of statements containing the output of this plug-in
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		SetOfStatements result = null;

		if (spanWorkflow) {
			// create new workflow
			try {
				result = PlatformConnector.runWorkflow(Workflows
						.getMinimalWorkflowDescription(), new SPARQLQueryImpl(
						SampleQueries.WHO_KNOWS_FRANK).toRDF());
			} catch (IllegalWorkflowGraphException e) {
				System.out.println("EXCEPTION: IllegalWorkflowGraphException");
			} catch (MultiplePluginParametersException e) {
				System.out
						.println("EXCEPTION: MultiplePluginParameterException");
			} catch (RepositoryException e) {
				System.out.println("EXCEPTION: RepositoryException");
			} catch (ExecutorException e) {
				System.out.println("EXCEPTION: ExecutorException");
			}

		} else {
			ArrayList<Statement> stmtList = new ArrayList<Statement>();
			Statement statement = new StatementImpl(new BNodeImpl("blank"),
					new URIImpl("urn:result"), new LiteralImpl("null"));
			stmtList.add(statement);
			result = new SetOfStatementsImpl(stmtList);
		}

		return result;
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow
	 * deletion. Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
		// TODO shutdown the internal workflow
	}
}
