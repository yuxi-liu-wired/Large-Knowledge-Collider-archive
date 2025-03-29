package eu.larkc.endpoint.query;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.restlet.Application;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.endpoint.Endpoint;
import eu.larkc.core.endpoint.query.QueryEndpoint;
import eu.larkc.core.executor.Executor;
import eu.larkc.shared.SerializationHelper;

/**
 * The current Endpoint implementation offers two basic methods:
 * <ul>
 * <li>
 * <code>HTTP POST http://host:port/endpoint?query=QUERY_AS_STRING</code> <br/>
 * passes any query to the corresponding path and starts execution,</li>
 * <li><code>HTTP GET http://host:port/endpoint</code><br/>
 * retrieves the results as RDF/XML.</li>
 * </ul>
 */
public class QueryEndpointResource extends ServerResource {

	/**
	 * Accepted parameter
	 */
	private static final String PARAMETER_NAME = "query";
	private static Logger logger = LoggerFactory
			.getLogger(QueryEndpointResource.class);

	/**
	 * This method executes a query.
	 * 
	 * @param entity
	 *            the entity
	 * @return web page
	 * @throws Exception
	 *             if the query is empty
	 */
	@Post
	public Representation executeQuery(Representation entity) throws Exception {
		Form form = new Form(entity);
		final String q = form.getFirstValue(PARAMETER_NAME);

		if (q == null) {
			logger.warn("Empty query! The workflow will be invoked with a NULL query. Invocation might not work if the input plug-in expects a query.");
		}

		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof QueryEndpoint) {
			ep = ((QueryEndpoint) application).getEndpoint();
			ex = ep.getExecutor();
		}

		assert (ep != null);
		assert (ex != null);

		// Prepare a triple which holds the query
		Set<Statement> statementSet = new HashSet<Statement>();
		Resource subject = new BNodeImpl(PARAMETER_NAME);
		URI predicate = WorkflowDescriptionPredicates.PLUGIN_PARAMETER_QUERY;
		Value object = new LiteralImpl(q);
		statementSet.add(new StatementImpl(subject, predicate, object));

		// Pass a set of statements containing the query to the executor
		SetOfStatements setOfStatementsImpl = new SetOfStatementsImpl(
				statementSet);

		ex.execute(setOfStatementsImpl, ep.getPathId());

		// Prepare the Representation that is returned (and e.g. displayed in the browser).
		StringBuilder sb = new StringBuilder();
		sb.append("<p>Query submitted to endpoint <b>");
		sb.append(ep.getURI());
		sb.append("</b>.</p>");
		// set response
		setStatus(Status.SUCCESS_CREATED);

		Representation rep = new StringRepresentation(sb.toString(),
				MediaType.TEXT_HTML);
		return rep;
	}

	/**
	 * Returns the results of the workflow.
	 * 
	 * @return the results
	 */
	@Get
	public Representation getResults() {
		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof QueryEndpoint) {
			ep = ((QueryEndpoint) application).getEndpoint();
			ex = ep.getExecutor();
		}

		assert (ep != null);
		assert (ex != null);

		// Get the next results
		SetOfStatements nextResults = ex.getNextResults(ep.getPathId());

		// If the result is empty, return an empty rdf/xml document
		if (nextResults == null) {
			return new StringRepresentation("", MediaType.APPLICATION_RDF_XML);
		}

		// Transform the SetOfStatements in an RDF/XML string
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		RDFXMLWriter writer = new RDFXMLWriter(byteStream);
		SerializationHelper.printSetOfStatements(nextResults, byteStream,
				writer);
		String serializedStatements = new String(byteStream.toByteArray());

		// Set mime-type to APP RDF/XML and return result
		Representation rep = new StringRepresentation(serializedStatements,
				MediaType.APPLICATION_RDF_XML);

		return rep;
	}
}
