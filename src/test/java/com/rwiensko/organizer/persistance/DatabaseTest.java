package com.rwiensko.organizer.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import com.rwiensko.organizer.entity.Event;
import org.junit.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;


public class DatabaseTest {
    private final Persister db = new Persister("target/test");
    private final ArrayList<Event> arrayList = new ArrayList<>();

    @Before
    public void beforeTest(){
        db.deleteAllEvents();
    }

    private ArrayList<Event> setTestEvents(LocalDate localDate){
        ArrayList<Event> array = new ArrayList<>();
        for(int i=0;i<24;i++){
            array.add(new Event("TEST",ZonedDateTime.of(localDate, LocalTime.of(i, 0), ZoneId.systemDefault())));
        }
        return array;
    }

    @Test
    public void testInsertAndDeleteEvent(){
        Event event = new Event("Test", ZonedDateTime.now());
        db.insertEvent(event);
        assertEquals(event, db.selectEventById(event.getId()));
        db.deleteEventById(event.getId());
        assertEquals(arrayList, db.selectAllEvents());
    }

    @Test
    public void testUpdateEvent() {
        Event event = new Event("Test", ZonedDateTime.now());
        db.insertEvent(event);
        event.setDescription("new description");
        db.updateEvent(event);
        assertEquals("new description", db.selectEventById(event.getId()).getDescription());
    }

    @Test
    public void testSelectEventsFromOneDay(){
        ArrayList<Event> arrayOfEvent = setTestEvents(LocalDate.now());
        arrayOfEvent.forEach(db::insertEvent);
        ArrayList<Event> arrayOfExpectedEvents = new ArrayList<>(arrayOfEvent);
        arrayOfEvent.add(new Event("redundant", ZonedDateTime.of(LocalDateTime.of(1990, 1, 1, 12, 30), ZoneId.systemDefault())));
        db.insertEvent(arrayOfEvent.get(arrayOfEvent.size()-1));
        System.out.println(db.selectAllEvents());
        assertEquals(arrayOfExpectedEvents, db.selectEventsFromOneDay(ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.systemDefault())));

    }
}
