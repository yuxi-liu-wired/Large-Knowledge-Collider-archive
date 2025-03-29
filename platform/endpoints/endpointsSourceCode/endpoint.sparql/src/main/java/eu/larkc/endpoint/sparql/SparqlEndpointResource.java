package eu.larkc.endpoint.sparql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.restlet.Application;
import org.restlet.Request;
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

import eu.larkc.core.endpoint.Endpoint;
import eu.larkc.core.executor.Executor;
import eu.larkc.endpoint.sparql.exceptions.MalformedSparqlQueryException;
import eu.larkc.endpoint.sparql.exceptions.SparqlException;

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
public class SparqlEndpointResource extends ServerResource {

	private static Logger logger = LoggerFactory
			.getLogger(SparqlEndpointResource.class);

	/**
	 * Parses an URL-encoded SPARQL query request, as described in sec. 2.2 of
	 * the SPARQL Protocol for RDF.
	 * 
	 * @param url
	 *            The URL to be used in error messages.
	 * @param query
	 *            The query part a URL, which is to be parsed.
	 * @return An instance of SparqlQueryRequest containing data extracted from
	 *         the URL.
	 * @exception MalformedSparqlQueryException
	 *                Thrown if the URL is malformed or does not contains
	 *                exactly one 'query' parameter.
	 */
	protected SparqlQueryRequest parseGetUrl(java.net.URI url, String query)
			throws SparqlException {
		String[] parts = query.split("&");
		if (parts == null) {
			throw new MalformedSparqlQueryException(
					"The query part of the URL of the GET request is empty.");
		}
		SparqlQueryRequest qr = new SparqlQueryRequest();
		boolean hasQuery = false;
		for (String part : parts) {
			int eq = part.indexOf('=');
			if (eq < 0) {
				logger.warn("Warning: no \'=\' sign in \"" + part
						+ "\" in the URL \"" + url.toString() + "\".");
				continue;
			}
			String rawName = part.substring(0, eq);
			String rawValue = part.substring(eq + 1);
			String name, value;
			try {
				name = URLDecoder.decode(rawName, "UTF-8");
				value = URLDecoder.decode(rawValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new MalformedSparqlQueryException(e.toString());
			}
			if (name.equals("query")) {
				if (hasQuery) {
					throw new MalformedSparqlQueryException(
							"More than one value of the \""
									+ name
									+ "\" parameter is being provided (first \""
									+ qr.getQuery() + "\", then \"" + value
									+ "\").");
				}
				hasQuery = true;
				qr.setQuery(value);
			} else if (name.equals("default-graph-uri")) {
				qr.addDefaultGraphUri(value);
			} else if (name.equals("named-graph-uri")) {
				qr.addNamedGraphUri(value);
			} else {
				throw new MalformedSparqlQueryException(
						"Unknown parameter name: \""
								+ name
								+ "\"; should be \"query\", \"default-graph-uri\" or \"named-graph-uri\".");
				// logger.debug("Unknown parameter: {}, {}", name, value);
			}
		}
		if (!hasQuery) {
			throw new MalformedSparqlQueryException(
					"The required parameter \"query\" is missing.");
		}
		return qr;
	}

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
	public Representation executeQueryPost(Representation entity)
			throws Exception {
		logger.debug("HTTP POST called ...");

		// check content type
		logger.debug("ContentType " + entity.getMediaType());
		String contentType = entity.getMediaType().toString();
		if (!contentType.contains("application/x-www-form-urlencoded")) {
			throw new MalformedSparqlQueryException(
					"Unsupported Content-Type in the HTTP request: \""
							+ contentType
							+ "\".  Only application/x-www-form-urlencoded is supported.");
		}

		String query="";
		// Read the body of the request.
		InputStream is = entity.getStream();
		if (is != null){
			StringBuilder reqBody = new StringBuilder();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			final int capacity = 8192;
			char[] buf = new char[capacity];
			int count;
			while ((count = isr.read(buf, 0, capacity)) > 0) {
				reqBody.append(buf, 0, count);
			}
			isr.close();
			query = reqBody.toString();
		}
		else
		{
			// get the query out of the HTTP POST command.
			Form form = new Form(entity);
			final String q = form.getFirstValue("query");
			if (q == null) {
				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				throw new Exception("Empty query!");
			}
			query=q;
		}

		
	
		java.net.URI uri = getRequest().getOriginalRef().toUri();
	
		SparqlQueryRequest queryRequest = parseGetUrl(uri, query);

		// Get the corresponding executor from the LarKC platform.
		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof SparqlEndpointApp) {
			ep = ((SparqlEndpointApp) application).getEndpoint();
			ex = ep.getExecutor();
		}
		assert (ep != null);
		assert (ex != null);
		logger.debug("Found executor " + ex.toString());

		// handle the query
		SparqlQueryHandler handler = new SparqlQueryHandler(ex, ep);
		SparqlQueryResult queryResult = handler.handleQuery(queryRequest);

		String xmlResult = SparqlQueryHandler.xmlToString(
				queryRequest.getQuery(), queryResult.getDocument());

		// set response
		setStatus(Status.SUCCESS_OK);
		Representation rep = new StringRepresentation(xmlResult,
				MediaType.register(queryResult.getContentType(),
						"SPARQL endpoint response"));
		return rep;
	}

	/**
	 * Returns the results of the workflow.
	 * 
	 * @param entity
	 * 
	 * @return the results
	 * @throws Exception
	 */
	@Get
	public Representation executeQueryGet()
			throws Exception {
		logger.debug("HTTP GET called ...");

		Request r = getRequest();
		
		
		
		// get the query out of the HTTP GET command.
		Form form = getQuery(); 
		final String q = form.getFirstValue("query");
		if (q == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			throw new Exception("Empty query!");
		}

		java.net.URI uri = new URI(r.getRootRef().toString());
		SparqlQueryRequest queryRequest = parseGetUrl(uri, form.getQueryString());

		// Get the corresponding executor from the LarKC platform.
		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof SparqlEndpointApp) {
			ep = ((SparqlEndpointApp) application).getEndpoint();
			ex = ep.getExecutor();
		}
		assert (ep != null);
		assert (ex != null);
		logger.debug("Found executor " + ex.toString());

		// handle the query
		SparqlQueryHandler handler = new SparqlQueryHandler(ex, ep);
		SparqlQueryResult queryResult = handler.handleQuery(queryRequest);

		String xmlResult = SparqlQueryHandler.xmlToString(
				queryRequest.getQuery(), queryResult.getDocument());

		// set response
		setStatus(Status.SUCCESS_OK);
		Representation rep = new StringRepresentation(xmlResult,
				MediaType.register(queryResult.getContentType(),
						"SPARQL endpoint response"));
		return rep;
	}
}
