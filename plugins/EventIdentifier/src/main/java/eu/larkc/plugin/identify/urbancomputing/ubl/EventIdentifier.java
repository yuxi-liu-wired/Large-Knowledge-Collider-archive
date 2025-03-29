package eu.larkc.plugin.identify.urbancomputing.ubl;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import javax.management.RuntimeErrorException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.model.Value;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.plugin.Plugin;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;

/**
 * An identifier to extract from Eventful the list of events that are in the input city
 *
 * @author Daniele Dell'Aglio
 * Ported to 2.0 by Bas Groenewoud and Chris Dijkshoorn
 */
public class EventIdentifier extends Plugin{
	
//	private static Logger logger = LoggerFactory.getLogger(Plugin.class);

	private static boolean importConfig = false;

	private static float DEFAULT_RANGE = 5;

	private static final URI localityUri = new URIImpl("http://schemas.talis.com/2005/address/schema#localityName");
	private static final URI countryUri = new URIImpl("http://schemas.talis.com/2005/address/schema#countryName");
	private static final URI latUri = new URIImpl("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
	private static final URI lngUri = new URIImpl("http://www.w3.org/2003/01/geo/wgs84_pos#long");
	private static final URI rangeUri= new URIImpl("http://www.example.org/range");

	public static final URI hasApiKey = new URIImpl("http://larkc.cefriel.it/ontologies/urbancomputing#hasApiKey");
	public static final URI hasXsltTransformation = new URIImpl("http://larkc.cefriel.it/ontologies/urbancomputing#hasXsltTransformation");
	
	//only first time when called, return results (anytime b.)
	private boolean once = false;
	
	private String eventfulKey;
	private URI xsltTransf;

	
	public EventIdentifier(URI pluginName) {
		super(pluginName);
	}
	
	protected void initialiseInternal(SetOfStatements arg0) {
		logger.info("Event Identifier, initialization");
		CloseableIterator<Statement> it = arg0.getStatements();
		while(it.hasNext()){
			Statement s = it.next();
			if(s.getPredicate().equals(hasApiKey))
				eventfulKey = s.getObject().stringValue();
			if(s.getPredicate().equals(hasXsltTransformation))
				xsltTransf = new URIImpl(s.getObject().stringValue());
		}
		logger.debug("API key: {}", eventfulKey);
		logger.debug("XSLT: {}", xsltTransf);

		logger.info("Event Identifier system, all green!");
//		if(!importConfig){
//			DataFactory.INSTANCE.createRdfStoreConnection().removeStatement(null, null, null, this.getIdentifier());
//			LarKCUtilities.importRdfGraph(new File("plugins/EventIdentifier/configuration-schema/configuration.rdf"), this.getIdentifier());
//			importConfig=true;
//		}
//		
	}

	protected SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info("EventIdentifier, fire!");
		//Get SparQL query
		SPARQLQuery theQuery = DataFactory.INSTANCE.createSPARQLQuery(input);
		
		if (once) { return null; }
		once = true;

		logger.debug("Query to be processed: {}", theQuery);

//		Config config = generateConfig();
		City city = new City(theQuery);
//		String eventfulKey = config.getEvdbKey();
		
		if(eventfulKey==null){
			logger.error("The EVDB key has not been set!");
			throw new RuntimeErrorException(new Error("The EVDB key has not been set!"));
		}

		Collection<SetOfStatements> ret = new ArrayList<SetOfStatements>();
		ret.addAll(generateEvdbDocs(city));//, config));
		logger.debug("Found {} events", ret.size());

		List<Statement> allStatements = new ArrayList<Statement>(10000);
		
		//Add all statements from ret to allStatements to replace queue
		for (SetOfStatements sos : ret) {
			CloseableIterator<Statement> iter = sos.getStatements();
			while (iter.hasNext()) {
				allStatements.add(iter.next());
			}	
		}
		
		SetOfStatementsImpl output = new SetOfStatementsImpl( allStatements );
		System.out.println("EventIdentifier:invokeInternal-start");
		return output;
	}

	
	@Override
	public void shutdownInternal() {
		// TODO Auto-generated method stub	
	}
	
//	private Config generateConfig(){
//		Config config = new Config();
//		RdfStoreConnection dataLayer = DataFactory.INSTANCE.createRdfStoreConnection();
//		if(dataLayer.createRdfGraph(this.getIdentifier()).getStatements().hasNext()){
//			logger.debug("Retrieving the queries from the Data Layer");
//			String query =
//					"PREFIX uc: <http://larkc.cefriel.it/ontologies/urbancomputing#> " +
//					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//					"SELECT ?apiKey ?xslt " +
//					"FROM <"+this.getIdentifier()+"> " +
//					"WHERE{ " +
//					"<http://api.evdb.com/> uc:hasApiKey ?apiKey; "+
//					"uc:hasXsltTransformation ?xslt. " +
//					"}";
//			logger.debug("Submitting query: {}", query);
//
//			CloseableIterator<Binding> it = dataLayer.executeSelect(new SPARQLQueryImpl(query)).iterator();
//			logger.debug("Query executed");
//			if(it.hasNext()){
//				List<Value> values = it.next().getValues();
//				logger.debug("The retrieved API key is: {}", values.get(0).stringValue());
//				config.setEvdbKey(values.get(0).stringValue());
//				logger.debug("and the XSLT file is located at: {}", values.get(1).stringValue());
//				config.setEvdbXslt(values.get(1).stringValue());
//			}
//			else{
//				logger.debug("No patterns has been found in the DL for EVDB");
//			}
//		}
//		dataLayer.close();
//		return config;
//	}
	
	private Collection<SetOfStatements> generateEvdbDocs(City city){//, Config config){
		String temporalRange;

		if(city.getStartTime()!=null && city.getEndTime()!=null){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
			temporalRange=formatter.format(city.getStartTime())+"-"+formatter.format(city.getEndTime());
		}
		else{
			logger.error("The data has not been defined, today will be set");
			Date todayDate = new Date();
			SimpleDateFormat formatter;

			formatter = new SimpleDateFormat("yyyyMMdd");


			String today=formatter.format(todayDate);
			temporalRange=today+"00-"+today+"23";
		}

		String url;
		if(!Double.isNaN(city.getLat())&&!Double.isNaN(city.getLng()))
			url="http://api.evdb.com/rest/events/search?app_key="+eventfulKey+"&location="+city.getLat()+","+city.getLng()+"&within="+city.getRange()+"&units=km&date="+temporalRange;
		else
			url="http://api.evdb.com/rest/events/search?app_key="+eventfulKey+"&location="+city.getName()+"&date="+temporalRange;
		return processEventfulResult(url, temporalRange);//, config);
	}
	
	private Collection<SetOfStatements> processEventfulResult(String location, String temporalRange){//, Config config){
		Collection<SetOfStatements> ret = new ArrayList<SetOfStatements>();
		try {
			logger.debug("Opening the connection with {}", location);
			URL url = new URL(location);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			// retrieve the content
			InputStream doc = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(doc);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			int ch;
			while((ch=br.read())>-1){
				sb.append((char)ch);
			}
			String s = sb.toString();

			XPathFactory xpf = javax.xml.xpath.XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			int i=1;
			String ns;
			do{
				javax.xml.xpath.XPathExpression xpe = xp.compile("search/events/event["+i+"]/@id");
				ns = xpe.evaluate(new org.xml.sax.InputSource(new ByteArrayInputStream(s.getBytes("UTF-8"))));
				if(ns!=""){
					ArrayList<Statement> statements = new ArrayList<Statement>();

					URI graphName = new URIImpl("http://eventful.com/milan/events?t="+temporalRange);
					URI eventName = new URIImpl("http://api.evdb.com/rest/events/get?app_key="+eventfulKey+"&id="+ns);

					statements.add(new StatementImpl(eventName, new URIImpl("http://larkc.cefriel.it/ontologies/plugin#hasXslt"), xsltTransf));
					statements.add(new StatementImpl(eventName, new URIImpl("http://larkc.cefriel.it/ontologies/plugin#isPartOfGraph"), graphName));

					ret.add(new SetOfStatementsImpl(statements));
				}
				i++;
			}while(ns!="" && i<100);
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IO error", e);
			if(e.getMessage().contains("HTTP response code: 400"))
				throw new RuntimeErrorException(new Error("EVDB API key is not correct!!"));
		}
		return ret;
	}
	
	
	private class City{
		private Date startTime;
		private Date endTime;
		private double lat=Double.NaN;
		private double lng=Double.NaN;
		private String name;
		private String country;
		private double range = DEFAULT_RANGE;

		City(SPARQLQuery theQuery){
			String strQuery = theQuery.toString();
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
							startTime = formatter.parse(f);
						} catch (ParseException e) {
							logger.error("The date format is not valid");
							e.printStackTrace();
						}

						f = strQuery.substring(to);
						f=f.substring(f.indexOf("\"")+1, f.lastIndexOf("\"")-1);

						try {
							endTime = formatter.parse(f);
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
							startTime = formatter.parse(f);
							endTime=startTime;
						} catch (ParseException e) {
							logger.error("The date format is not valid");
							e.printStackTrace();
						}
					}
			}

			if(theQuery instanceof SPARQLQueryImpl)
			{
				StatementPatternCollector spc = new StatementPatternCollector();
				TupleExpr te = ((SPARQLQueryImpl) theQuery).getParsedQuery().getTupleExpr();
				te.visit(spc);

				for (StatementPattern sp : spc.getStatementPatterns()){
					URI p = (URI) sp.getPredicateVar().getValue();
					if(p!=null)
						if (p.equals(localityUri)) {
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
			}
		}

		public Date getStartTime() {
			return startTime;
		}
		public Date getEndTime() {
			return endTime;
		}
		public double getLat() {
			return lat;
		}
		public double getLng() {
			return lng;
		}
		public String getName() {
			return name;
		}
		@SuppressWarnings("unused")
		public String getCountry() {
			return country;
		}
		public double getRange() {
			return range;
		}
	}
}
