package eu.larkc.plugin.reasoner.iris;

/**
 * Helper class to manipulate strings.
 * 
 * @author Christoph Fuchs, Iker Larizgoitia
 * 
 */
public class StringHelper {

	/**
	 * Removes trailing and ending single quotes.
	 * 
	 * @param str
	 *            string to be manipulated
	 * @return given string, without trailing and ending single quotes.
	 */
	public static String removeSingleQuotes(String str) {
		str = (String) ((str.startsWith("'") && str.endsWith("'")) ? str
				.subSequence(1, str.length() - 1) : str);
		return str;
	}

	/**
	 * Removes trailing and ending double quotes.
	 * 
	 * @param str
	 *            string to be manipulated
	 * @return given string, without trailing and ending double quotes.
	 */
	public static String removeDoubleQuotes(String str) {
		str = (String) ((str.startsWith("\"") && str.endsWith("\"")) ? str
				.subSequence(1, str.length() - 1) : str);
		return str;
	}

}
