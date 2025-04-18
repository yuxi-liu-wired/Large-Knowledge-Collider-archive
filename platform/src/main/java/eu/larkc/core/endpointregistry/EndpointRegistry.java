package eu.larkc.core.endpointregistry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cyc.tool.subl.jrtl.nativeCode.subLisp.Errors;

import eu.larkc.core.endpoint.Endpoint;
import eu.larkc.core.executor.Executor;

/**
 * The LarKC platform endpoint registry.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class EndpointRegistry {
	private static Logger logger = LoggerFactory
			.getLogger(EndpointRegistry.class);

	private String ENDPOINT_DIR = "." + File.separatorChar + "endpoints";
	private final HashMap<URI, Class<Endpoint>> javaEndpointClasses;

	/**
	 * Initializes the registry and populates its KB (ex Cyc KB).
	 * 
	 * @param endpointsPath
	 *            the directory where endpoints are stores. If null default will
	 *            be taken.
	 */
	public EndpointRegistry(String endpointsPath) {
		if (endpointsPath != null && endpointsPath.length() != 0) {

			// check if path exists
			File file = new File(ENDPOINT_DIR);
			if (file.isDirectory() && file.getAbsoluteFile().exists())
				ENDPOINT_DIR = endpointsPath;
			else
				logger.warn(endpointsPath
						+ " is not a valid directory. Default ./endpoints will be used for endpoints import!");

		}

		javaEndpointClasses = new HashMap<URI, Class<Endpoint>>();
		loadEndpoints();
	}

	/**
	 * Initializes the registry with the default directory for endpoints.
	 */
	public EndpointRegistry() {
		this(null);
	}

	/**
	 * Instantiates the endpoint.
	 * 
	 * @param endpointUri
	 *            The URI of the desired endpoint.
	 * @param executor
	 *            The executor that is responsible for this endpoint.
	 * @return A new endpoint instance of the desired type.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public Endpoint getNewEndpointInstance(URI endpointUri, Executor executor)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class<Endpoint> endpoint = javaEndpointClasses.get(endpointUri);

		if (endpoint == null)
			return null;

		Constructor<Endpoint> constructor = endpoint
				.getConstructor(Executor.class);
		return constructor.newInstance(executor);
	}

	/**
	 * Load endpoints from PLATFORM/endpoints.
	 */
	public void loadEndpoints() {
		javaEndpointClasses.clear();

		File endpointDir = new File(ENDPOINT_DIR);
		File[] endpointFiles = endpointDir.listFiles();

		if (endpointFiles != null && endpointFiles.length != 0) {
			for (File file : endpointFiles) {
				findEndpoints(file);
			}
		} else {
			logger.warn("No endpoints in the endpoints directory ({})",
					ENDPOINT_DIR);
		}
		logger.debug("Loaded available endpoints from '{}'.", ENDPOINT_DIR);
	}

	/**
	 * Returns the URIs of all the endpoints available to the platform
	 * 
	 * @return Collection of endpoint URIs
	 */
	public Collection<URI> getAllEndpoints() {
		return javaEndpointClasses.keySet();
	}

	/**
	 * Registers the endpoint or more endpoints in the directory, but only one
	 * level deep.
	 * 
	 * @param fileOrDir
	 *            directory or a file location of the endpoints
	 */
	private void findEndpoints(File fileOrDir) {
		InputStream wsdlFile = null;
		File wsdlFileParent = null;

		if (fileOrDir.isDirectory()) {
			for (File file : fileOrDir.listFiles()) {
				if (!file.isDirectory())// only check directories one level deep
					findEndpoints(file);
			}// list files in the sub-directory
			return;// after scanned all the files it already registered whatever
			// it had. So the method should end.
		} else {
			String fileOrDirName = fileOrDir.getName();
			if (fileOrDirName.endsWith(".wsdl")) {
				try {
					wsdlFile = new FileInputStream(fileOrDir);
				} catch (FileNotFoundException e) {
					logger.warn("File doesn't exists: "
							+ fileOrDir.getAbsolutePath());
					return;
				}
				wsdlFileParent = fileOrDir.getParentFile();
			} else if (fileOrDirName.endsWith(".larkc")
					|| fileOrDirName.endsWith(".jar")) {
				try {
					String unzipDirName = fileOrDirName.substring(0,
							fileOrDirName.lastIndexOf('.'));
					File unzipWhere = new File(fileOrDir.getParentFile()
							.getAbsolutePath() + File.separator + unzipDirName);
					unzip(fileOrDir, unzipDirName);
					findEndpoints(unzipWhere);
					return;
				} catch (ZipException e) {
					logger.warn(
							"Cannot extract " + fileOrDir.getAbsolutePath(), e);
				} catch (IOException e) {
					logger.warn(
							"Cannot extract " + fileOrDir.getAbsolutePath(), e);
				}
			} else
				// ignore other files
				return;
		}

		Document document;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(wsdlFile);
		} catch (Exception e) {
			logger.warn("Error parsing the wsdl file " + wsdlFile + " in:"
					+ fileOrDir.getAbsolutePath(), e);
			return;
		}

		NodeList services = document.getElementsByTagName("wsdl:service");
		for (int endpointNum = 0; endpointNum < services.getLength(); endpointNum++) {
			Element endpoint = (Element) services.item(endpointNum);
			String endpointName = endpoint.getAttribute("name");

			NodeList endpoints = endpoint.getElementsByTagName("wsdl:endpoint");
			String location = ((Element) endpoints.item(0))
					.getAttribute("location");

			if (location.startsWith("java:")) {
				loadEndpointClass(new URIImpl(endpointName),
						location.substring(5), wsdlFileParent);
				// logger.info("Registered " + location.substring(5));
			} else {
				logger.warn("Endpoint could not be registered: {}",
						endpointName);
			}
		}
	}

	/**
	 * Finds the class file and loads it with ClassLoader
	 * 
	 * @param endpointName
	 *            name of the endpoint
	 * @param _class
	 *            name with package included
	 * @param pluginPath
	 *            path to the root of where the class is
	 */
	@SuppressWarnings("unchecked")
	private void loadEndpointClass(URI endpointName, String _class, File file) {
		try {
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			ClassLoader classLoader = new URLClassLoader(urls);
			// load the class
			Class<Endpoint> endpointClass = (Class<Endpoint>) classLoader
					.loadClass(_class);

			javaEndpointClasses.put(endpointName, endpointClass);
		} catch (MalformedURLException e) {
			Errors.handleError(e);
		} catch (ClassNotFoundException e) {
			logger.warn("Classloader cannot find class: " + e.getMessage()
					+ "! Endpoint not loaded!");
		} catch (ClassCastException e) {
			Errors.handleError("Endpoint \"" + _class + "\" must implement"
					+ Endpoint.class.getName() + " interface", e);
		} catch (IncompatibleClassChangeError e) {
			logger.warn("Classloader cannot load endpoint class: "
					+ _class
					+ "! The endpoint API had changed. Please recompile the endpoint. Endpoint not loaded!");
		}
	}

	/**
	 * Unzips the .larkc or other file into unzipSubDir directory
	 */
	private void unzip(File file, String unzipSubDir) throws ZipException,
			IOException {
		int BUFFER = 2048;
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(file);

		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			is = new BufferedInputStream(zipfile.getInputStream(entry));
			int count;
			byte data[] = new byte[BUFFER];

			File where = new File(file.getParentFile().getAbsolutePath()
					+ File.separator + unzipSubDir + File.separator
					+ entry.getName());
			if (entry.isDirectory()) {
				where.mkdirs();
				continue;
			} else {
				where.getParentFile().mkdirs();
				where.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(
					where.getCanonicalFile());
			dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
			is.close();
		}
	}
}
