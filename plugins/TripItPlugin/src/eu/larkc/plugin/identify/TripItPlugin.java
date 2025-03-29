package eu.larkc.plugin.identify;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.qos.logback.classic.Logger;

import com.tripit.api.Action;
import com.tripit.api.Client;
import com.tripit.api.Response;
import com.tripit.api.Type;
import com.tripit.auth.WebAuthCredential;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;

import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;

/**
 * This plugin collects data about trips from www.tripit.com and creates a simple RDF.
 * parameters: tripit username and password.
 *   
 * @author Janez Starc
 *
 */
public class TripItPlugin extends Plugin {

	
	//CONSTANTS
	public static String Tripit_PREFIX = "http://example.com/TripitPlugin/tripit#";
	public static String TIME_PREFIX = "http://www.w3.org/2006/time#";
	public static String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	private ArrayList<String> usernames = new ArrayList<String>();
	private ArrayList<String> users = new ArrayList<String>();
	private ArrayList<String> passwords = new ArrayList<String>();
	
	
	public TripItPlugin(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		CloseableIterator<Statement> parameters = workflowDescription
				.getStatements();

		while (parameters.hasNext()) {
			Statement stmt = parameters.next();
			if (stmt.getPredicate().equals(
					new URIImpl("http://larkc.eu/schema#loginInfo"))) {
				String loginInfo = stmt.getObject().stringValue();
				users.add(loginInfo.split(",")[0]);
				usernames.add(loginInfo.split(",")[1]);
				passwords.add(loginInfo.split(",")[2]);
			}

		}

		logger.info("Plug-in initialized: " + usernames + ", " + passwords);
	}

	public SetOfStatements invokeInternal(SetOfStatements input){
		System.out.println("TRIPIT PLUGIN:::: INVOKIKEDD" );
		// the plug-in code
		
		
		URI graph = new URIImpl (Tripit_PREFIX+"graph"); 
		
		URI cycSubEvents = new URIImpl(RDF.CYC_PREFIX+"subEvents");
		URI rdfSubClassOf = new URIImpl(RDF_PREFIX+"subClassOf");
		URI cycRegularAirplaneFlight = new URIImpl(RDF.CYC_PREFIX+"regularAirplaneFlight");
		URI cycFromLocation = new URIImpl(RDF.CYC_PREFIX+"fromLocation");
		URI cycToLocation = new URIImpl(RDF.CYC_PREFIX+"toLocation");
		URI cycAirportPhysical = new URIImpl(RDF.CYC_PREFIX+"Airport-Physical");
		URI cycAirportHasIATACode = new URIImpl(RDF.CYC_PREFIX+"airportHasIATACode");
		URI prefixCityName = new URIImpl(Tripit_PREFIX+"cityName");
		URI cycHasMarketingAirline = new URIImpl(RDF.CYC_PREFIX+"hasMarketingAirline");
		URI cycAirlineIATACarrierCode =  new URIImpl(RDF.CYC_PREFIX+"airlineIATACarrierCode");
		URI cycMarketingAirlineFlightNumber = new URIImpl(RDF.CYC_PREFIX+"marketingAirlineFlightNumber");
		URI cycAirplaneTypeUsed = new URIImpl(RDF.CYC_PREFIX+"airplaneTypeUsed");
		URI cycAircraftTypeByBrand = new URIImpl(RDF.CYC_PREFIX+"AircraftTypeByBrand");
		URI prefixDistance = new URIImpl(Tripit_PREFIX+"distance");
		URI prefixDuration = new URIImpl(Tripit_PREFIX+"duration");
		URI timeHasBeginning = new URIImpl(TIME_PREFIX+"hasBeginning");
		URI rdfDataType = new URIImpl(RDF_PREFIX+"datatype");
		URI timeInstant = new URIImpl(TIME_PREFIX+"Instant");
		URI rdfDateTime = new URIImpl(RDF_PREFIX+"&xsd;dateTime");
		URI timeHasEnd= new URIImpl(TIME_PREFIX+"hasEnd");
		URI cycRecipientOfService = new URIImpl(RDF.CYC_PREFIX+"recipientOfService");
		
		URI prefixEventOccursAt = new URIImpl(Tripit_PREFIX+"eventOccursAt");
		URI prefixAddress= new URIImpl(Tripit_PREFIX+"address");
		
		int lodgingCounter=0;
	
		Document tripitXML;
		RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection ();
		
		//get tripit username and password from parameters
		/*SetOfStatements parameters = this.getPluginParameters();
		CloseableIterator<Statement> it = parameters.getStatements();
		String username="";
		String password="";
		while(it.hasNext())
		{
			Statement st =it.next();
			if(st.getPredicate().equals(new URIImpl("http://credentials#username")))
				username=st.getObject().stringValue();
			else if(st.getPredicate().equals(new URIImpl("http://credentials#password")))
				password=st.getObject().stringValue();
		}*/
		
		for(int usrId=0; usrId<users.size(); usrId++)
		{
			String username=usernames.get(usrId);
			String password = passwords.get(usrId);
			String user = users.get(usrId);

			try {
				tripitXML = getTripitXML(username,password,"list","trip",new HashMap<String,String>());
				if(tripitXML.getElementsByTagName("Error").getLength()>0)
					logger.warn(tripitXML.getElementsByTagName("description").item(0).getTextContent());
				else{
					NodeList tripids = tripitXML.getElementsByTagName("id");
					// for each trip...
					for(int tripindex=0; tripindex<tripids.getLength(); tripindex++)
					{
						Node n = tripids.item(tripindex);
						HashMap<String,String> requestParameterMap = new HashMap<String,String>();
						requestParameterMap.put("id",  n.getTextContent());
						requestParameterMap.put("include_objects","true");
						//get trip data
						tripitXML = getTripitXML(username,password,"get","trip",requestParameterMap);


						Element tripElement = (Element) tripitXML.getElementsByTagName("Trip").item(0);
						Literal tripName = new LiteralImpl(tripElement.getElementsByTagName("display_name").item(0).getTextContent());
						BNode trip = new BNodeImpl(removeSpaces(tripName.toString().replaceAll("\"", ""))+removeSpaces(user));
						con.addStatement(trip, RDFConstants.RDF_TYPE,RDF.URI_ROUNT_TRIP, graph);
						con.addStatement(trip, RDF.URI_EVENT_HASOWNER, new LiteralImpl(user),graph);
						con.addStatement(trip, RDF.URI_PREFERRED_NAME_STRING,tripName, graph);
						Literal tripStartDate = new LiteralImpl(tripElement.getElementsByTagName("start_date").item(0).getTextContent());
						con.addStatement(trip, RDF.URI_EVENT_STARTS, tripStartDate,graph);
						Literal tripEndDate = new LiteralImpl(tripElement.getElementsByTagName("end_date").item(0).getTextContent());
						con.addStatement(trip, RDF.URI_EVENT_ENDS, tripEndDate,graph);
						Literal primaryLocation = new LiteralImpl(tripElement.getElementsByTagName("primary_location").item(0).getTextContent());
						con.addStatement(trip, RDF.URI_EVENT_HASLOCATION,primaryLocation,graph);

						// AIR OBJECTS
						NodeList airObjects = tripitXML.getElementsByTagName("AirObject");
						int k=0;
						for(int i=0; i<airObjects.getLength(); i++)
						{
							Element airTrip = (Element) airObjects.item(i);
							NodeList segments = airTrip.getElementsByTagName("Segment");
							NodeList travelers = airTrip.getElementsByTagName("Traveler");
							for(int j=0; j<segments.getLength(); j++)
							{	
								Element segment = (Element) segments.item(j);
								BNode segmentBlank = new BNodeImpl("flight"+ k++ );
								con.addStatement(trip,cycSubEvents,segmentBlank, graph);

								//instance of regular airplane flight
								BNode regularAirplaneFlight = new BNodeImpl("raf"+k);
								con.addStatement(segmentBlank,RDFConstants.RDF_TYPE,regularAirplaneFlight, graph);

								// REGULAR AIRPLANE FLIGHT 
								con.addStatement(regularAirplaneFlight,rdfSubClassOf,cycRegularAirplaneFlight, graph);

								//fromLocation
								BNode startAirport = new BNodeImpl("startAirport"+k);
								con.addStatement(regularAirplaneFlight,cycFromLocation,startAirport, graph);
								con.addStatement(startAirport,RDFConstants.RDF_TYPE,cycAirportPhysical, graph);
								Literal startIataCode = new LiteralImpl(segment.getElementsByTagName("start_airport_code").item(0).getTextContent());
								con.addStatement(startAirport,cycAirportHasIATACode,startIataCode, graph);
								Literal startCityName = new LiteralImpl(segment.getElementsByTagName("start_city_name").item(0).getTextContent());
								con.addStatement(startAirport,prefixCityName,startCityName, graph);
								//toLocation
								BNode endAirport = new BNodeImpl("endAirport"+k);
								con.addStatement(regularAirplaneFlight,cycToLocation,endAirport, graph);
								con.addStatement(endAirport,RDFConstants.RDF_TYPE,cycAirportPhysical, graph);
								Literal endIataCode = new LiteralImpl(segment.getElementsByTagName("end_airport_code").item(0).getTextContent());
								con.addStatement(endAirport,cycAirportHasIATACode,endIataCode, graph);
								Literal endCityName = new LiteralImpl(segment.getElementsByTagName("end_city_name").item(0).getTextContent());
								con.addStatement(endAirport,prefixCityName,endCityName, graph);

								//marketingAirline + flightNO
								BNode marketingAirline = new BNodeImpl("marketingAirline"+k);
								con.addStatement(regularAirplaneFlight,cycHasMarketingAirline,marketingAirline, graph);
								Literal marketingAirlineCode = new LiteralImpl(segment.getElementsByTagName("marketing_airline_code").item(0).getTextContent());
								con.addStatement(marketingAirline,cycAirlineIATACarrierCode,marketingAirlineCode, graph);
								Literal marketingAirlineL = new LiteralImpl(segment.getElementsByTagName("marketing_airline").item(0).getTextContent());
								con.addStatement(marketingAirline,RDF.URI_PREFERRED_NAME_STRING,marketingAirlineL, graph);
								Literal marketingAirlineFlightNO = new LiteralImpl(segment.getElementsByTagName("marketing_flight_number").item(0).getTextContent());
								con.addStatement(regularAirplaneFlight,cycMarketingAirlineFlightNumber,marketingAirlineFlightNO, graph);

								//airplaneTypeUsed
								BNode airplaneTypeUsed = new BNodeImpl("airplaneTypeUsed"+k);
								con.addStatement(regularAirplaneFlight,cycAirplaneTypeUsed,airplaneTypeUsed, graph);
								con.addStatement(airplaneTypeUsed, RDFConstants.RDF_TYPE,cycAircraftTypeByBrand, graph);
								Literal airplaneTypeUsedL = new LiteralImpl(segment.getElementsByTagName("aircraft_display_name").item(0).getTextContent());
								con.addStatement(airplaneTypeUsed,RDF.URI_PREFERRED_NAME_STRING,airplaneTypeUsedL, graph);

								//duration + distance
								Literal distance = new LiteralImpl(segment.getElementsByTagName("distance").item(0).getTextContent());
								con.addStatement(regularAirplaneFlight,prefixDistance , distance, graph);
								Literal duration = new LiteralImpl(segment.getElementsByTagName("duration").item(0).getTextContent());
								con.addStatement(regularAirplaneFlight,prefixDuration, duration, graph);
								// END REGULAR AIRLINE FLIGHT

								//startTime
								BNode startTime = new BNodeImpl("startTime"+k);
								con.addStatement(segmentBlank,timeHasBeginning,startTime, graph);
								Element startTimeE = (Element)segment.getElementsByTagName("StartDateTime").item(0);
								Literal startTimeL = new LiteralImpl(startTimeE.getElementsByTagName("date").item(0).getTextContent() +
										"T"+startTimeE.getElementsByTagName("time").item(0).getTextContent() +
										startTimeE.getElementsByTagName("utc_offset").item(0).getTextContent());

								con.addStatement(startTime,rdfDataType,rdfDateTime, graph);
								con.addStatement(startTime,timeInstant,startTimeL, graph);

								//endTime
								BNode endTime = new BNodeImpl("endTime"+k);
								con.addStatement(segmentBlank,timeHasEnd,endTime, graph);
								Element endTimeE = (Element)segment.getElementsByTagName("EndDateTime").item(0);
								Literal endTimeL = new LiteralImpl(endTimeE.getElementsByTagName("date").item(0).getTextContent() +
										"T"+endTimeE.getElementsByTagName("time").item(0).getTextContent() +
										endTimeE.getElementsByTagName("utc_offset").item(0).getTextContent());
								con.addStatement(endTime,rdfDataType,rdfDateTime, graph);
								con.addStatement(endTime,timeInstant,endTimeL, graph);

								//travelers

								for(int l=0; l<travelers.getLength(); l++)
								{
									Element traveler = (Element) travelers.item(i);
									NodeList lastName = traveler.getElementsByTagName("last_name");
									Literal travelerL = new LiteralImpl(traveler.getElementsByTagName("first_name").item(0).getTextContent()+" "+
											((lastName!=null)? lastName.item(0).getTextContent() : ""));
									con.addStatement(segmentBlank,cycRecipientOfService, travelerL, graph);
								}
							}
						}

						// LODGING OBJECTS
						NodeList lodgingObjects = tripitXML.getElementsByTagName("LodgingObject");
						for(int i=0; i<lodgingObjects.getLength(); i++)
						{
							
							Element lodgingObject = (Element) lodgingObjects.item(i);
							BNode lodgingObjectB = new BNodeImpl("lodging"+lodgingCounter);
							
							con.addStatement(lodgingObjectB, RDFConstants.RDF_TYPE,RDF.URI_LODGING, graph);

							Literal lodgingName = new LiteralImpl(lodgingObject.getElementsByTagName("display_name").item(0).getTextContent());
							//con.addStatement(lodgingObjectB,RDF.URI_PREFERRED_NAME_STRING ,lodgingName, graph);

							//Guests
							NodeList guests = lodgingObject.getElementsByTagName("Guest");
							for(int l=0; l<guests.getLength(); l++)
							{
								Element guest = (Element) guests.item(i);
								NodeList lastName = guest.getElementsByTagName("last_name");
								Literal guestL = new LiteralImpl(guest.getElementsByTagName("first_name").item(0).getTextContent()+" "+
										((lastName!=null) ? lastName.item(0).getTextContent() : ""));
								con.addStatement(lodgingObjectB,cycRecipientOfService, guestL, graph);
							}

							//start time
							BNode startTime = new BNodeImpl("startTime"+lodgingCounter);
							con.addStatement(lodgingObjectB,timeHasBeginning,startTime, graph);
							Element startTimeE = (Element)lodgingObject.getElementsByTagName("StartDateTime").item(0);

							String date = startTimeE.getElementsByTagName("date").item(0).getTextContent();
							String time = (startTimeE.getElementsByTagName("time").item(0) != null) ? startTimeE.getElementsByTagName("time").item(0).getTextContent() : "";
							String offset = (startTimeE.getElementsByTagName("utc_offset").item(0) != null) ? startTimeE.getElementsByTagName("utc_offset").item(0).getTextContent() : "";
							Literal startTimeL = new LiteralImpl(date + "T" + time + offset);

							con.addStatement(startTime,rdfDataType,rdfDateTime, graph);
							con.addStatement(startTime,timeInstant,startTimeL, graph);

							//endTime
							BNode endTime = new BNodeImpl("endTime"+lodgingCounter);
							con.addStatement(lodgingObjectB,timeHasEnd,endTime, graph);
							Element endTimeE = (Element)lodgingObject.getElementsByTagName("EndDateTime").item(0);

							date = endTimeE.getElementsByTagName("date").item(0).getTextContent();
							time = (endTimeE.getElementsByTagName("time").item(0) != null) ? endTimeE.getElementsByTagName("time").item(0).getTextContent() : "";
							offset = (endTimeE.getElementsByTagName("utc_offset").item(0) != null) ? endTimeE.getElementsByTagName("utc_offset").item(0).getTextContent() : "";

							Literal endTimeL = new LiteralImpl(date + "T" + time + offset);
							con.addStatement(endTime,rdfDataType,rdfDateTime, graph);
							con.addStatement(endTime,timeInstant,endTimeL, graph);

							//location
							BNode lodgingLocation = new BNodeImpl("lodgingLocation"+lodgingCounter);
							con.addStatement(lodgingObjectB,prefixEventOccursAt,lodgingLocation, graph);
							Literal lodgingNameL = new LiteralImpl(lodgingObject.getElementsByTagName("supplier_name").item(0).getTextContent());
							con.addStatement(lodgingObjectB,RDF.URI_PREFERRED_NAME_STRING,lodgingNameL, graph);

							String address = (lodgingObject.getElementsByTagName("Address").item(0) != null) ? lodgingObject.getElementsByTagName("Address").item(0).getFirstChild().getTextContent() : "";
							if (address != "")
							{	Literal lodgingAddress = new LiteralImpl(address);
							con.addStatement(lodgingObjectB, prefixAddress ,lodgingAddress, graph);
							}
							con.addStatement(trip, RDF.URI_PROVIDING_LODGING ,lodgingObjectB, graph);
							lodgingCounter++;
						}
					}
				}
			}catch (Exception e) {
				e.printStackTrace();

			}
		}
		// execute Query
		
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
		if (query != null){
		VariableBinding binding = con.executeSelect(query);
		SetOfStatements statements3 = binding.toRDF(new SetOfStatementsImpl());
		
		return statements3;}
		
		SetOfStatements sos = con.createRdfGraph(graph);
		return con.createRdfGraph(graph);
	}
	public Document getTripitXML(String username,String password,String action,String typeStr,Map<String, String> requestParameterMap) throws Exception
	{
		WebAuthCredential cred = new WebAuthCredential(username,password);
		String url = Client.DEFAULT_API_URI_PREFIX;
		Action a = Action.get(action);
		Type type = Type.get(a,typeStr);		
		Client client = new Client(cred, url);
		Response response = a.execute(client, type, requestParameterMap);
		String tripIdXML = response.getContent();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(tripIdXML.getBytes("UTF-8")));
		doc.getDocumentElement().normalize();
		return doc;
	}

	public void shutdown() {
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}

	private String removeSpaces(String s){
		return s.replaceAll(" ", "");
	}
	
	@Override	
	protected void cacheInsert(SetOfStatements input, SetOfStatements output) {
		
	}
}



