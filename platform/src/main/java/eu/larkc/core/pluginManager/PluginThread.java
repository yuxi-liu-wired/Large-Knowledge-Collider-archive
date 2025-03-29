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
package eu.larkc.core.pluginManager;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.queue.Queue;

/**
 * Interface for the plugin threads.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public interface PluginThread {

	/**
	 * Gets a SetOfStatements out of a queue.
	 * 
	 * @param queue
	 *            the queue.
	 */
	public void getQueueElement(Queue<SetOfStatements> queue);

	/**
	 * Stops the plugin thread if it is waiting.
	 */
	public void stopWaiting();
}
