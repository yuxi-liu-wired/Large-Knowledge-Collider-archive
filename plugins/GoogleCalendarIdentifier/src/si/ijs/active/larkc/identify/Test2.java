package si.ijs.active.larkc.identify;

import java.io.IOException;
import java.util.List;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import si.ijs.google.calendar.GoogleCalendarRetriever;

public class Test2 {
	public static void main(String[] args) {
		GoogleCalendarRetriever gc = null;
		try {
			gc = new GoogleCalendarRetriever("Luka@Cyceurope.com", "19iMaPe1");
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			List<CalendarEventEntry> events = gc.getAllEvents(); for (int i = 0; i < events.size(); i++) {
			     CalendarEventEntry entry = events.get(i);
			     
			     System.out.println("ID:"+entry.getId() +" plaintext " + entry.getTitle().getPlainText());
			   }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
