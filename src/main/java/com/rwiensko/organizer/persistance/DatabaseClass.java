package com.rwiensko.organizer.persistance;

import com.rwiensko.organizer.entity.Event;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseClass {

    private static final String DRIVER = "org.sqlite.JDBC";
    private Connection connection;
    private String databaseName;
    public DatabaseClass(String fileName) {

        try {
            Class.forName(DatabaseClass.DRIVER);
        } catch(ClassNotFoundException e) {
            System.err.println("Driver JDBC couldn't be loaded");
            e.printStackTrace();
        }
        try {
            this.databaseName = "jdbc:sqlite:" + fileName + ".db";
            connection = DriverManager.getConnection(this.databaseName);

        } catch(SQLException e) {
            System.err.println("Error server connection");
            e.printStackTrace();
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        createTables();
    }

    private void createTables()  {

        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            String sqlString = "CREATE TABLE IF NOT EXISTS Events(id_Event INTEGER PRIMARY KEY AUTOINCREMENT, date_of_event date, description varchar(255))";
            statement.execute(sqlString);
        } catch(SQLException e) {
            System.err.println("Failed to create MyEvent");
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private ZonedDateTime fromSQLDateToZonedDateTime(Date date){
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    private Date fromZonedDateTimeToSQLDate(ZonedDateTime zonedDateTime){
        long millis = zonedDateTime.toInstant().toEpochMilli();
        return new Date(millis);
    }
    private long fromZonedDateTimeToLong(ZonedDateTime zonedDateTime){
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public void insertEvent(Event event)  {
        try {
            connection = DriverManager.getConnection(databaseName);
            Date sqlDate = fromZonedDateTimeToSQLDate(event.getZonedDateTime());
            String description = event.getDescription();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Events VALUES(NULL, ?,?)", RETURN_GENERATED_KEYS);
            preparedStatement.setDate(1, sqlDate);
            preparedStatement.setString(2, description);
            preparedStatement.execute();
            try{
                ResultSet generatedKey = preparedStatement.getGeneratedKeys();
                if(generatedKey.next()){
                    event.setId(generatedKey.getInt(1));
                }
            }catch(SQLException e){
                System.err.println("Failed to find id");
                e.printStackTrace();
            }

        } catch(SQLException e) {
            System.err.println("Failed to add myEvent");
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Event> selectAllEvents()  {
        ArrayList<Event> arrayOfMyEvent;
        arrayOfMyEvent = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM Events");
            while (result.next()) {
                Event event = new Event();
                Date date = result.getDate("date_of_event");
                event.setZonedDateTime(fromSQLDateToZonedDateTime(date));
                event.setDescription(result.getString("description"));
                event.setId(result.getInt("id_Event"));
                arrayOfMyEvent.add(event);
            }
        } catch(SQLException e) {
            System.err.println("Failed to select events");
            e.printStackTrace();
            return null;
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return arrayOfMyEvent;
    }
    
    public ArrayList<Event> selectEventsFromOneDay(ZonedDateTime zonedDateTime)  {
        ArrayList<Event> arrayOfMyEvent;
        arrayOfMyEvent = new ArrayList<>();
        long selectedDay = fromZonedDateTimeToLong(zonedDateTime);
        long endOfDay = selectedDay + 86400000;
        try {
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM Events WHERE date_of_event >=" + selectedDay + " AND date_of_event <" + endOfDay);
            while (result.next()) {
                Event event = new Event();
                Date date = result.getDate("date_of_event");
                event.setZonedDateTime(fromSQLDateToZonedDateTime(date));
                event.setDescription(result.getString("description"));
                event.setId(result.getInt("id_Event"));
                arrayOfMyEvent.add(event);
            }
        } catch(SQLException e) {
            System.err.println("Failed to select events");
            e.printStackTrace();
            return null;
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return arrayOfMyEvent;
    }

    public void deleteAllEvents()  {

        try {
            connection = DriverManager.getConnection(databaseName);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE from Events");
            preparedStatement.execute();
        } catch(SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to delete records");
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Event selectEventById(int id)  {
        Event event = new Event();
        try{
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM Events WHERE id_Event = " + id);
            Date date = result.getDate("date_of_event");
            event.setZonedDateTime(fromSQLDateToZonedDateTime(date));
            event.setDescription(result.getString("description"));
            event.setId(result.getInt("id_Event"));

        }catch(SQLException e){
            e.printStackTrace();
            System.err.println("Failed to select record");
            return null;
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return event;
    }

    public void deleteEventById(int id)  {
        try {
            connection = DriverManager.getConnection(databaseName);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE from Events WHERE id_Event = " + id);
            preparedStatement.execute();
        }catch(SQLException e){
            e.printStackTrace();
            System.err.println("Failed to delete record");
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String updateEvent(int id, String description) {
        String changedDescription = " ";
        try{
            connection = DriverManager.getConnection(databaseName);
            Statement statement = connection.createStatement();
            statement.executeUpdate("UPDATE Events SET description = " + "'" + description + "'" +  " WHERE id_Event = " + id);
            ResultSet result = statement.executeQuery("SELECT * FROM Events WHERE id_Event = " + id);
            changedDescription = result.getString("description");
        }catch(SQLException e){
            e.printStackTrace();
            System.err.println("Failed to update record");
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return changedDescription;
    }

    public void closeDatabaseConnection() {
        try {
            connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
            System.err.println("Error while trying to close connection");
        }
    }
}