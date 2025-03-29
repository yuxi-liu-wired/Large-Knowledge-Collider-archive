package eu.larkc.plugin.reasoner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.SparqlWorkflowDescription;
import eu.larkc.shared.RDFParsingUtils;

/**
 * Utility class to ease the handling of workflows.
 * 
 * @author Iker Larizgoitia, Christoph Fuchs
 * 
 */
public class WorkflowUtils {

	/**
	 * Given a filename, this method will read and parse the workflow file and
	 * return a SparqlWorkflowDescription instance.
	 * 
	 * @param workflowFile
	 *            filename of the workflow file
	 * @return the parsed workflow description
	 * @throws Exception
	 */
	public static SparqlWorkflowDescription prepareWorkflow(String workflowFile)
			throws Exception {

		URL workflowURL = WorkflowUtils.class.getResource(workflowFile);
		String workflow = FileUtils.readFileToString(new File(workflowURL
				.toURI()));
		Collection<Statement> parsedWorkflow = RDFParsingUtils
				.parseN3(workflow);
		SetOfStatements workflowDescription = new SetOfStatementsImpl(
				parsedWorkflow);
		return new SparqlWorkflowDescription(workflowDescription);
	}

	/**
	 * Creates a parameter with the URN {@link ParallelIrisReasonerConstants#QUERY} as a
	 * predicate, and the query (which is contained in the given file / URL) as
	 * a value.
	 * 
	 * @param queryURL
	 *            the URL where the query file can be retrieved
	 * @return a SetOfStatements encapsulating the query string.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static SetOfStatements createQueryParameter(URL queryURL)
			throws IOException, URISyntaxException {
		String query = FileUtils.readFileToString(new File(queryURL.toURI()));
		return createQueryParameter(query);
	}

	/**
	 * Creates a parameter with the URN {@link ParallelIrisReasonerConstants#QUERY} as a
	 * predicate, and the query as a value.
	 * 
	 * @param query
	 *            the query string
	 * @return a SetOfStatements encapsulating the query string.
	 * @return
	 */
	public static SetOfStatements createQueryParameter(String query) {
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Statement queryParameter = new StatementImpl(new BNodeImpl("query"),
				new URIImpl(ParallelIrisReasonerConstants.QUERY), new LiteralImpl(query));

		statements.add(queryParameter);

		return new SetOfStatementsImpl(statements);
	}
}
