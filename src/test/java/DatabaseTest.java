import resources.DatabaseClass;
import resources.Event;
import org.junit.Test;
import junit.framework.Assert;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class DatabaseTest {
    private DatabaseClass db = new DatabaseClass("test");
    @Test
    public void testInsertAndDeleteEvent() {
        db.deleteAllEvents();
        ArrayList<Event> arrayList = new ArrayList<>();
        Event event = new Event("Hello", ZonedDateTime.now());
        db.insertEvent(event);
        Assert.assertEquals(event, db.selectEventById(event.getId()));
        db.deleteEventById(event.getId());
        Assert.assertEquals(arrayList, db.selectAllEvents());
    }
    @Test
    public void testUpdateEvent() {
        db.deleteAllEvents();
        Event event = new Event();
        db.insertEvent(event);
        db.updateEvent(event.getId(), "UPDATE");
        Assert.assertEquals("UPDATE", db.selectEventById(event.getId()).getDescription());
    }

    @Test
    public void testSelectEventsFromOneDay(){
        db.deleteAllEvents();
        ArrayList<Event> arrayOfEvents = new ArrayList<>();
        for(int i=0; i<10;i++){
            Event event = new Event("day",ZonedDateTime.of(LocalDate.now(), LocalTime.of(i,i), ZoneId.systemDefault()));
            arrayOfEvents.add(event);
        }
        ArrayList<Event> arrayOfWrongEvents = new ArrayList<>();
        for(int i=0; i<10;i++){
            Event event = new Event("wrong day", ZonedDateTime.of(LocalDate.of(2000+i,1+i,10+i),LocalTime.of(i,i),ZoneId.systemDefault()));
            arrayOfWrongEvents.add(event);
        }
        for(int i=0;i<arrayOfEvents.size();i++){
            db.insertEvent(arrayOfEvents.get(i));
            db.insertEvent(arrayOfWrongEvents.get(i));
        }
        Assert.assertEquals(arrayOfEvents,db.selectEventsFromOneDay(ZonedDateTime.of(LocalDate.now(),LocalTime.MIN,ZoneId.systemDefault())));
    }
}
