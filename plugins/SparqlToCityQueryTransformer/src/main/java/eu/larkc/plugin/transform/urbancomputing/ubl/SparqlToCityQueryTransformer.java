package eu.larkc.plugin.transform.urbancomputing.ubl;

import java.text.ParseException;
import java.util.Date;

import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.plugin.Plugin;

/**
 * @author Daniele Dell'Aglio
 * Ported to 2.0 by Bas Groenewoud and Chris Dijkshoorn
 */
public class SparqlToCityQueryTransformer extends Plugin {

	//only first time when called, return results (anytime b.)
	@SuppressWarnings("unused")
	private boolean once = false;
	
	private static final URI nameUri = new URIImpl("http://schemas.talis.com/2005/address/schema#localityName");
	private static final URI countryUri = new URIImpl("http://schemas.talis.com/2005/address/schema#countryName");
	private static final URI latUri = new URIImpl("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
	private static final URI lngUri = new URIImpl("http://www.w3.org/2003/01/geo/wgs84_pos#long");
	private static final URI rangeUri = new URIImpl("http://www.example.org/range");
	
	private String name = null;;
	private String country = null;;
	private double lat = Double.NaN;
	private double lng = Double.NaN;
	@SuppressWarnings("unused")
	private double range = Double.NaN;
	
	//Initialize the logger
	protected static Logger logger = LoggerFactory.getLogger(Plugin.class);

	public SparqlToCityQueryTransformer(URI pluginName) {
		super(pluginName);
	}

	
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
	}

	public SetOfStatements invokeInternal(SetOfStatements input) {
		System.out.println("EventQueryToReasonerQueryTransformer:invokeInternal-start");
		logger.debug("Starting SparqlToCityQueryTrasformer");
		SPARQLQuery theQuery = DataFactory.INSTANCE.createSPARQLQuery(input);
		SPARQLQuery query = (SPARQLQuery) theQuery;
		SPARQLQuery result = null;
		String strQuery = theQuery.toString();
		Date start = new Date();
		Date end = new Date();
		int filter = strQuery.indexOf("FILTER");
		
		if(filter >= 0){
			strQuery=strQuery.substring(filter);
			int from=strQuery.indexOf("dateTime");
			int to=filter=strQuery.lastIndexOf("dateTime");
			if(from>0)
				if(from!=to){
					String f = strQuery.substring(from, to);
					f=f.substring(f.indexOf("\"")+1, f.lastIndexOf("\"")-1);
					java.text.SimpleDateFormat formatter;

					formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					try {
						start = formatter.parse(f);
					} catch (ParseException e) {
						logger.error("The date format is not valid");
						e.printStackTrace();
					}
					
					f = strQuery.substring(to);
					f=f.substring(f.indexOf("\"")+1, f.lastIndexOf("\"")-1);

					try {
						end = formatter.parse(f);
					} catch (ParseException e) {
						logger.error("The date format is not valid");
						e.printStackTrace();
					}
				}
				else{
					String f = strQuery.substring(from);
					f=f.substring(f.indexOf("\"")+1, f.lastIndexOf("\"")-1);
					java.text.SimpleDateFormat formatter;

					formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					try {
						start = formatter.parse(f);
						end=start;
					} catch (ParseException e) {
						logger.error("The date format is not valid");
						e.printStackTrace();
					}
				}
		}
		
		if(query instanceof SPARQLQueryImpl)
		{
			StatementPatternCollector spc = new StatementPatternCollector();
			TupleExpr te = ((SPARQLQueryImpl) query).getParsedQuery().getTupleExpr();
			te.visit(spc);
			
			for (StatementPattern sp : spc.getStatementPatterns()){
				URI p = (URI) sp.getPredicateVar().getValue();
				if (p.equals((URI)nameUri)) {
					Literal o = (Literal) sp.getObjectVar().getValue();
					name = o.stringValue();
				} else if (p.equals((URI) countryUri)) {
					Literal o = (Literal) sp.getObjectVar().getValue();
					country = o.stringValue();
				} else if (p.equals((URI) latUri)) {
					Value v = sp.getObjectVar().getValue();
					if(v instanceof Literal)
						lat = ((Literal)v).doubleValue();
				} else if (p.equals((URI) lngUri)) {
					Value v = sp.getObjectVar().getValue();
					if(v instanceof Literal)
						lng = ((Literal)v).doubleValue();
				} else if (p.equals((URI) rangeUri)) {
					Value v = sp.getObjectVar().getValue();
					if(v instanceof Literal)
						range = ((Literal)v).doubleValue();
				}
			}
			logger.debug("Defined name, country, lat, lng and range.");
			if(name != null){
				ToponymSearchResult searchResult;
				try {
					logger.debug("Initialize webService.");
					searchResult = WebService.search(null, country==null?null:country, name, new String[]{"PPLA", "PPLC"}, 0);
					logger.debug("Got searchresult.");
					for (Toponym toponym : searchResult.getToponyms()) {
						lat=toponym.getLatitude();
						lng=toponym.getLongitude();
					}
				} catch (Exception e) {
					logger.error("The connection with GeoNames failed");
				}
			}
			java.text.SimpleDateFormat formatter;
			formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			
			result = new SPARQLQueryImpl(
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfcal: <http://www.w3.org/2002/12/cal/icaltzd#> " +
					"PREFIX addr: <http://schemas.talis.com/2005/address/schema#> " +
					"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " +
					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
					"DESCRIBE ?e WHERE {" +
					"?e rdf:type rdfcal:Vevent. " +
					"?e geo:location ?l. " +
					"?l rdfs:label ?lab. " +
					(Double.isNaN(lat)?"":"?l geo:lat \""+lat+"\". ") +
					(Double.isNaN(lng)?"":"?l geo:long \""+lng+"\". ") +
					"?e rdfcal:dtstart ?s . " +
					"?l addr:localityName \""+name+"\". " +
					"FILTER(?s > xsd:dateTime(\""+formatter.format(start)+"\") && ?s < xsd:dateTime(\""+formatter.format(end)+"\")). " +
					"}"
				);
		}
		System.out.println("SparqlToCityQueryTransformer:invokeInternal-end");
		return result.toRDF();
	}

	public void shutdownInternal() {
	}
}