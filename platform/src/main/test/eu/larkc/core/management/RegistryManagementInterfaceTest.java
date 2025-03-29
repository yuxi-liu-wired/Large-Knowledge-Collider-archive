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
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.model.URI;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import eu.larkc.core.Larkc;
import eu.larkc.shared.Resources;

/**
 * This class tests the functionality of the registry resource.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class RegistryManagementInterfaceTest extends MgmtTest {

	/**
	 * Tries to retrieve all deployed plug-ins.
	 * 
	 * @throws IOException
	 */
	@Test
	public void getAllPluginsTest() throws IOException {
		ClientResource rdfResource = new ClientResource(
				Resources.MGMT_PLUGINS_URL);
		Representation rdfResponse = rdfResource.get();

		String plugins = rdfResponse.getText();
		Collection<URI> allPlugins = Larkc.getPluginRegistry().getAllPlugins();

		for (URI plugin : allPlugins) {
			Assert.assertTrue(plugins.contains(plugin.toString()));
		}
	}

	/**
	 * Tries to retrieve all deployed endpoints.
	 * 
	 * @throws IOException
	 */
	@Test
	public void getAllEndpointsTest() throws IOException {
		ClientResource rdfResource = new ClientResource(
				Resources.MGMT_ENDPOINTS_URL);
		Representation rdfResponse = rdfResource.get();

		String endpoints = rdfResponse.getText();
		Collection<URI> allEndpoints = Larkc.getEndpointRegistry()
				.getAllEndpoints();

		for (URI endpoint : allEndpoints) {
			Assert.assertTrue(endpoints.contains(endpoint.toString()));
		}
	}

}
