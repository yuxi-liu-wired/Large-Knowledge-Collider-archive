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
package eu.larkc.core.management.workflows;

import java.util.Collection;
import java.util.Map.Entry;

import org.openrdf.model.URI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import eu.larkc.core.Larkc;
import eu.larkc.core.data.WorkflowObject;

/**
 * The management interface application for N3 workflow descriptions.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class WorkflowsManagementInterface extends ServerResource {

	// private static Logger logger = LoggerFactory
	// .getLogger(WorkflowsManagementInterface.class);

	/**
	 * Returns a page with all deployed workflow ids.
	 * 
	 * @return webpage
	 */
	@Get
	public Representation getDeployedWorkflows() {

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
		sb.append("<h2>Currently deployed workflows</h2>");
		if (!workflows.isEmpty()) {
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
		} else {
			sb.append("<p>Currently no workflows are deployed!</p>");
		}
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

		// set response
		setStatus(Status.SUCCESS_OK);

		Representation rep = new StringRepresentation(sb.toString(),
				MediaType.TEXT_HTML);
		return rep;
	}
}
