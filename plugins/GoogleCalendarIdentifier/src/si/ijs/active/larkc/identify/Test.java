package si.ijs.active.larkc.identify;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Test {
	
	// The base URL for a user's calendar metafeed (needs a username appended).
	 private static final String METAFEED_URL_BASE = 
	     "http://www.google.com/calendar/feeds/";

	 // The string to add to the user's metafeedUrl to access the event feed for
	 // their primary calendar.
	 private static final String EVENT_FEED_URL_SUFFIX = "/private/full";
	 
	// The URL for the metafeed of the specified user.
	// (e.g. http://www.google.com/feeds/calendar/jdoe@gmail.com)
	private static URL metafeedUrl = null;
	 
	// The URL for the event feed of the specified user's primary calendar.
	// (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/private/full)
	private static URL eventFeedUrl = null;
	
	
	public static void main(String[] args) {
		String userName = "something";
		String userPassword = "something2";
		
		
		CalendarService myService = new CalendarService("LarKC identifier plug-in");
		
		
		try {
			metafeedUrl = new URL(METAFEED_URL_BASE + userName);
		   eventFeedUrl = new URL(METAFEED_URL_BASE + userName
		         + EVENT_FEED_URL_SUFFIX);
		} catch (MalformedURLException e) {
			// Bad URL
		   System.err.println("Uh oh - you've got an invalid URL.");
		   e.printStackTrace();
		   return;
		}
		
		
		try {
			myService.setUserCredentials(userName, userPassword);
			
				CalendarEventFeed resultFeed = myService.getFeed(eventFeedUrl, CalendarEventFeed.class);
				System.out.println("All events on your calendar:");
			   System.out.println();
			   List<CalendarEventEntry> events = resultFeed.getEntries();
			   for (int i = 0; i < events.size(); i++) {
			     CalendarEventEntry entry = events.get(i);
			     
			     System.out.println("ID:"+entry.getId() +" plaintext " + entry.getTitle().getPlainText());
			   }
			   System.out.println();
			
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
   	} catch (IOException e) {
   		// TODO Auto-generated catch block
   		e.printStackTrace();
   	} catch (ServiceException e) {
   		// TODO Auto-generated catch block
   		e.printStackTrace();
   	}
		
	}//main
}
