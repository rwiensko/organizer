package com.rwiensko.organizer.persistance;

import com.rwiensko.organizer.entity.Event;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.TimeZone;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class Persister {

    private static final String DRIVER = "org.sqlite.JDBC";
    private Connection connection;
    private String databaseName;
    public Persister(String fileName) {

        try {
            Class.forName(Persister.DRIVER);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            this.databaseName = "jdbc:sqlite:" + fileName + ".db";
            connection = DriverManager.getConnection(this.databaseName);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
        createTables();
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() {

        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            String sqlString = "CREATE TABLE IF NOT EXISTS Events(id_Event INTEGER PRIMARY KEY AUTOINCREMENT, date_of_event date, description varchar(255))";
            statement.execute(sqlString);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }

    }

    private ZonedDateTime fromSQLDateToZonedDateTime(Date date) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    private Date fromZonedDateTimeToSQLDate(ZonedDateTime zonedDateTime) {
        long millis = zonedDateTime.toInstant().toEpochMilli();
        return new Date(millis);
    }
    private long fromZonedDateTimeToLong(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public void insertEvent(Event event) {
        try {
            connection = DriverManager.getConnection(databaseName);
            Date sqlDate = fromZonedDateTimeToSQLDate(event.getZonedDateTime());
            String description = event.getDescription();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Events VALUES(NULL, ?,?)", RETURN_GENERATED_KEYS);
            preparedStatement.setDate(1, sqlDate);
            preparedStatement.setString(2, description);
            preparedStatement.execute();

            ResultSet generatedKey = preparedStatement.getGeneratedKeys();
            if(generatedKey.next()) {
                event.setId(generatedKey.getInt(1));
            }

        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    public ArrayList<Event> selectAllEvents() {
        String sql = "SELECT * FROM Events";
        return selectEventsQuery(sql);
    }

    private ArrayList<Event> selectEventsQuery(String sql) {
        ArrayList<Event> events = new ArrayList<>();
        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                Event singleEvent = new Event();
                Date date = result.getDate("date_of_event");
                singleEvent.setZonedDateTime(fromSQLDateToZonedDateTime(date));
                singleEvent.setDescription(result.getString("description"));
                singleEvent.setId(result.getInt("id_Event"));
                events.add(singleEvent);
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
        return events;
    }
    
    public ArrayList<Event> selectEventsFromOneDay(ZonedDateTime zonedDateTime) {
        long selectedDayInMillis = fromZonedDateTimeToLong(zonedDateTime);
        long endOfDayInMillis = selectedDayInMillis + 86400000;
        String sql = "SELECT * FROM Events WHERE date_of_event >=" + selectedDayInMillis + " AND date_of_event <" + endOfDayInMillis;
        return selectEventsQuery(sql);
    }

    public void deleteAllEvents() {
        try {
            connection = DriverManager.getConnection(databaseName);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE from Events");
            preparedStatement.execute();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    public Event selectEventById(int id) {
        Event event = new Event();
        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM Events WHERE id_Event = " + id);
            Date date = result.getDate("date_of_event");
            event.setZonedDateTime(fromSQLDateToZonedDateTime(date));
            event.setDescription(result.getString("description"));
            event.setId(result.getInt("id_Event"));
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
        return event;
    }

    public void deleteEventById(int id) {
        try {
            connection = DriverManager.getConnection(databaseName);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE from Events WHERE id_Event = " + id);
            preparedStatement.execute();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    public String updateEvent(Event event) {
        String changedDescription = " ";
        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            statement.executeUpdate("UPDATE Events SET description = " + "'" + event.getDescription() + "'" +  " WHERE id_Event = " + event.getId());
            ResultSet result = statement.executeQuery("SELECT * FROM Events WHERE id_Event = " + event.getId());
            changedDescription = result.getString("description");
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
        return changedDescription;
    }

    public void closeDatabaseConnection() {
        closeConnection();
    }
}