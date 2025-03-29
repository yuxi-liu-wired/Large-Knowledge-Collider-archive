package eu.larkc.endpoint.active;

import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.restlet.Application;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.endpoint.Endpoint;
import eu.larkc.core.executor.Executor;
import eu.larkc.core.query.SPARQLQueryImpl;

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
public class ActiveEndpointResource extends ServerResource {

	private static Logger logger = LoggerFactory
			.getLogger(ActiveEndpointResource.class);
	private static String HTML_PART1 = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /> "
			+ "<title>Collected Appointments</title>"
			+ "<style type=\"text/css\">"
			+ ".err {"
			+ "	color: #F00;"
			+ "}"
			+ ".ok { "
			+ "	color: #0A0; "
			+ "}"
			+ "</style>"
			+ "</head>"
			+ "<body>" + "<h1> List of collected appointments</h1>";

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
		logger.info("Active endpoint Call Start");

		// Get the corresponding executor from the LarKC platform.
		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof ActiveEndpointApp) {
			ep = ((ActiveEndpointApp) application).getEndpoint();
			ex = ep.getExecutor();
		}
		assert (ep != null);
		assert (ex != null);
		logger.debug("Found executor " + ex.toString());

		// ex.execute(new SetOfStatementsImpl(statements), this.getPathId());
		ex.execute(new SetOfStatementsImpl(), ep.getPathId());

		// Prepare the Representation that is returned (and e.g. displayed in
		// the browser).
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
		// Get the corresponding executor from the LarKC platform.
		Application application = this.getApplication();
		Executor ex = null;
		Endpoint ep = null;
		if (application instanceof ActiveEndpointApp) {
			ep = ((ActiveEndpointApp) application).getEndpoint();
			ex = ep.getExecutor();
		}
		assert (ep != null);
		assert (ex != null);
		logger.debug("Found executor " + ex.toString());

		SetOfStatements s = new SetOfStatementsImpl();
		ex.execute(s, ep.getPathId());
		SetOfStatements st = ex.getNextResults(ep.getPathId());

		StringBuffer sResponse = new StringBuffer();
		sResponse.append(HTML_PART1 + generateEvents(st) + "</body></html>");

		// Set mime-type to TEXT/HTML and return result.
		Representation rep = new StringRepresentation(sResponse.toString(),
				MediaType.TEXT_HTML);

		logger.info("PushEndpoint Call End");

		return rep;
	}

	/**
	 * Reads the statements and generates the html events
	 * 
	 * @param st
	 * @return
	 */
	private String generateEvents(SetOfStatements st) {
		// Hashtable<String, String> events = new Hashtable<String, String>();
		String sResponse = "";
		RdfStoreConnection con = DataFactory.INSTANCE
				.createRdfStoreConnection();
		VariableBinding vb = con
				.executeSelect(new SPARQLQueryImpl(
						"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
								+ "PREFIX cyc:<http://example.com/cyc#>"
								+ "PREFIX tripit:<http://example.com/TripitPlugin/tripit#>"
								+ "PREFIX time:<http://www.w3.org/2006/time#>"
								+ "SELECT DISTINCT ?NAME " + "WHERE"
								+ "{ ?P rdf:type cyc:event."
								+ "  ?P cyc:eventHasLocation ?LOC."
								+ "  ?P cyc:preferredNameString ?NAME."
								+ "  ?P cyc:starts ?START."
								+ "  ?P cyc:ends ?END." + "}"));
		CloseableIterator<Binding> iter = vb.iterator();
		int iPersonNum = 0;
		while (iter.hasNext()) {
			Binding b = iter.next();
			List<Value> values = b.getValues();
			// String location = values.get(0).toString().replaceAll("\"", "");
			String eventName = values.get(0).toString().replaceAll("\"", "");
			// String starts = values.get(2).toString().replaceAll("\"", "");
			// String ends = values.get(3).toString().replaceAll("\"", "");

			/*
			 * sResponse += eventName + " in " + location + " (" + starts +
			 * " - " + ends + ") </br>";
			 */

			sResponse += "<Strong>" + eventName + "</Strong>";
			CloseableIterator<Statement> checkEventNode = con.search(null,
					RDF.URI_PREFERRED_NAME_STRING, (Literal) values.get(0),
					null, null);
			if (checkEventNode.hasNext()) {
				BNode trip = (BNode) checkEventNode.next().getSubject();
				CloseableIterator<Statement> checkLoc = con.search(trip,
						RDF.URI_EVENT_HASLOCATION, null, null, null);
				if (checkLoc.hasNext())
					sResponse += " in "
							+ checkLoc.next().getObject().toString()
									.replaceAll("\"", "");
			}

			sResponse += " </br>";

			VariableBinding vbAttendees = con
					.executeSelect(new SPARQLQueryImpl(
							"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
									+ "PREFIX cyc:<http://example.com/cyc#>"
									+ "SELECT ?OWNER ?P WHERE" + "{ "
									+ " ?P rdf:type cyc:event."
									+ " ?P cyc:preferredNameString \""
									+ eventName + "\"."
									+ " ?P cyc:eventHasOwner ?OWNER.}"));
			CloseableIterator<Binding> iterAtendees = vbAttendees.iterator();
			// all events with the same name

			while (iterAtendees.hasNext()) {
				boolean bThisOneHaveErr = false;

				Binding bOwner = iterAtendees.next();
				BNode btrip = (BNode) bOwner.getValues().get(1);
				sResponse += " - "
						+ bOwner.getValues().get(0).toString()
								.replaceAll("\"", "") + " <img src=\"niceimage"
						+ iPersonNum + ".png\" width=\"14\" height=\"14\" /> ";

				// get the lodging info
				VariableBinding vbLodging = con
						.executeSelect(new SPARQLQueryImpl(
								"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
										+ "PREFIX cyc:<http://example.com/cyc#>"
										+ "SELECT ?LODGING ?LNAME WHERE"
										+ "{"
										+ " ?P rdf:type cyc:event."
										+ " ?P cyc:preferredNameString \""
										+ eventName
										+ "\"."
										+ " ?P cyc:eventHasOwner "
										+ bOwner.getValues().get(0).toString()
										+ "."
										+ " ?LODGING cyc:preferredNameString ?LNAME."
										+ " ?P cyc:ProvidingLodging ?LODGING.}"));

				CloseableIterator<Binding> iterLodging = vbLodging.iterator();
				if (iterLodging.hasNext()) {
					while (iterLodging.hasNext()) {
						sResponse += okResponse(iterLodging.next().getValues()
								.get(1).toString().replaceAll("\"", ""));
					}
				} else {
					bThisOneHaveErr = true;
					sResponse += errResponse(" Lodging missing");
				}

				/*
				 * CloseableIterator<Statement> checklodging = con.search(btrip,
				 * RDF.URI_PROVIDING_LODGING, null, null, null); if
				 * (checklodging.hasNext()) { Statement stt =
				 * checklodging.next();
				 * System.out.println("SSSSSSSSSSSSSSSSSSSSS: " +
				 * stt.getSubject() + "  " + stt.getPredicate() + "  " +
				 * stt.getObject()); sResponse +=
				 * okResponse(stt.getObject().stringValue()); } else {
				 * bThisOneHaveErr = true; sResponse +=
				 * errResponse(" Lodging missing"); }
				 */

				CloseableIterator<Statement> checkflight = con.search(btrip,
						RDF.URI_SUBEVENTS, null, null, null);
				if (checkflight.hasNext()) {
					sResponse += okResponse(", Flight status OK");
				} else {
					bThisOneHaveErr = true;
					sResponse += errResponse(", Flight information missing");
				}

				sResponse += "</br>";

				if (bThisOneHaveErr) {
					String sReplace = "niceimage" + iPersonNum++;
					sResponse = sResponse
							.replaceAll(sReplace,
									"http://www.clker.com/cliparts/a/x/q/L/G/9/delete-icon-th");
				} else {
					String sReplace = "niceimage" + iPersonNum++;
					sResponse = sResponse
							.replaceAll(sReplace,
									"http://www.clker.com/cliparts/U/b/3/E/T/z/ok-icon-th");
				}
			}

			sResponse += "</br>";
		}
		/*
		 * String sResponse = ""; CloseableIterator<Statement> it =
		 * st.getStatements(); while (it.hasNext()) { Statement statement =
		 * it.next(); if (statement.getPredicate().equals(RDFConstants.RDF_TYPE)
		 * && statement.getObject().equals(RDF.URI_EVENT)) { sResponse +=
		 * it.next().getObject().toString() + " in "; }
		 * 
		 * 
		 * if(statement.getPredicate().equals(new
		 * URIImpl("http://credentials#username")))
		 * username=st.getObject().stringValue(); else
		 * if(st.getPredicate().equals(new
		 * URIImpl("http://credentials#password")))
		 * password=st.getObject().stringValue();
		 * 
		 * // sResponse += statement.toString() + "\n"; }
		 */

		return sResponse;
	}

	private String errResponse(String sString) {
		return "<span class=\"err\">" + sString + "</span>";
	}

	private String okResponse(String sString) {
		return "<span class=\"ok\">" + sString + "</span>";
	}
}
