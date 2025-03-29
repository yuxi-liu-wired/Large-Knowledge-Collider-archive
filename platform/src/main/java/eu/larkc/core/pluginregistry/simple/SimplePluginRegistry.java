package eu.larkc.core.pluginregistry.simple;

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

import eu.larkc.core.pluginregistry.PluginRegistry;
import eu.larkc.plugin.Plugin;

/**
 * The simple LarKC platform plugin registry.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class SimplePluginRegistry implements PluginRegistry {
	private static Logger logger = LoggerFactory
			.getLogger(SimplePluginRegistry.class);

	private String PLUGIN_DIR = "." + File.separatorChar + "plugins";
	private final HashMap<URI, Class<Plugin>> javaPluginClasses;

	/**
	 * Initializes the registry and populates its KB (ex Cyc KB).
	 * 
	 * @param pluginsPath
	 *            the directory where endpoints are stores. If null default will
	 *            be taken.
	 */
	public SimplePluginRegistry(String pluginsPath) {
		if (pluginsPath != null && pluginsPath.length() != 0) {

			// check if path exists
			File file = new File(PLUGIN_DIR);
			if (file.isDirectory() && file.getAbsoluteFile().exists())
				PLUGIN_DIR = pluginsPath;
			else
				logger.warn(pluginsPath
						+ " is not a valid directory. Default ./plugins will be used for plugins import!");

		}

		javaPluginClasses = new HashMap<URI, Class<Plugin>>();
		loadPlugins();
	}

	/**
	 * Initializes the registry with the default directory for plugins.
	 */
	public SimplePluginRegistry() {
		this(null);
	}

	/**
	 * Instantiates the plugin.
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
			SecurityException, NoSuchMethodException {
		Class<Plugin> plugin = javaPluginClasses.get(pluginUri);

		if (plugin == null)
			return null;

		Constructor<Plugin> constructor = plugin.getConstructor(URI.class);
		return constructor.newInstance(pluginUri);
	}

	/**
	 * Load plugins.
	 */
	public void loadPlugins() {
		javaPluginClasses.clear();

		File pluginDir = new File(PLUGIN_DIR);
		File[] pluginFiles = pluginDir.listFiles();

		if (pluginFiles != null && pluginFiles.length != 0) {
			for (File file : pluginFiles) {
				findPlugins(file);
			}
		} else {
			logger.warn("No plugins in the plugin directory ({})", PLUGIN_DIR);
		}
		logger.debug("Loaded available plugins from '{}'.", PLUGIN_DIR);
	}

	/**
	 * Registers the plugin or more plugins in the directory, but only one level
	 * deep.
	 * 
	 * @param fileOrDir
	 *            directory or a file location of the plugins
	 */
	private void findPlugins(File fileOrDir) {
		InputStream wsdlFile = null;
		File wsdlFileParent = null;

		if (fileOrDir.isDirectory()) {
			for (File file : fileOrDir.listFiles()) {
				if (!file.isDirectory())
					findPlugins(file);
			}
			return;
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
					findPlugins(unzipWhere);
					return;
				} catch (ZipException e) {
					logger.warn(
							"Cannot extract " + fileOrDir.getAbsolutePath(), e);
				} catch (IOException e) {
					logger.warn(
							"Cannot extract " + fileOrDir.getAbsolutePath(), e);
				}
			} else
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
		for (int pluginNum = 0; pluginNum < services.getLength(); pluginNum++) {
			Element plugin = (Element) services.item(pluginNum);
			String pluginName = plugin.getAttribute("name");

			NodeList plugins = plugin.getElementsByTagName("wsdl:endpoint");
			String location = ((Element) plugins.item(0))
					.getAttribute("location");

			if (location.startsWith("java:")) {
				loadPluginClass(new URIImpl(pluginName), location.substring(5),
						wsdlFileParent);
			} else {
				logger.warn("Plugin could not be registered: {}", pluginName);
			}
		}
	}

	/**
	 * Returns the URIs of all the plug-ins available to the platform
	 * 
	 * @return Collection of plug-in URIs
	 */
	public Collection<URI> getAllPlugins() {
		return javaPluginClasses.keySet();
	}

	/**
	 * Finds the class file and loads it with ClassLoader.
	 * 
	 * @param pluginName
	 *            name of the plugin
	 * @param _class
	 *            name with package included
	 * @param pluginPath
	 *            path to the root of where the class is
	 */
	@SuppressWarnings("unchecked")
	private void loadPluginClass(URI pluginName, String _class, File file) {
		try {
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			ClassLoader classLoader = new URLClassLoader(urls);
			// load the class
			Class<Plugin> pluginClass = (Class<Plugin>) classLoader
					.loadClass(_class);

			javaPluginClasses.put(pluginName, pluginClass);
		} catch (MalformedURLException e) {
			Errors.handleError(e);
		} catch (ClassNotFoundException e) {
			logger.warn("Classloader cannot find class: " + e.getMessage()
					+ "! Plugin not loaded!");
		} catch (ClassCastException e) {
			Errors.handleError("Plugin \"" + _class + "\" must implement"
					+ Plugin.class.getName() + " interface", e);
		} catch (IncompatibleClassChangeError e) {
			logger.warn("Classloader cannot load plugin class: "
					+ _class
					+ "! The plugin API had changed. Please recompile the plugin. Plugin not loaded!");
		}
	}

	/**
	 * Unzips the .larkc or other file into unzipSubDir directory.
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

	/**
	 * Returns true if the URI specifies a plug-in.
	 * 
	 * @param object
	 *            the URI of the object
	 * @return true if the URI specifies a plug-in.
	 */
	public boolean isLarKCPlugin(URI object) {
		return javaPluginClasses.keySet().contains(object);
	}
}
