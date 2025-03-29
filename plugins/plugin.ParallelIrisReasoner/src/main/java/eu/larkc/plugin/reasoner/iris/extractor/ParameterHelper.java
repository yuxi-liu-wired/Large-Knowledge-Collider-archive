package eu.larkc.plugin.reasoner.iris.extractor;

import java.net.MalformedURLException;
import java.net.URL;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;

/**
 * Helper class to extract plug-in parameters.
 * 
 * @author Iker Larizgoitia, Christoph Fuchs
 * 
 */
public class ParameterHelper {

	/**
	 * Extracts an URL from a SetOfStatements which is specified by the
	 * predicate parameterURI.
	 * 
	 * @param parameterURI
	 *            URI of the parameter (predicate)
	 * @param pluginParameters
	 *            plugin parameters wrapped in a SetOfStatements
	 * @return the extracted URL
	 */
	public static URL extractURL(String parameterURI,
			SetOfStatements pluginParameters) {
		Value object = extractObject(parameterURI, pluginParameters);

		return objectToURL(object);
	}

	/**
	 * Extracts a string from a SetOfStatements which is specified by the
	 * predicate parameterURI.
	 * 
	 * @param parameterURI
	 *            URI of the parameter (predicate)
	 * @param pluginParameters
	 *            plugin parameters wrapped in a SetOfStatements
	 * @return the extracted string
	 */
	public static String extractString(String parameterURI,
			SetOfStatements pluginParameters) {
		Value object = extractObject(parameterURI, pluginParameters);

		if (object == null) {
			throw new IllegalArgumentException("Unable to extract parameter "
					+ parameterURI + " from the following pluginParameters: "
					+ pluginParameters);
		}

		String value = object.toString();

		// value has a double "", why?
		value = value.substring(1, value.length() - 1);

		return value;
	}

	private static Value extractObject(String parameterURI,
			SetOfStatements pluginParameters) {
		CloseableIterator<Statement> statements = pluginParameters
				.getStatements();
		Statement stmt;

		Value object = null;

		while (statements.hasNext()) {
			stmt = statements.next();
			String predicateName = stmt.getPredicate().toString();

			if (predicateName.equals(parameterURI)) {
				object = stmt.getObject();
			}
		}
		return object;
	}

	private static URL objectToURL(Value object) {
		if (object == null)
			return null;

		// value is a URL?
		try {
			return new URL(object.toString());
		} catch (MalformedURLException e) {
			// Ignored
		}

		String value = object.toString();

		// value has a double "", why?
		value = value.substring(1, value.length() - 1);
		return ParameterHelper.class.getClass().getResource(value);
	}

}
