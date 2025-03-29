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
package eu.larkc.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyc.cycjava.cycl.operation_communication;
import com.cyc.tool.subl.jrtl.nativeCode.subLisp.CommonSymbols;
import com.cyc.tool.subl.jrtl.nativeCode.subLisp.SubLMain;
import com.cyc.tool.subl.util.SafeRunnable;
import com.cyc.tool.subl.util.SubLFiles;

import eu.larkc.core.data.DataLayerService;
import eu.larkc.core.data.WorkflowObject;
import eu.larkc.core.data.workflow.IllegalWorkflowGraphException;
import eu.larkc.core.endpoint.EndpointShutdownException;
import eu.larkc.core.endpointregistry.EndpointRegistry;
import eu.larkc.core.executor.Executor;
import eu.larkc.core.management.ManagementInterfaceMain;
import eu.larkc.core.pluginregistry.PluginRegistry;
import eu.larkc.core.pluginregistry.cyc.CycPluginRegistry;
import eu.larkc.core.pluginregistry.simple.SimplePluginRegistry;
import eu.larkc.shared.Resources;

/**
 * Main entry point to the LarKC platform
 * 
 * @author Blaz Fortuna, Luka Bradesko, Christoph Fuchs, Matthias Assel, Norbert
 *         Lanzanasto
 * 
 */
public final class Larkc {

	private static Logger logger = LoggerFactory.getLogger(Larkc.class);

	private static String PLUGIN_PATH_PARAMETER = null;
	private static String ENDPOINT_PATH_PARAMETER = null;
	private static boolean USE_CYC = false;

	private static PluginRegistry pluginRegistry;

	/**
	 * ENUM to keep track of the platform
	 */
	private static PlatformStatus platformStatus = PlatformStatus.NOT_INITIALIZED;;
	/**
	 * ENUM to keep track of the KB status
	 */
	private static LarkcKBStatus kbStatus = LarkcKBStatus.NOT_INITIALIZED;

	/**
	 * This map represents the connection between a workflow id (which is
	 * created when the executor is initialized) and the executor. This map is
	 * necessary to delete a workflow via the management interface (where only
	 * the workflow id is known). TODO persistence (gigi)
	 */
	private static Map<UUID, Executor> workflowIdExecutorMap = new HashMap<UUID, Executor>();

	private static EndpointRegistry endpointRegistry;

	/**
	 * Main method of the LarKC platform. If no arguments are provided the
	 * following arguments will be used: <blockquote>-i path_to/larkc-init.lisp
	 * -b</blockquote> -i is the parameter for specifying the larkc-init.lisp
	 * file, where are commands what to run after initial (skeleton) start -b is
	 * a switch, which prevents the console input, which is disabled in LarKC
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		File tmpFile = null;
		try {
			logger.debug("Starting LarKC");

			if (platformStatus == PlatformStatus.RUNNING) {
				logger.warn("Trying to start the platform but platform is already running.");
				return;
			}

			platformStatus = PlatformStatus.INITIALIZING;

			if (args != null && args.length != 0) {
				for (String param : args) {
					if (param.startsWith("-plugindir=")) {
						PLUGIN_PATH_PARAMETER = param.substring(11);
						logger.debug("Plugin directory set to '{}'",
								PLUGIN_PATH_PARAMETER);
					} else if (param.startsWith("-endpointdir=")) {
						ENDPOINT_PATH_PARAMETER = param.substring(13);
						logger.debug("Endpoint directory set to '{}'",
								ENDPOINT_PATH_PARAMETER);
					} else if (param.equals("-cyc")) {
						USE_CYC = true;
					}
				}
			}
			// if (args == null || args.length == 0) {
			// Try loading LarKC init file from classloader
			InputStream inputStream = Larkc.class.getClassLoader()
					.getResourceAsStream(Resources.LARKC_INIT);

			if (inputStream == null) {
				throw new RuntimeException(
						"Unable to load LarKC init file. Make sure "
								+ Resources.LARKC_INIT
								+ " exists and is readable.");
			}

			// Since a resource contained within a JAR file is not itself a
			// file, and cannot be read using a FileInputStream, we need
			// to extract the data using getResourceAsStream() and copy it
			// into a temporary file.
			tmpFile = copyToTempFile(inputStream);

			if (USE_CYC) {
				// SubLMain requires a FileInputStream
				String larkcInitLocation = tmpFile.toString();
				SubLMain.main(new String[] { "-i", larkcInitLocation, "-b" });
				// } else { SubLMain.main(args); }

				while (Larkc.getPlatformStatus() == PlatformStatus.INITIALIZING) {
					logger.debug("Initializing LarKC");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// Sleep got interrupted, we don't need to care about
						// that
					}
				}

				if (Larkc.getPlatformStatus() == PlatformStatus.RUNNING) {
					logger.info("Done initializing LarKC.");
				} else {
					logger.error("Unable to initialize the LarKC platform.");
				}
			} else {
				initialize(USE_CYC);
				logger.info("Done initializing LarKC.");
			}
		} catch (IOException e) {
			// If an I/O error occurred while copying larkc-init.lisp to the
			// temporary file
			e.printStackTrace();
		} finally {
			// Clean up the temporary file if it has been created
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}

	/**
	 * Copies the contents of an input stream to a temporary file.
	 * 
	 * @param inputStream
	 *            the inputStream which should be copied to a temporary file
	 * @throws IOException
	 *             on I/O error
	 */
	private static File copyToTempFile(InputStream inputStream)
			throws IOException {
		File tmpFile = File.createTempFile("larkc", null);
		FileWriter fw = new FileWriter(tmpFile);

		int c;
		while ((c = inputStream.read()) != -1) {
			fw.write(c);
		}
		fw.flush();
		fw.close();

		return tmpFile;
	}

	/**
	 * Starts the LarKC platform. Call this to start and initialize the
	 * platform. Calls the main method without any arguments.
	 * 
	 * @see Larkc#main(String[])
	 */
	public static void start() {
		Larkc.main(null);
	}

	/**
	 * Starts the LarKC platform. Call this to start and initialize the
	 * platform. Calls the main method without any arguments.
	 * 
	 * @param useCyc
	 *            defines if CYC should be used
	 * 
	 * @see Larkc#main(String[])
	 */
	public static void start(boolean useCyc) {
		if (useCyc) {
			Larkc.main(new String[] { "-cyc" });
		} else {
			Larkc.main(null);
		}
	}

	/**
	 * Initializes the platform. Invoked via {@link LarkcInit} Should not be
	 * called directly. This method is marked {@link Deprecated} since it should
	 * not be called directly. Use {@link Larkc#start()} instead.
	 * 
	 * @param useCyc
	 *            defines if cyc should be used or not
	 * 
	 * @see Larkc#start()
	 */
	@Deprecated
	public static void initialize(boolean useCyc) {
		if (useCyc) {
			setPluginRegistry(new CycPluginRegistry(PLUGIN_PATH_PARAMETER));

			// load plug-ins' meta-data to the internal KB
			getPluginRegistry().loadPlugins();

			// Start the Management Interface by declaring the main method of
			// the
			// ManagementInterface enabling the cyc registry to invoke the
			// method
			SubLFiles.declareFunction(ManagementInterfaceMain.class.getName(),
					"start", "START-MANAGEMENT-INTERFACE", 0, 0, false);
		} else {
			pluginRegistry = new SimplePluginRegistry(PLUGIN_PATH_PARAMETER);
			ManagementInterfaceMain.start();
		}

		java.util.Properties props = System.getProperties();
		props.put("gat.adaptor.path", Resources.GAT_ADAPTORS_DIR);
		System.setProperties(props);

		// load the endpoints
		// loadEndpoints();
		endpointRegistry = new EndpointRegistry(ENDPOINT_PATH_PARAMETER);

		/*
		 * while (!SubLMain.isInitialized()) { // Wait for SubLMain
		 * initialization }
		 */

		// initialization complete
		platformStatus = PlatformStatus.RUNNING;
	}

	/**
	 * Returns if CYC is used by this instance of the platform.
	 * 
	 * @return True if CYC is used, false otherwise.
	 */
	public static boolean isCycUsed() {
		return USE_CYC;
	}

	/**
	 * Returns the status of the platform
	 * 
	 * @return the {@link PlatformStatus} which indicates the current status of
	 *         the platform
	 * 
	 * @see PlatformStatus
	 */
	public static PlatformStatus getPlatformStatus() {
		return platformStatus;
	}

	/**
	 * Returns the status of the KB
	 * 
	 * @return the {@link LarkcKBStatus} which indicates the current status of
	 *         the KB
	 * 
	 * @see LarkcKBStatus
	 */
	public static LarkcKBStatus getKBStatus() {
		return kbStatus;
	}

	/**
	 * Sets the status of the KB
	 * 
	 * @param status
	 *            the new {@link LarkcKBStatus}
	 * 
	 * @see LarkcKBStatus
	 */
	public static void setKBStatus(LarkcKBStatus status) {
		kbStatus = status;
	}

	/**
	 * Checks if the platform is running and has been initialized properly.
	 * 
	 * @return true if the platform is running and has been initialized
	 *         properly, false otherwise.
	 */
	public static boolean isInitialized() {
		logger.debug(
				"Checking if platform is initialized... Platform status: {}",
				platformStatus);
		return platformStatus == PlatformStatus.RUNNING;
	}

	/**
	 * Private setter. Sets or updates the pluginRegistry to the passed value.
	 * 
	 * @param pluginRegistry
	 *            the pluginRegistry to set
	 */
	private static void setPluginRegistry(PluginRegistry pluginRegistry) {
		Larkc.pluginRegistry = pluginRegistry;
	}

	/**
	 * Getter. Retrieves the pluginRegistry.
	 * 
	 * @return the pluginRegistry
	 */
	public static PluginRegistry getPluginRegistry() {
		return pluginRegistry;
	}

	/**
	 * Getter. Retrieves the endpointRegistry.
	 * 
	 * @return the endpointRegistry
	 */
	public static EndpointRegistry getEndpointRegistry() {
		return endpointRegistry;
	}

	/**
	 * This method adds a workflow - executor mapping.
	 * 
	 * @param workflowId
	 *            the workflow id
	 * @param executor
	 *            the executor
	 */
	public static void addWorkflowExecutorMapping(UUID workflowId,
			Executor executor) {
		workflowIdExecutorMap.put(workflowId, executor);
		logger.debug("Added workflow - executor mapping");
	}

	/**
	 * This method terminates the executor which is responsible for the specific
	 * workflow.
	 * 
	 * @param workflowId
	 *            the workflow
	 * @throws EndpointShutdownException
	 *             if one or more endpoints are unable to shutdown
	 * @throws IllegalWorkflowGraphException
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 */
	public static void terminateExecutor(UUID workflowId)
			throws EndpointShutdownException, RepositoryException,
			MalformedQueryException, QueryEvaluationException,
			IllegalWorkflowGraphException {
		logger.debug("Terminating executor for workflow {}...", workflowId);
		Executor executor = workflowIdExecutorMap.get(workflowId);
		executor.terminate();
		workflowIdExecutorMap.remove(workflowId);
		logger.debug("Terminated executor for workflow {}.", workflowId);
	}

	/**
	 * This method returns executor for a specific workflow id.
	 * 
	 * @param workflowId
	 *            the workflow id
	 * 
	 * @return the executor for this workflow id
	 */
	public static Executor getExecutor(UUID workflowId) {
		return workflowIdExecutorMap.get(workflowId);
	}

	/**
	 * Gets a free port for next executor trial and error. On error (if the port
	 * is already used) the port will be incremented.
	 * 
	 * @param port
	 *            the port to start from
	 * 
	 * @return a free port for next executor
	 * 
	 */
	public static int findNextFreePort(int port) {
		int tmpPort = port;

		for (;;) {
			try {
				new Socket("localhost", tmpPort);
				tmpPort++;
			} catch (IOException e) {
				// Address already in use;
				logger.warn("Retrying endpoint initialization using another port...");
				return tmpPort;
			}
		}
	}

	/**
	 * Returns all deployed workflows.
	 * 
	 * @return all deployed workflows
	 */
	public static Collection<WorkflowObject> getWorkflows() {
		Collection<WorkflowObject> result = new ArrayList<WorkflowObject>();
		Executor executor;
		WorkflowObject wo;
		for (Entry<UUID, Executor> entry : workflowIdExecutorMap.entrySet()) {
			wo = new WorkflowObject();
			wo.setWorkflowId(entry.getKey());
			executor = entry.getValue();
			for (String endpoint : executor.getAvailableEndpoints()) {
				wo.addEndpoint(endpoint, executor.getEndpoint(endpoint)
						.getURI());
			}

			result.add(wo);
		}
		return result;
	}

	/**
	 * Stops the LarKC platform. The intention of this method is to provide a
	 * clear teardown of the platform and all its components.
	 * 
	 * @throws EndpointShutdownException
	 *             on endpoint shutdown errors (e.g. if a server was unable to
	 *             shutdown)
	 */
	public static void stop() throws EndpointShutdownException {
		logger.warn("Platform shutdown is currently disabled. Reason: "
				+ "There is no way to restart cyc without killing the JVM.");

		// doShutdown();
	}

	/**
	 * FIXME: This method is never called since the platform shutdown is
	 * disabled. As soon as there is a way to restart cyc (and thus the plugin
	 * registry) without killing the JVM, uncomment the call to this method in
	 * {@link #stop()}
	 * 
	 * @see #stop()
	 * @throws EndpointShutdownException
	 * @throws IllegalWorkflowGraphException
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 */
	private static void doShutdown() throws EndpointShutdownException,
			RepositoryException, MalformedQueryException,
			QueryEvaluationException, IllegalWorkflowGraphException {
		logger.info("Shutting down LarKC platform...");
		// Terminate all Executors. The Executor itself will take care of
		// tearing down the endpoints and eventual other resources.
		for (UUID workflowId : workflowIdExecutorMap.keySet()) {
			terminateExecutor(workflowId);
		}

		logger.debug("Stopping management interface...");
		try {
			ManagementInterfaceMain.stop();
		} catch (Exception e) {
			logger.error("Error while stopping management interface: {}",
					e.getLocalizedMessage());
			e.printStackTrace();
		}

		logger.debug("Cleaning up temp files...");

		// HaltCyc task = new HaltCyc();
		// SubLThreadPool.getDefaultPool().execute(task);

		/*
		 * Shut down cyc (plugin registry). This is not an easy task since cyc
		 * does it by exiting via System.exit() which shuts down the JVM.
		 * 
		 * FIXME Luka: Make sure cyc can be shut down and restarted.
		 */
		// Runnable task = new HaltCyc();
		// SubLThreadPool.getDefaultPool().execute(task); // ClassCastException
		// SubLThreadPerTaskExecutor executor = new SubLThreadPerTaskExecutor();
		// executor.execute(task); // multiple NullPointerExceptions

		// Shut down the data layer
		try {
			DataLayerService.shutdown();
			logger.info("Stopped the data layer service successfully!");
		} catch (Throwable t) {
			logger.warn("Failed to shutdown the Data Layer!", t);
		}

		platformStatus = PlatformStatus.STOPPED;
		logger.info("Shutdown complete. Bye!");
	}

	static class HaltCyc extends SafeRunnable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.cyc.tool.subl.util.SafeRunnable#safeRun()
		 */
		@Override
		public void safeRun() {
			operation_communication.halt_cyc_image(CommonSymbols.UNPROVIDED);
		}

	}

}
