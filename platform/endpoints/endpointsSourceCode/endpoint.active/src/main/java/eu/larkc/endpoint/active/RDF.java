package eu.larkc.endpoint.active;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class RDF {
	public static String CYC_PREFIX = "http://example.com/cyc#";
	public static String GC_PREFIX = "http://example.com/GCPlugin/cyc#";

	public static final URI URI_EVENT = new URIImpl(CYC_PREFIX + "event");
	public static final URI URI_EVENT_HASNAME = new URIImpl(CYC_PREFIX
			+ "eventHasName");
	public static final URI URI_PREFERRED_NAME_STRING = new URIImpl(CYC_PREFIX
			+ "preferredNameString");
	public static final URI URI_EVENT_HASLOCATION = new URIImpl(CYC_PREFIX
			+ "eventHasLocation");
	public static final URI URI_EVENT_HASOWNER = new URIImpl(CYC_PREFIX
			+ "eventHasOwner");
	public static final URI URI_SUBEVENTS = new URIImpl(CYC_PREFIX
			+ "subEvents");

	public static final URI URI_PROVIDING_LODGING = new URIImpl(CYC_PREFIX
			+ "ProvidingLodging");
}
