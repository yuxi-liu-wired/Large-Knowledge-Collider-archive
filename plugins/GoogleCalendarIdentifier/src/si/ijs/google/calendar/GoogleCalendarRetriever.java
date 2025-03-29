package si.ijs.google.calendar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


/**
 * Main class for retrieving Google calendar data
 * @author Luka Bradesko
 */
public class GoogleCalendarRetriever {
	
	// The base URL for a user's calendar metafeed (needs a username appended).
	private static final String METAFEED_URL_BASE = 
										"http://www.google.com/calendar/feeds/";
	// The string to add to the user's metafeedUrl to access the event feed for
	// their primary calendar.
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";
	 
	private String username;
	private CalendarService gService;
	private URL eventFeedUrl = null;
	
	
	
	/**
	 * Constructs the class and tries to conects to Google Calendar
	 * @param _username
	 * @param _password
	 * @throws AuthenticationException
	 */
	public GoogleCalendarRetriever(String _username, String _password) 
												throws AuthenticationException {
		gService = new CalendarService("GC Retriever");
		gService.setUserCredentials(_username, _password);
		
		this.username = _username;
		
		try {
			eventFeedUrl = new URL(METAFEED_URL_BASE + this.username
			         								+ EVENT_FEED_URL_SUFFIX);
		} catch (MalformedURLException e) {
			// if Authentication goes through, thne this will never happen
			//except when Google changes the API
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Retrieves all events from default Google Calendar for this user
	 * @return collection of events
	 * @throws ServiceException 
	 * @throws IOException 
	 */
	public List<CalendarEventEntry> getAllEvents() throws IOException, ServiceException{
		CalendarEventFeed resultFeed = gService.getFeed(eventFeedUrl, CalendarEventFeed.class);
		return resultFeed.getEntries();
	}
	
	
	/**
	 * Returns the username of this calendar retriever
	 * @return username of this retriever
	 */
	public String getUser(){
		return username;
	}

}
