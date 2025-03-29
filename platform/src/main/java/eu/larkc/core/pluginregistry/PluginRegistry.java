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
package eu.larkc.core.pluginregistry;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.openrdf.model.URI;

import eu.larkc.plugin.Plugin;

/**
 * Interface for the plug-in registry.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public interface PluginRegistry {

	/**
	 * Instantiates the plug-in.
	 * 
	 * @param pluginUri
	 * @return plug-in instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public Plugin getNewPluginInstance(URI pluginUri)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			SecurityException, NoSuchMethodException;

	/**
	 * Load plug-ins.
	 */
	public void loadPlugins();

	/**
	 * Returns the URIs of all the plug-ins available to the platform
	 * 
	 * @return Collection of plug-in URIs
	 */
	public Collection<URI> getAllPlugins();

	/**
	 * Returns true if the URI specifies a plug-in.
	 * 
	 * @param object
	 *            the URI of the object
	 * @return true if the URI specifies a plug-in.
	 */
	public boolean isLarKCPlugin(URI object);

}
