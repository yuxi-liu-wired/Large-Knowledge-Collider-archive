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
package eu.larkc.core.pluginManager.remote.Servlet.Tomcat.pluginconverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 
 * Class to convert .larkc plugin files to .war files. This is necessary in the
 * process of wrapping LarKC plug-ins inside servlet containers.
 * 
 * @author Barry Bishop
 * 
 */
public class Convert {

	/**
	 * Enables converting from the command line. Parameters are the same order
	 * as in the constructor: input .larkc file, output .war file, plugin class,
	 * plugin type. The pluginType will be verified to be one of
	 * <code>data_transformer, query_transformer, reasoner, identifier, selecter, decider</code>
	 * .
	 * 
	 * @see #Convert(String, String, String, String)
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			usage();
			System.exit(1);
		}

		new Convert(args[0], args[1], args[2]);
	}

	/**
	 * <p>
	 * Takes one .larkc file and converts it to the specified .war file. The
	 * extension has to be specified with both the inputLarkc and the outputWar
	 * files. Further, the plugin class has to be provided as well as the plugin
	 * type.
	 * </p>
	 * 
	 * <p>
	 * Example: <blockquote>Convert("SindiceTriplePatternIdentifier.larkc",
	 * "SindiceTriplePatternIdentifier.war",
	 * "eu.larkc.plugin.identify.sindice.SindiceTriplePatternIdentifier",
	 * "identifier")</blockquote>
	 * </p>
	 * 
	 * @param inputLarkc
	 *            the input file to convert. Has to be a LarKC plugin file
	 *            (.larkc)
	 * @param outputWar
	 *            the target output .war file
	 * @param pluginClass
	 *            full plug-in class name. E.g.
	 *            <code>eu.larkc.plugin.identify.sindice.SindiceTriplePatternIdentifier</code>
	 */
	public Convert(String inputLarkc, String outputWar, String pluginClass) {

		doubleLine();

		System.out.println("INPUT:  " + inputLarkc);
		System.out.println("OUTPUT: " + outputWar);
		line();

		try {
			ZipFile zipfile = new ZipFile(inputLarkc);

			FileOutputStream outStream = new FileOutputStream(outputWar);
			ZipOutputStream zipOutStream = new ZipOutputStream(
					new BufferedOutputStream(outStream));

			// STEP 1. Copy the contents from the .larkc file to the .war file.
			Enumeration e = zipfile.entries();

			while (e.hasMoreElements()) {
				ZipEntry inEntry = (ZipEntry) e.nextElement();
				if (inEntry.isDirectory()) {
					// Required ?????????
					// new File(entry.getName()).mkdir();
					// zipOutStream.putNextEntry( inEntry );
				} else {
					ZipEntry outEntry = new ZipEntry(inEntry);
					String name = inEntry.getName();

					if (!name.endsWith(".java")) {
						// Move lib directory to WEB-INF/lib
						if (name.startsWith("lib/")) {
							name = WEB_INF + name;
							outEntry = changeName(inEntry, name);

							System.out.println("Moving: " + inEntry + " => "
									+ outEntry);
						} else if (name.endsWith(".class")) {
							name = WEB_INF + "classes/" + name;
							outEntry = changeName(inEntry, name);

							System.out.println("Moving: " + inEntry + " => "
									+ outEntry);
						} else {
							System.out.println("Copying: " + inEntry);
						}

						BufferedInputStream fileInputStream = new BufferedInputStream(
								zipfile.getInputStream(inEntry));

						zipOutStream.putNextEntry(outEntry);

						copy(fileInputStream, zipOutStream);
						fileInputStream.close();
					}
				}
			}

			// STEP 2. Add the platform jar
			InputStream platformJar = new FileInputStream(
					"target/platform-2.5.0-SNAPSHOT-LarkcAssembly.jar");
			ZipEntry platformOutEntry = new ZipEntry(WEB_INF + "lib/"
					+ "platform-2.5.0-SNAPSHOT-LarkcAssembly.jar");
			zipOutStream.putNextEntry(platformOutEntry);
			copy(platformJar, zipOutStream);

			// STEP 3. Create and add the web.xml file
			String webXmlZip = WEB_INF + "web.xml";
			System.out.println("Creating: " + webXmlZip);
			String containerClassname = "eu.larkc.core.pluginManager.remote.Servlet.Tomcat.servlet.PluginServlet";
			String webXml = generateWebXml(pluginClass, containerClassname);
			ZipEntry outEntry = new ZipEntry(WEB_INF + "web.xml");
			zipOutStream.putNextEntry(outEntry);
			zipOutStream.write(webXml.getBytes());

			outStream.flush();
			zipOutStream.close();

			System.out.println("FINISHED.");
		} catch (Exception e) {
			System.err.println("An error occurred: " + e);
		}
	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		final byte data[] = new byte[2048];
		int count;
		while ((count = in.read(data, 0, data.length)) != -1) {
			out.write(data, 0, count);
		}
	}

	private ZipEntry changeName(ZipEntry entry, String newName) {
		ZipEntry result = new ZipEntry(newName);

		result.setComment(entry.getComment());
		result.setMethod(entry.getMethod());
		result.setTime(entry.getTime());

		return result;

	}

	private static void line() {
		System.out
				.println("---------------------------------------------------------------");
	}

	private static void doubleLine() {
		System.out
				.println("===============================================================");
	}

	private String generateWebXml(String pluginClass, String pluginContainer) {
		StringBuilder buffer = new StringBuilder();
		String br = System.getProperty("line.separator");

		final String propertyEditorServlet = "eu.larkc.core.pluginManager.jee.server.PropertyEditorServlet";
		final String propertyEditorUrlPattern = "/properties";

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(br);
		buffer.append("<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		buffer.append("xmlns=\"http://java.sun.com/xml/ns/javaee\" ");
		buffer.append("xmlns:web=\"http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" ");
		buffer.append("xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" ");
		buffer.append("id=\"WebApp_ID\" version=\"2.5\">").append(br);
		buffer.append("  <display-name>").append("LarkcPlugin")
				.append("</display-name>").append(br);
		buffer.append("  <welcome-file-list>").append(br);
		buffer.append("    <welcome-file>index.html</welcome-file>").append(br);
		buffer.append("    <welcome-file>index.htm</welcome-file>").append(br);
		buffer.append("    <welcome-file>index.jsp</welcome-file>").append(br);
		buffer.append("    <welcome-file>default.html</welcome-file>").append(
				br);
		buffer.append("    <welcome-file>default.htm</welcome-file>")
				.append(br);
		buffer.append("    <welcome-file>default.jsp</welcome-file>")
				.append(br);
		buffer.append("  </welcome-file-list>").append(br);
		buffer.append("  <servlet>").append(br);
		buffer.append("    <description>Servlet for wrapping a ")
				.append("LarkcPlugin").append(" plug-in.</description>")
				.append(br);
		buffer.append("    <display-name>").append("LarkcPlugin")
				.append("</display-name>").append(br);
		buffer.append("    <servlet-name>").append(pluginClass)
				.append("</servlet-name>").append(br);
		buffer.append("    <servlet-class>").append(pluginContainer)
				.append("</servlet-class>").append(br);
		buffer.append("    <init-param>").append(br);
		buffer.append(
				"      <description>The class name of the plug-in.</description>")
				.append(br);
		buffer.append("      <param-name>PluginClassname</param-name>").append(
				br);
		buffer.append("      <param-value>").append(pluginClass)
				.append("</param-value>").append(br);
		buffer.append("    </init-param>").append(br);
		buffer.append("  </servlet>").append(br);
		buffer.append("  <servlet-mapping>").append(br);
		buffer.append("    <servlet-name>").append(pluginClass)
				.append("</servlet-name>").append(br);
		buffer.append("    <url-pattern>/").append("LarkcPlugin")
				.append("/*</url-pattern>").append(br);
		buffer.append("  </servlet-mapping>").append(br);

		buffer.append("  <servlet>").append(br);
		buffer.append("    <description>Plugin property editor</description>")
				.append(br);
		buffer.append("    <display-name>").append("LarkcPlugin")
				.append("</display-name>").append(br);
		buffer.append("    <servlet-name>PropertyEditor</servlet-name>")
				.append(br);
		buffer.append("    <servlet-class>").append(propertyEditorServlet)
				.append("</servlet-class>").append(br);
		buffer.append("  </servlet>").append(br);
		buffer.append("  <servlet-mapping>").append(br);
		buffer.append("    <servlet-name>PropertyEditor</servlet-name>")
				.append(br);
		buffer.append("    <url-pattern>").append(propertyEditorUrlPattern)
				.append("/*</url-pattern>").append(br);
		buffer.append("  </servlet-mapping>").append(br);

		buffer.append("</web-app>").append(br);

		return buffer.toString();
	}

	private static final String WEB_INF = "WEB-INF/";

	private static final String WEB_APP_LIB = "webapp_lib";

	private static void usage() {
		System.err.println("Usage: java " + Convert.class.getName()
				+ " <input_plugin_package>.larkc <output_war_file>.war "
				+ "<plugin_implementation_class> " + "");
	}
}
