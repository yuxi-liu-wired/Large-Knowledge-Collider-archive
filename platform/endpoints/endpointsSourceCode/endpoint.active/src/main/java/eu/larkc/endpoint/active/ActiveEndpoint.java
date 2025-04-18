package eu.larkc.endpoint.active;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.endpoint.Endpoint;
import eu.larkc.core.endpoint.EndpointException;
import eu.larkc.core.endpoint.EndpointInitalizationException;
import eu.larkc.core.endpoint.EndpointShutdownException;
import eu.larkc.core.executor.Executor;

/**
 * This class represents the main class of the endpoint.
 * 
 * -----------------------------------
 * No changes are needed in this class
 * -----------------------------------
 */
public class ActiveEndpoint extends Endpoint {

	private static Logger logger = LoggerFactory
			.getLogger(ActiveEndpoint.class);
	private static Component component;
	private Server restletServer;

	/**
	 * Custom constructor
	 * 
	 * @param ex
	 *            the executor
	 */
	public ActiveEndpoint(Executor ex) {
		super(ex, "/endpoint");
	}

	/**
	 * This method starts the endpoint and attaches the the resource to the path /endpoint.
	 * 
	 * @param port
	 * 			  the port where the resource is attaches
	 */
	@Override
	public void start(int port) throws EndpointException {
		// Create a new Component.
		component = new Component();

		// Create a new server
		restletServer = new Server(Protocol.HTTP, port);

		// Add the new HTTP server
		component.getServers().add(restletServer);

		component.getDefaultHost().attach("/endpoint",
				new ActiveEndpointApp(this));

		try {
			component.start();
		} catch (Exception e) {
			throw new EndpointInitalizationException(e);
		}
		setPort(port);
		initialized = true;
		logger.info("Endpoint started on port " + port);
	}

	/**
	 * This method stops the endpoint.
	 * 
	 * @throws EndpointShutdownException
	 */
	@Override
	public void stop() throws EndpointShutdownException {
		try {
			component.stop();
		} catch (Exception e) {
			throw new EndpointShutdownException(e);
		}
		initialized = false;
		logger.info("Endpoint on port " + getPort() + " stopped");
	}
}
