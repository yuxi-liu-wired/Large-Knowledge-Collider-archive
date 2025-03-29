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
package eu.larkc.core.pluginManager.remote.Servlet.Tomcat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.parallelization.MultiThreading;
import eu.larkc.core.parallelization.MultiThreadingException;
import eu.larkc.core.pluginManager.ControlMessage;
import eu.larkc.core.pluginManager.Message;
import eu.larkc.core.pluginManager.PluginManager;
import eu.larkc.core.pluginManager.PluginThread;
import eu.larkc.core.pluginManager.remote.Servlet.Tomcat.servlet.HttpRequest;
import eu.larkc.core.queue.Queue;
import eu.larkc.core.queue.QueueListener;
import eu.larkc.core.queue.QueueListenerImpl;
import eu.larkc.plugin.Plugin;

/**
 * LocalPluginManager is a particular implementation of PluginManger that runs
 * within a thread on a local machine. A pipeline made using
 * LocalPluginManager's has a number of strongly type queues between each
 * PluginManager that represent the input and output streams to and from the
 * plugin in question.
 * 
 * 
 * @author Mick Kerrigan, Barry Bishop
 */
public class JeePluginManager implements PluginManager {

	/**
	 * The plugin controlled by this manager.
	 */
	private final Plugin mPlugin;

	/**
	 * The resource description for JEE.
	 */
	private final JeeResourceDescription jeeResource;

	/**
	 * The URI.
	 */
	protected URI mInstanceUri;

	/**
	 * The queue for storing and accessing control methods sent by other
	 * PluginManagers.
	 * 
	 * @see PluginManager.ControlMessage
	 */
	private Queue<ControlMessage> mControlQueue;

	/** The PluginManagers managing the previous plugins in the pipeline. */
	private List<PluginManager> mPreviousPlugins;

	/** The thread that the plugin management goes on within. */
	private JeePluginThread pluginThread;

	/**
	 * The queues from which input messages will come from the previous plugin
	 * in the pipeline.
	 */
	private Map<String, List<Queue<SetOfStatements>>> inputQueues;

	/**
	 * The queues onto which output messages should be put to send them to the
	 * next plugin in the pipeline.
	 */
	private Map<String, List<Queue<SetOfStatements>>> outputQueues;

	/** The logger. */
	protected final Logger logger;

	/**
	 * Defines the input behavior of the plugin (if it has multiple inputs).
	 */
	private int inputBehavior;

	/**
	 * Constructor that takes only the plugin as input (input and output queues
	 * have to be set later).
	 * 
	 * @param plugin
	 *            The plugin for which this manager is responsible
	 * @param resource
	 */
	public JeePluginManager(Plugin plugin, JeeResourceDescription resource) {
		mPlugin = plugin;
		logger = LoggerFactory.getLogger(plugin.getClass());
		inputQueues = new HashMap<String, List<Queue<SetOfStatements>>>();
		outputQueues = new HashMap<String, List<Queue<SetOfStatements>>>();
		mControlQueue = new Queue<ControlMessage>();
		jeeResource = resource;
		mPreviousPlugins = new ArrayList<PluginManager>();
		inputBehavior = plugin.getInputBehavior();

		setThread(new JeePluginThread());

		logger.debug("Initialized JEE plugin manager");
	}

	/**
	 * Constructor thats takes the input and output queues as input.
	 * 
	 * @param theInputQueues
	 *            The queues from which input messages will come from the
	 *            previous plugin in the pipeline
	 * @param theOutputQueues
	 *            The queues onto which output messages should be put to send
	 *            them to the next plugin in the pipeline
	 * @param plugin
	 *            The plugin for which this manager is responsible
	 * @param resource
	 */
	public JeePluginManager(Plugin plugin,
			Map<String, List<Queue<SetOfStatements>>> theInputQueues,
			Map<String, List<Queue<SetOfStatements>>> theOutputQueues,
			JeeResourceDescription resource) {
		mPlugin = plugin;
		logger = LoggerFactory.getLogger(plugin.getClass());
		inputQueues = theInputQueues;
		outputQueues = theOutputQueues;
		mControlQueue = new Queue<ControlMessage>();
		jeeResource = resource;
		mPreviousPlugins = new ArrayList<PluginManager>();

		setThread(new JeePluginThread());

		logger.debug("Initialized JEE plugin manager");
	}

	/**
	 * This method returns the input queue of the manager.
	 * 
	 * @return the input queue
	 */
	public List<Queue<SetOfStatements>> getInputQueues(String pathId) {
		return inputQueues.get(pathId);
	}

	/**
	 * This method sets the input queue of the manager.
	 * 
	 * @param theInputQueues
	 *            the input queues
	 */
	public void setInputQueues(List<Queue<SetOfStatements>> theInputQueues,
			String pathId) {
		inputQueues.put(pathId, theInputQueues);
	}

	/**
	 * This method returns the output queues of the manager.
	 * 
	 * @return the output queues
	 */
	public List<Queue<SetOfStatements>> getOutputQueues(String pathId) {
		return outputQueues.get(pathId);
	}

	/**
	 * This method sets the output queues of the manager.
	 * 
	 * @param theOutputQueues
	 *            the output queues
	 */
	public void setOutputQueues(List<Queue<SetOfStatements>> theOutputQueues,
			String pathId) {
		outputQueues.put(pathId, theOutputQueues);
	}

	/**
	 * Accept.
	 * 
	 * @param message
	 *            the message
	 */
	public void accept(ControlMessage message) {
		mControlQueue.put(message);
	}

	/**
	 * This method enables the next control message that was sent to this
	 * PluginManager to be retrieved.
	 * 
	 * @return the next control message
	 */
	public ControlMessage getNextControlMessage() {
		return mControlQueue.take();
	}

	/**
	 * Adds the previous plugin manager.
	 * 
	 * @param provider
	 *            the provider
	 * 
	 * @see eu.larkc.core.pluginManager.PluginManager#addPrevious(eu.larkc.core.pluginManager.PluginManager)
	 */
	public void addPrevious(PluginManager provider) {
		mPreviousPlugins.add(provider);
	}

	/**
	 * This method should be called in order to tell the previous plugin in the
	 * pipeline to send the next piece of input on the input queue.
	 */
	public void alertPrevious(String pathId) {
		for (PluginManager manager : mPreviousPlugins) {
			manager.accept(new ControlMessage(Message.NEXT, pathId));
		}
	}

	/**
	 * Start.
	 * 
	 * @see eu.larkc.core.pluginManager.PluginManager#start()
	 */
	public void start() {
		if (pluginThread != null) {
			pluginThread.start();
		}
	}

	/**
	 * Stop.
	 * 
	 * @see eu.larkc.core.pluginManager.PluginManager#stopWaiting()
	 */
	public void stopWaiting() {
		if (pluginThread != null
				&& pluginThread.getState().equals(Thread.State.WAITING)) {
			pluginThread.interrupt();
		}
	}

	/**
	 * This method is used to specify the thread in which the plugin management
	 * occurs.
	 * 
	 * @param theThread
	 *            The Thread in which the plugin management occurs
	 */
	protected void setThread(JeePluginThread theThread) {
		pluginThread = theThread;
	}

	/**
	 * The thread containing the plugin
	 * 
	 * @author norlan
	 * 
	 */
	public class JeePluginThread extends Thread implements PluginThread {

		private int neededInputs;
		private ArrayList<Statement> statements;

		/**
		 * Custom constructor.
		 */
		public JeePluginThread() {
			super("JeePluginManager");
			neededInputs = inputBehavior;
			statements = new ArrayList<Statement>();
		}

		public void run() {
			// the plugin has to be initialized before this point (by the
			// executor)
			// mPlugin.initialise();
			String pathId;

			for (;;) {
				ControlMessage controlMessage = getNextControlMessage();

				if (controlMessage.getMessage().equals(Message.NEXT)) {
					pathId = controlMessage.getPathId();
					alertPrevious(pathId);

					SetOfStatements input = getNextInput(pathId);

					if (input == null) {
						putNextOutput(null, pathId);
						break;
					}

					SetOfStatements output = null;

					CloseableIterator<Statement> statements = mPlugin
							.getPluginParameters().getStatements();

					Statement stmt = null;
					Statement tmpStmt = null;

					while (statements.hasNext()) {
						tmpStmt = statements.next();
						if (tmpStmt
								.getPredicate()
								.stringValue()
								.equals(WorkflowDescriptionPredicates.IS_INPUT_SPLITTABLE_URI
										.stringValue())) {
							stmt = tmpStmt;
							break;
						}
					}

					if (stmt != null) {
						HashMap<List<Plugin>, SetOfStatements> map = new HashMap<List<Plugin>, SetOfStatements>();
						List<Plugin> list = new ArrayList<Plugin>();
						list.add(mPlugin);
						map.put(list, input);
						try {
							MultiThreading.INSTANCE.invokeThreadPool(map);
							output = MultiThreading.INSTANCE
									.getSynchronizedResults();
						} catch (MultiThreadingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// The following operations are equivalent to the
						// original call: output = mPlugin.invoke(input);

						// InstantiatePlugin, should be moved to the Constructor
						instantiatePlugin();

						// CallPluginInstance
						output = callPluginInstance(input,
								OperationDescriptor.OP_INVOKE);

						// Shutdown plug-in, should be moved to the Destructor
						destroyPlugin();
					}

					putNextOutput(output, pathId);
				} else if (controlMessage.getMessage().equals(Message.STOP)) {
					break;
				}
			}
			logger.debug("Plugin is shutting down ...");
			mPlugin.shutdown();
		}

		/**
		 * This method should be called to get the next input from the input
		 * queue.
		 * 
		 * @return the next element on the input queue
		 */
		private synchronized SetOfStatements getNextInput(String pathId) {
			statements = new ArrayList<Statement>();

			if (inputQueues.get(pathId) != null) {
				int numberOfInputs = inputQueues.get(pathId).size();

				if (inputBehavior < 0 || inputBehavior > numberOfInputs) {
					neededInputs = numberOfInputs;
					logger.debug("Use standard input behavior for {}: {}",
							mPlugin.toString(), neededInputs);
				} else {
					neededInputs = inputBehavior;
					logger.debug("Found input behavior for {}: {}",
							mPlugin.toString(), neededInputs);
				}

				for (Queue<SetOfStatements> queue : inputQueues.get(pathId)) {
					if (!queue.isEmpty()) {
						getQueueElement(queue);
					} else {
						QueueListener<SetOfStatements> listener = new QueueListenerImpl(
								this);
						queue.addListener(listener);
					}
				}
			} else {
				logger.debug("No input queues defined for {} ({})",
						mPlugin.toString(), pathId);
			}

			try {
				while (neededInputs > 0) {
					wait();
					logger.debug("Got notification ...");
				}
			} catch (InterruptedException e) {
				logger.debug("Waiting was interrupted ...");
				// e.printStackTrace();
			}

			return new SetOfStatementsImpl(statements);
		}

		/**
		 * This method should be called to put an output on the output queue.
		 * 
		 * @param theF
		 *            The element to put on the output queue
		 */
		private void putNextOutput(SetOfStatements theF, String pathId) {
			logger.debug("Wrote data to output queues ...");
			for (Queue<SetOfStatements> queue : outputQueues.get(pathId)) {
				queue.put(theF);
			}
		}

		/**
		 * Gets a SetOfStatements out of a queue.
		 * 
		 * @param queue
		 *            the queue.
		 */
		public synchronized void getQueueElement(Queue<SetOfStatements> queue) {
			CloseableIterator<Statement> it;
			SetOfStatements queueElement = queue.take();
			if (queueElement == null) {
				logger.warn("No data in input queue. Found a NULL element in the input queue, probably some plug-in did not compute any results.");
			} else {
				// logger.debug(
				// "Used input queue on {} for plugin {}",
				// pathId, mPlugin.getIdentifier());
				it = queueElement.getStatements();
				while (it.hasNext()) {
					statements.add(it.next());
				}
			}
			neededInputs--;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see eu.larkc.core.pluginManager.PluginThread#stopWaiting()
		 */
		@Override
		public void stopWaiting() {
			neededInputs = 0;
			this.notify();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.core.pluginManager.PluginManager#addInputQueue(eu.larkc.core
	 * .pluginManager.local.queue.Queue)
	 */
	@Override
	public void addInputQueue(Queue<SetOfStatements> inputQueue, String pathId) {
		if (inputQueues.containsKey(pathId)) {
			inputQueues.get(pathId).add(inputQueue);
		} else {
			ArrayList<Queue<SetOfStatements>> arrayList = new ArrayList<Queue<SetOfStatements>>();
			arrayList.add(inputQueue);
			inputQueues.put(pathId, arrayList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.core.pluginManager.PluginManager#addOuputQueue(eu.larkc.core
	 * .pluginManager.local.queue.Queue)
	 */
	@Override
	public void addOutputQueue(Queue<SetOfStatements> outputQueue, String pathId) {
		if (outputQueues.containsKey(pathId)) {
			outputQueues.get(pathId).add(outputQueue);
		} else {
			ArrayList<Queue<SetOfStatements>> arrayList = new ArrayList<Queue<SetOfStatements>>();
			arrayList.add(outputQueue);
			outputQueues.put(pathId, arrayList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "PluginManager for" + mPlugin.getIdentifier();
	}

	private void instantiatePlugin() {
		try {
			HttpRequest request = new HttpRequest(jeeResource.URI.toString());
			request.setGetMethod();
			request.setContentType("text/plain");

			request.open();
			mInstanceUri = new URIImpl(request.readAsString());

		} catch (IOException e) {
			throw new RuntimeException("Instantiating plug-in failed.", e);
		}
	}

	protected SetOfStatements callPluginInstance(SetOfStatements parameters,
			String operation) {
		try {
			HttpRequest request = new HttpRequest(mInstanceUri.toString());
			request.setPutMethod();

			request.addParameter(OperationDescriptor.OPERATION, operation);

			// Always write the parameters object, even if it null.
			// If nothing is written then just creating the ObjectInputStream on
			// the server-side
			// will cause errors.
			request.setContentType("application/octet-stream");

			ObjectOutputStream out = new ObjectOutputStream(
					request.openForWriting());

			out.writeObject(parameters);
			out.flush();

			ObjectInputStream in = new ObjectInputStream(request.read());
			Object readObject = in.readObject();

			SetOfStatements output = (SetOfStatements) readObject;

			return output;
		} catch (Exception e) {
			throw new RuntimeException("Call to plug-in instance failed.", e);
		}
	}

	private void destroyPlugin() {
		try {
			HttpRequest request = new HttpRequest(mInstanceUri.toString());
			request.setDeleteMethod();
			request.open();
			request.read();
		} catch (IOException e) {
			throw new RuntimeException("Shutting down plug-in failed.", e);
		}
	}

}