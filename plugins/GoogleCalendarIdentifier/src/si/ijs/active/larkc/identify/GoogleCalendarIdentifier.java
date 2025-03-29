package si.ijs.active.larkc.identify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import si.ijs.google.calendar.GoogleCalendarRetriever;

import com.google.gdata.client.GoogleService.InvalidCredentialsException;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.ServiceException;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;

/**
 * This is an LarKC identifier plug-in, which can connect to Google calendar and
 * 
 * @author LarKC plug-in Wizard
 * 
 */
public class GoogleCalendarIdentifier extends Plugin {
	Logger logger = Logger.getLogger("si.ijs.active.larkc.identify.GoogleCalendarIdentifier");

	public final static URI CYC_APPOINTMENT = new URIImpl("http://sw.opencyc.org/concept/Mx4rvVieKpwpEbGdrcN5Y29ycA");
	
	private ArrayList<String> usernames = new ArrayList<String>();
	private ArrayList<String> users = new ArrayList<String>();
	private ArrayList<String> passwords = new ArrayList<String>();

	public GoogleCalendarIdentifier(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		logger.info(this.getClass().getName() + "invoked!");
		ArrayList<Statement> statements=new ArrayList<Statement>();
		try {
			for (int iLogins = 0; iLogins < usernames.size(); iLogins++) {
				GoogleCalendarRetriever gc = new GoogleCalendarRetriever(usernames.get(iLogins), passwords.get(iLogins));
				statements.addAll(convertToRDF(gc,users.get(iLogins)));
				
				
			}// for all users
		} catch (InvalidCredentialsException e) {
			System.out.println("INVALID CREDENTIALS");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		//add statements to internal store (if nothing else, it removes direct duplicates)
		/*RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection ();
		URI graph = new URIImpl (RDF.CYC_PREFIX+"GCgraph");
		for (Statement statement : statements) {
			if (!con.search(statement.getSubject(), statement.getPredicate(), statement.getObject(), graph, null).hasNext());
				con.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(),graph);//trip, RDFConstants.RDF_TYPE,cycRoundTrip, graph);
		}*/

		

		return new SetOfStatementsImpl(statements);
		//return con.createRdfGraph(graph);
	}

	@Override
	public void shutdown() {
		System.out.println("SHUT DOWN");
		// TODO Auto-generated method stub

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

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Reads the events and converts the data into triples
	 * @param gc
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	private ArrayList<Statement> convertToRDF(GoogleCalendarRetriever gc, String sUser) throws IOException, ServiceException{
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		
		List<CalendarEventEntry> events = gc.getAllEvents();
		for (int i = 0; i < events.size(); i++) {
			CalendarEventEntry entry = events.get(i);
			BNode bEvent = new BNodeImpl(removeSpaces(entry.getTitle().getPlainText())+removeSpaces(sUser));
			
			Statement st = new StatementImpl(bEvent, RDFConstants.RDF_TYPE, RDF.URI_EVENT);
			Statement st2 = new StatementImpl(bEvent, RDF.URI_PREFERRED_NAME_STRING, new LiteralImpl(entry.getTitle().getPlainText()));
			Statement st4 = new StatementImpl(bEvent, RDF.URI_EVENT_HASOWNER, new LiteralImpl(entry.getAuthors().get(0).getName()));
			statements.add(st);statements.add(st2);statements.add(st4);
			
			List<When> when = entry.getTimes();
			for (When when2 : when) {
				statements.add(new StatementImpl(bEvent, RDF.URI_EVENT_STARTS, new LiteralImpl(when2.getStartTime().toString())));
				statements.add(new StatementImpl(bEvent, RDF.URI_EVENT_ENDS, new LiteralImpl(when2.getEndTime().toString())));
				
				
			}
			
			List<Where> locations = entry.getLocations();
			if(!locations.isEmpty()){
				Where loc = locations.get(0);
				
				String sLoc = loc.getValueString();
				if (sLoc != null && !sLoc.equals(""))
				{Statement st3 = new StatementImpl(bEvent, RDF.URI_EVENT_HASLOCATION, new LiteralImpl(sLoc));
				statements.add(st3);}
			}

			System.out.println("ID:" + entry.getId() + " plaintext "
					+ entry.getTitle().getPlainText());
		}
		
		//return new RdfGraphInMemory(new URIImpl (RDF.GC_PREFIX+"graph"), statements);
		return statements;
		
	}

	private String removeSpaces(String s){
		return s.replaceAll(" ", "");
	}
	
	@Override	
	protected void cacheInsert(SetOfStatements input, SetOfStatements output) {
		
	}
}