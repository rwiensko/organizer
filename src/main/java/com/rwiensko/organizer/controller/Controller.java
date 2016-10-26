package com.rwiensko.organizer.controller;

import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.rwiensko.organizer.persistance.Persister;
import com.rwiensko.organizer.entity.Event;


public class Controller {

    private final Persister databaseClass = new Persister("organiser");
    private final ObservableList<Event> items = FXCollections.observableArrayList();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
    private Date date;
    @FXML
    public TextField descriptionField;
    @FXML
    public TextField zonedDateTimeField;
    @FXML
    public ListView<Event> listView;
    @FXML
    public MenuItem deleteMenuItem;
    @FXML
    public ContextMenu contextMenuOfListView;
    @FXML
    public BorderPane borderPane;
    @FXML
    public TextArea workingArea;
    private final DatePicker datePicker = new DatePicker(LocalDate.now());



    @FXML
    public void addEvent() throws IOException {

        Text errorMessage = new Text("Wrong date format");
        Stage errorStage = new Stage();
        VBox errorVBox = new VBox(20);
        errorVBox.getChildren().add(errorMessage);
        errorVBox.setAlignment(Pos.CENTER);
        Scene errorScene = new Scene(errorVBox,200,100);
        errorStage.setScene(errorScene);

        String dateString = zonedDateTimeField.getText();
        String descriptionString = descriptionField.getText();
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            errorStage.show();
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        Event event = new Event(descriptionString, zonedDateTime);
        databaseClass.insertEvent(event);
        items.add(event);
        listView.setItems(items);
    }

    @FXML
    public void showAboutBox() {
        Stage popupStage = new Stage();
        VBox vBox = new VBox(20);
        Text text = new Text("Made by Rafa³ Wiensko");
        text.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(text);
        vBox.setAlignment(Pos.CENTER);
        Scene popupScene = new Scene(vBox, 300, 200);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    @FXML
    public void loadEvents(){
        listView.getItems().clear();
        ArrayList<Event> myEventArrayList = databaseClass.selectAllEvents();
        items.addAll(myEventArrayList.stream().collect(Collectors.toList()));
        listView.setItems(items);
    }

    @FXML
    public void deleteAllEventsFromListView() {
        databaseClass.deleteAllEvents();
        listView.getItems().clear();
    }

    @FXML
    public void deleteEventByIdFromListView(){
        databaseClass.deleteEventById(listView.getSelectionModel().getSelectedItem().getId());
        loadEvents();
    }

    @FXML
    public void quit() {
        databaseClass.closeDatabaseConnection();
        System.exit(0);
    }

    @FXML
    public void setCalendarToBorderPane(){
        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        borderPane.setCenter(datePickerSkin.getPopupContent());
    }

    private Event[] setListViewWithEvent(LocalDate localDate){
        Event [] tableOfEvents = new Event[24];
        for(int i =0;i<24;i++){
            Event event = new Event(" ",ZonedDateTime.of(localDate, LocalTime.of(i, 0), ZoneId.systemDefault()));
            tableOfEvents[i] = event;
        }
        return tableOfEvents;
    }

    @FXML
    public void selectEventsFromOneDay(){
        listView.getItems().clear();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(datePicker.getValue(), LocalTime.MIN, ZoneId.systemDefault());
        Event [] tableOfEvents = setListViewWithEvent(datePicker.getValue());
        ArrayList<Event> arrayOfEvents = databaseClass.selectEventsFromOneDay(zonedDateTime);
        for (Event arrayOfEvent : arrayOfEvents) {
            tableOfEvents[arrayOfEvent.getZonedDateTime().getHour()] = arrayOfEvent;
        }
        Collections.addAll(items, tableOfEvents);
        listView.setItems(items);
    }

    @FXML
    public void showUpdateStage() {
        final Stage popupStage = new Stage();
        GridPane gridPane = new GridPane();
        Button buttonUpdate = new Button("Update");
        final TextField updateField = new TextField("Description");
        gridPane.add(buttonUpdate,1,1);
        gridPane.add(updateField, 1, 2);
        Scene popupScene = new Scene(gridPane, 300, 200);
        popupStage.setScene(popupScene);
        popupStage.show();
        buttonUpdate.setOnAction((event) -> {
            String description = updateField.getText();
            updateEvent(description);
            popupStage.close();
        });
    }

    @FXML
    public void updateEvent(String description){
        Event event = listView.getSelectionModel().getSelectedItem();
        event.setDescription(description);
        listView.getSelectionModel().getSelectedItem().setDescription(databaseClass.updateEvent(event));
        selectEventsFromOneDay();
    }

    @FXML
    public void getDescriptionToTextField(){
        String description = listView.getSelectionModel().getSelectedItem().getDescription();
        workingArea.setText(description);
    }

    @FXML
    public void saveChanges(){
        String description = workingArea.getText();
        Event event = listView.getSelectionModel().getSelectedItem();
        if(event.getId()==0) {
            databaseClass.insertEvent(new Event(description,listView.getSelectionModel().getSelectedItem().getZonedDateTime()));
        } else {
            databaseClass.updateEvent(event);
        }
        selectEventsFromOneDay();
    }

    @FXML
    public void initialize(){
        setCalendarToBorderPane();
    }

}