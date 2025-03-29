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
package eu.larkc.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.pluginManager.PluginThread;
import eu.larkc.core.pluginManager.local.LocalPluginManager.LocalPluginThread;
import eu.larkc.core.pluginManager.remote.GAT.GatPluginManager.GatPluginThread;
import eu.larkc.core.pluginManager.remote.Servlet.Tomcat.JeePluginManager.JeePluginThread;

/**
 * This class implements a queue listener.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class QueueListenerImpl implements QueueListener<SetOfStatements> {

	/** The logger. */
	protected final Logger logger = LoggerFactory
			.getLogger(QueueListener.class);
	private PluginThread pluginThread;

	/**
	 * Custom constructor.
	 * 
	 * @param thread
	 *            the plugin thread.
	 */
	public QueueListenerImpl(LocalPluginThread thread) {
		pluginThread = thread;
	}

	/**
	 * Custom constructor.
	 * 
	 * @param thread
	 *            the plugin thread.
	 */
	public QueueListenerImpl(JeePluginThread thread) {
		pluginThread = thread;
	}

	/**
	 * Custom constructor.
	 * 
	 * @param thread
	 *            the plugin thread.
	 */
	public QueueListenerImpl(GatPluginThread thread) {
		pluginThread = thread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.core.queue.QueueListener#elementAdded(eu.larkc.core.queue.Queue)
	 */
	@Override
	public synchronized void elementAdded(Queue<SetOfStatements> queue) {
		logger.debug("Got notifaction that elements were added to a queue");
		pluginThread.getQueueElement(queue);
		// queue.removeListeners();
	}
}
