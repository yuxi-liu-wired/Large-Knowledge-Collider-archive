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
package eu.larkc.core.endpoint;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.larkc.core.management.MgmtTest;
import eu.larkc.shared.Resources;
import eu.larkc.shared.SampleQueries;
import eu.larkc.shared.SampleRdfWorkflows;

/**
 * This class tests the SPARQL endpoint.
 * 
 * @author Norbert Lanzanasto, Christoph Fuchs
 * 
 */
public class SparqlEndpointTest extends MgmtTest {

	private static Logger logger = LoggerFactory
			.getLogger(SparqlEndpointTest.class);

	/**
	 * This test only checks if the basic functionality of the SPARQL query
	 * handler with a SELECT query does not yield any wierd results (i.e. result
	 * is null or unexpected exceptions are thrown)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSparqlQueryHandlerWithSelect() throws Exception {
		// construct workflow
		ClientResource rdfResource = new ClientResource(
				Resources.MGMT_WORKFLOWS_URL);

		Form form = new Form();
		form.add("workflow",
				SampleRdfWorkflows.getWorkflowWithSparqlTestPlugin());// getWorkflowWithSparqlEndpoint());
		Representation rep = form.getWebRepresentation();
		rep.setMediaType(MediaType.APPLICATION_RDF_XML);
		Representation rdfResponse = rdfResource.post(rep);
		Reference url = rdfResponse.getLocationRef();

		// retrieve endpoint
		ClientResource endpointResource = new ClientResource(url
				+ "/endpoint?urn=urn:eu.larkc.endpoint.sparql.ep1");
		Representation endpointResponse = endpointResource.get();

		Reference sparqlEndpointUrl = endpointResponse.getLocationRef();
		logger.debug("SPARQLEndpoint URL: {}", sparqlEndpointUrl);

		// execute query
		ClientResource sparqlEndpointResource = new ClientResource(
				sparqlEndpointUrl);

		Form queryForm = new Form();
		queryForm.add("query", SampleQueries.WHO_KNOWS_TIM_BERNERS_LEE);
		Representation queryRep = queryForm.getWebRepresentation();

		try {
			sparqlEndpointResource.post(queryRep);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Caught exception while posting query.");
		}
		int iCode = sparqlEndpointResource.getResponse().getStatus().getCode();
		Assert.assertTrue(iCode == 200 || iCode == 201);
		// Assert.assertEquals(200, iCode);

		Response response = sparqlEndpointResource.getResponse();
		String rText = response.getEntityAsText();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(rText));

		Document doc = db.parse(is);
		NodeList element = doc.getElementsByTagName("literal");
		Element node = (Element) element.item(0);// this one is results
		String itemName = node.getTextContent();

		Assert.assertEquals(itemName, "Luka");

		// delete workflow
		ClientResource workflowResource = new ClientResource(url);
		workflowResource.delete();
	}

	/**
	 * This test only checks if the basic functionality of the SPARQL query
	 * handler with a CONSTRUCT query does not yield any wierd results (i.e.
	 * result is null or unexpected exceptions are thrown)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSparqlQueryHandlerWithConstruct() throws Exception {
		// construct workflow
		ClientResource rdfResource = new ClientResource(
				Resources.MGMT_WORKFLOWS_URL);

		Form form = new Form();
		form.add("workflow",
				SampleRdfWorkflows.getWorkflowWithSparqlTestPlugin());
		Representation rep = form.getWebRepresentation();
		rep.setMediaType(MediaType.APPLICATION_RDF_XML);
		Representation rdfResponse = rdfResource.post(rep);
		Reference url = rdfResponse.getLocationRef();

		// retrieve endpoint
		ClientResource endpointResource = new ClientResource(url
				+ "/endpoint?urn=urn:eu.larkc.endpoint.sparql.ep1");
		Representation endpointResponse = endpointResource.get();

		Reference sparqlEndpointUrl = endpointResponse.getLocationRef();
		logger.debug("SPARQLEndpoint URL: {}", sparqlEndpointUrl);

		// execute query
		ClientResource sparqlEndpointResource = new ClientResource(
				sparqlEndpointUrl);

		Form queryForm = new Form();
		queryForm.add("query", SampleQueries.WHO_KNOWS_FRANK);
		Representation queryRep = queryForm.getWebRepresentation();

		try {
			sparqlEndpointResource.post(queryRep);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Caught exception while posting query.");
		}

		int iCode = sparqlEndpointResource.getResponse().getStatus().getCode();
		Assert.assertTrue(iCode == 200 || iCode == 201);

		/*
		 * TODO Implement a test which actually retrieves results
		 */

		// delete workflow
		ClientResource workflowResource = new ClientResource(url);
		workflowResource.delete();
	}

	/**
	 * This test only checks if the basic functionality of the SPARQL query
	 * handler with an ASK query does not yield any wierd results (i.e. result
	 * is null or unexpected exceptions are thrown).
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSparqlQueryHandlerWithAsk() throws Exception {
		// construct workflow
		ClientResource rdfResource = new ClientResource(
				Resources.MGMT_WORKFLOWS_URL);

		Form form = new Form();
		form.add("workflow",
				SampleRdfWorkflows.getWorkflowWithSparqlTestPlugin());
		Representation rep = form.getWebRepresentation();
		rep.setMediaType(MediaType.APPLICATION_RDF_XML);
		Representation rdfResponse = rdfResource.post(rep);
		Reference url = rdfResponse.getLocationRef();

		// retrieve endpoint
		ClientResource endpointResource = new ClientResource(url
				+ "/endpoint?urn=urn:eu.larkc.endpoint.sparql.ep1");
		Representation endpointResponse = endpointResource.get();

		Reference sparqlEndpointUrl = endpointResponse.getLocationRef();
		logger.debug("SPARQLEndpoint URL: {}", sparqlEndpointUrl);

		// execute query
		ClientResource sparqlEndpointResource = new ClientResource(
				sparqlEndpointUrl);

		Form queryForm = new Form();
		queryForm.add("query", SampleQueries.ASK_ALL_TRIPLES);
		Representation queryRep = queryForm.getWebRepresentation();

		try {
			sparqlEndpointResource.post(queryRep);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Caught exception while posting query.");
		}

		int iCode = sparqlEndpointResource.getResponse().getStatus().getCode();
		Assert.assertTrue(iCode == 200 || iCode == 201);

		/*
		 * TODO Implement a test which actually retrieves results
		 */

		// delete workflow
		ClientResource workflowResource = new ClientResource(url);
		workflowResource.delete();
	}
}
