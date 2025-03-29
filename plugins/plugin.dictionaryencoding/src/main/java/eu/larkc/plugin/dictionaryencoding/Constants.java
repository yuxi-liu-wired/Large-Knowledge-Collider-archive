package eu.larkc.plugin.dictionaryencoding;

import org.openrdf.model.impl.URIImpl;

public interface Constants {
	public static final URIImpl DICTIONARYMODE = new URIImpl("http://larkc.eu/schema#dictionaryMode");
	public static final String ENCODE = "encode";
	public static final String DECODE = "decode";
	public static final URIImpl FILEPATH = new URIImpl("http://larkc.eu/schema#filePath");
	public static final String LASTMODIFIED =  "larkc:lastmodified";
	public static final String SIZE = "larkc:size";
}
