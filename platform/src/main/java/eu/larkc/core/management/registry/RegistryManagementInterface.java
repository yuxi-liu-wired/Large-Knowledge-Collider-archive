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
package eu.larkc.core.management.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.openrdf.model.URI;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import eu.larkc.core.Larkc;
import eu.larkc.core.endpointregistry.EndpointRegistry;
import eu.larkc.core.pluginregistry.PluginRegistry;

/**
 * <p>
 * This bridge lists all loaded plug-ins or all loaded endpoints. To retrieve a
 * list of loaded plug-ins or endpoints form a HTTP GET request to the
 * management interface URL appended by "/registry?getAllPlugins" or
 * "/registry?getAllEndpoints". E.g:
 * </p>
 * 
 * <code>GET http://localhost:8182/registry?getAllPlugins</code>
 * <code>GET http://localhost:8182/registry?getAllEndpoints</code>
 * 
 * <p>
 * </p>
 * 
 * @author Luka Bradesko, Norbert Lanzanasto
 * 
 */
public class RegistryManagementInterface extends ServerResource {

	@Get
	public String toString() {

		String sFunction = getReference().getRemainingPart();

		if (sFunction == null || sFunction.length() < 1) {
			super.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"Missing function name in the request URL.");
			return null;
		}

		sFunction = sFunction.substring(1);
		int iParamIndex = sFunction.indexOf('?');
		if (iParamIndex != -1) {
			sFunction = sFunction.substring(0, iParamIndex);
			// For now this is ignored since we don't use any parameters. In
			// future call to parameter parsing function.
		}

		String sReturn = "";
		try {
			PluginRegistry pluginRegistry = Larkc.getPluginRegistry();
			Method method = pluginRegistry.getClass().getMethod(sFunction);

			@SuppressWarnings("unchecked")
			Collection<URI> plugins = (Collection<URI>) method
					.invoke(pluginRegistry);

			for (URI uri : plugins) {
				sReturn += uri.stringValue() + "\n";
			}
		} catch (NoSuchMethodException e) {
			try {
				EndpointRegistry endpointRegistry = Larkc.getEndpointRegistry();
				Method method = endpointRegistry.getClass()
						.getMethod(sFunction);

				@SuppressWarnings("unchecked")
				Collection<URI> endpoints = (Collection<URI>) method
						.invoke(endpointRegistry);

				for (URI uri : endpoints) {
					sReturn += uri.stringValue() + "\n";
				}
			} catch (NoSuchMethodException f) {
				super.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, f,
						"Wrong function.");
				return null;
			} catch (SecurityException f) {
				super.setStatus(Status.SERVER_ERROR_INTERNAL, f);
				return null;
			} catch (IllegalArgumentException f) {
				super.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, f,
						"Wrong arguments for this function.");
				return null;
			} catch (IllegalAccessException f) {
				super.setStatus(Status.SERVER_ERROR_INTERNAL, f);
				return null;
			} catch (InvocationTargetException f) {
				super.setStatus(Status.SERVER_ERROR_INTERNAL, f);
				return null;
			}
		} catch (SecurityException e) {
			super.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return null;
		} catch (IllegalArgumentException e) {
			super.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e,
					"Wrong arguments for this function.");
			return null;
		} catch (IllegalAccessException e) {
			super.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return null;
		} catch (InvocationTargetException e) {
			super.setStatus(Status.SERVER_ERROR_INTERNAL, e);
			return null;
		}

		return sReturn;
	}
}