/*
   This file is part of the LarKC platform 
   http://www.larkc.eu/

   Copyright 2010 LarKC project consortium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.larkc.core.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.Larkc;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.WorkflowObject;
import eu.larkc.core.data.workflow.IllegalWorkflowGraphException;
import eu.larkc.core.executor.Executor;
import eu.larkc.core.executor.ExecutorException;
import eu.larkc.shared.RDFParsingUtils;

/**
 * Abstract workflow handler base class. Has to be extended by actual workflow
 * handler implementation, e.g. a workflow handler that handles RDF/XML, or a
 * workflow handler that handles workflows in N3 notation.
 * 
 * @author Christoph Fuchs
 * 
 */
public class WorkflowHandler extends ServerResource implements
		WorkflowHandlerResource {

	private static Logger logger = LoggerFactory
			.getLogger(WorkflowHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.core.management.RDFHandlerResource#downloadWorkflowDescription()
	 */
	@Override
	public Representation downloadWorkflowDescription(Representation entity)
			throws Exception {

		Form form = new Form(entity);

		MediaType mediaType = entity.getMediaType();
		logger.debug("Media type: {}", mediaType);

		String workflow = form.getFirstValue("workflow");

		if (workflow == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			throw new IllegalWorkflowGraphException("Empty workflow!");
		}

		try {
			Collection<Statement> col = null;

			if (mediaType.equals(MediaType.APPLICATION_RDF_XML)) {
				logger.debug("Workflow is in RDF/XML format.");
				col = extractXMLStatementsFromWorkflow(workflow);
			} else if (mediaType.equals(MediaType.TEXT_RDF_N3)) {
				logger.debug("Workflow is in N3 format.");
				col = extractN3StatementsFromWorkflow(workflow);
			} else {

				try {
					logger.debug("Try to parse RDF/XML ...");
					col = extractXMLStatementsFromWorkflow(workflow);
				} catch (Exception e1) {
					logger.debug("Exception: {}", e1.getMessage());
					try {
						logger.debug("Try to parse N3 ...");
						col = extractN3StatementsFromWorkflow(workflow);
					} catch (Exception e2) {
						logger.debug("Exception: {}", e2.getMessage());
						logger.debug(
								"No matching media type found (media type was {})",
								mediaType);
						throw new RuntimeException(
								"No matching media type found!");
					}
				}

			}

			Iterator<Statement> iter = col.iterator();
			List<Statement> stmnts = new ArrayList<Statement>();

			while (iter.hasNext()) {
				stmnts.add(iter.next());
			}

			SetOfStatements workflowDescription = new SetOfStatementsImpl(
					stmnts);
			Executor executor = new Executor(workflowDescription);

			// set response
			setStatus(Status.SUCCESS_CREATED);

			UUID workflowId = executor.getId();

			Collection<WorkflowObject> workflows = Larkc.getWorkflows();
			StringBuilder sb = new StringBuilder();

			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"");
			sb.append("\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
			sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			sb.append("<head>");
			sb.append("<style type=\"text/css\" title=\"currentStyle\" media=\"screen\">");
			sb.append("@import \"style.css\";</style></head>");
			sb.append("<body>");
			sb.append("<div id=\"main\"><div id=\"header\"></div>");
			sb.append("<h1>LarKC management interface</h1>");

			sb.append("<p>Workflow with ID <b>");
			sb.append(workflowId);
			sb.append("</b> successfully deployed.</p>");

			sb.append("<h2>Currently deployed workflows</h2>");
			sb.append("<table border=\"1\" cellpadding=\"10\"><tr><th>Workflow ID</th><th>Endpoints</th></tr>");

			for (WorkflowObject wo : workflows) {
				sb.append("<tr><td>");
				sb.append(wo.getWorkflowId());
				sb.append("</td><td>");
				for (Entry<String, URI> endpoint : wo.getEndpoints().entrySet()) {
					sb.append(endpoint.getKey());
					sb.append(" (");
					sb.append(endpoint.getValue().stringValue());
					sb.append(")");
					sb.append("<br>");
				}

				sb.append("</td>");
				sb.append("</tr>");
			}

			sb.append("</table>");

			sb.append("<h2>Submit a query to an endpoint</h2>");
			sb.append("<p>To submit the query in the textarea below to an endpoint you have to specify the endpoint URL (all available endpoints are shown in the table above in the last column).</p>");

			sb.append("<form name=\"endpoint-address\">");
			sb.append("<p><b>Endpoint address: </b>");
			sb.append("<input name=\"endpointAddress\" type=\"text\" size=\"70\"/></p>");
			sb.append("</form>");

			sb.append("<script>");
			sb.append("function submitQueryFn() {");
			sb.append("var endpointAddress1 = document.getElementsByName(\"endpointAddress\")[0]; ");
			sb.append("var sform = document.getElementsByName( \"sparql-form\")[0];");
			sb.append("sform.action = endpointAddress1.value;");
			sb.append("sform.submit();");
			sb.append("}");
			sb.append("</script>");

			sb.append("<script>");
			sb.append("function submitGetFn() { ");
			sb.append("var endpointAddress2 = document.getElementsByName( \"endpointAddress\")[0]; ");
			sb.append("var getform = document.getElementsByName( \"get-form\")[0];");
			sb.append("getform.action = endpointAddress2.value;");
			sb.append("getform.submit();");
			sb.append("}");
			sb.append("</script>");

			sb.append("<form name=\"sparql-form\" onsubmit=\"submitQueryFn();\" method=\"post\">");
			sb.append("<textarea name=\"query\" rows=\"10\" cols=\"80\">");
			sb.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
			sb.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
			sb.append("SELECT DISTINCT ?name where {\n");
			sb.append("?person rdf:type foaf:Person.\n");
			sb.append("?person foaf:name \"Frank van Harmelen\".\n");
			sb.append("?person2 rdf:type foaf:Person.\n");
			sb.append("?person2 foaf:knows ?person.\n");
			sb.append("?person2 foaf:name ?name.\n");
			sb.append("}\n");
			sb.append("</textarea><br />");
			sb.append("<input type=\"submit\" value=\"Submit\" />");
			sb.append("</form>");

			sb.append("<form name=\"get-form\" onsubmit=\"submitGetFn();\" method=\"get\">");
			sb.append("<input type=\"submit\" value=\"Get results\" />");
			sb.append("</form>");

			sb.append("</div></body></html>");

			Representation rep = new StringRepresentation(sb.toString(),
					MediaType.TEXT_HTML);
			// Indicates where the new resource is located
			String identifier = getRequest().getResourceRef().getIdentifier();
			rep.setLocationRef(identifier + "/" + workflowId);
			return rep;
		} catch (RuntimeException e) {
			String errorDescription = "No matching media type found";

			Representation rep = generateErrorMessage(
					Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, workflow, e,
					errorDescription);

			return rep;
		} catch (RDFParseException e) {
			String errorDescription = "Error parsing RDF file";

			Representation rep = generateErrorMessage(Status.SUCCESS_ACCEPTED,
					workflow, e, errorDescription);

			return rep;
		} catch (ExecutorException e) {
			String errorDescription = "Error initializing workflow";

			Representation rep = generateErrorMessage(
					Status.SERVER_ERROR_INTERNAL, workflow, e, errorDescription);

			return rep;
		}
	}

	/**
	 * This method generates an error message packed in a Representation object.
	 * 
	 * @param status
	 *            The return status
	 * @param workflow
	 *            The workflow
	 * @param e
	 *            The exception
	 * @param errorDescription
	 *            The description of the error
	 * @return
	 */
	private Representation generateErrorMessage(Status status, String workflow,
			Exception e, String errorDescription) {
		setStatus(status);
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>").append(errorDescription).append("</h1>");
		sb.append("<pre>");
		sb.append(workflow);
		sb.append("</pre>");
		sb.append("<p>Error message: </p>");
		sb.append("<pre>");
		sb.append(e.getMessage());
		sb.append("</pre>");
		Representation rep = new StringRepresentation(sb.toString(),
				MediaType.TEXT_HTML);
		return rep;
	}

	/**
	 * Extract statements from a workflow description by means of parsing the
	 * given workflow using a N3 RDF parser.
	 * 
	 * @param workflow
	 *            - the workflow description as a string
	 * @return a Collection of {@link Statement}s
	 * @throws RDFParseException
	 *             on RDF parsing error (e.g. malformed RDF)
	 * @throws RDFHandlerException
	 *             on RDF handling error
	 * @throws IOException
	 *             on I/O error
	 */
	private Collection<Statement> extractN3StatementsFromWorkflow(
			String workflow) throws RDFParseException, RDFHandlerException,
			IOException {
		return RDFParsingUtils.parseN3(workflow);
	}

	/**
	 * Extract statements from a workflow description by means of parsing the
	 * given workflow using an RDF/XML RDF parser.
	 * 
	 * @param workflow
	 *            - the workflow description as a string
	 * @return a Collection of {@link Statement}s
	 * @throws RDFParseException
	 *             on RDF parsing error (e.g. malformed RDF)
	 * @throws RDFHandlerException
	 *             on RDF handling error
	 * @throws IOException
	 *             on I/O error
	 */
	private Collection<Statement> extractXMLStatementsFromWorkflow(
			String workflow) throws RDFParseException, RDFHandlerException,
			IOException {
		return RDFParsingUtils.parseXML(workflow);
	}
}
