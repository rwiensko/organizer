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
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.rwiensko.organizer.persistance.DatabaseClass;
import com.rwiensko.organizer.entity.Event;


public class Controller {

    private DatabaseClass databaseClass = new DatabaseClass("organiser");
    private ObservableList<Event> items = FXCollections.observableArrayList();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
    Date date;
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
    public DatePicker datePicker = new DatePicker(LocalDate.now());



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
        Runtime.getRuntime().exit(0);
    }

    @FXML
    public void setBorderPane(){
        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        borderPane.setCenter(datePickerSkin.getPopupContent());

    }
    @FXML
    public void selectEventsFromOneDay(){
        listView.getItems().clear();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(datePicker.getValue(), LocalTime.MIN, ZoneId.systemDefault());
        Event [] tableOfEvents = new Event[24];
        ArrayList<Event> arrayOfEvents = new ArrayList<>();
        arrayOfEvents = databaseClass.selectEventsFromOneDay(zonedDateTime);
        for(int i =0;i<24;i++){
            Event event = new Event(" ",ZonedDateTime.of(datePicker.getValue(), LocalTime.of(i, 0), ZoneId.systemDefault()));
            tableOfEvents[i] = event;
        }
        for(int i=0;i<arrayOfEvents.size();i++){
            tableOfEvents[arrayOfEvents.get(i).getZonedDateTime().getHour()] = arrayOfEvents.get(i);
        }
        for(Event event : tableOfEvents){
            items.add(event);
        }
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
        int id = listView.getSelectionModel().getSelectedItem().getId();
        listView.getSelectionModel().getSelectedItem().setDescription(databaseClass.updateEvent(id, description));
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
        int id = listView.getSelectionModel().getSelectedItem().getId();
        if(id==0){
            databaseClass.insertEvent(new Event(description,listView.getSelectionModel().getSelectedItem().getZonedDateTime()));
        }else{
            databaseClass.updateEvent(id,description);
        }
        selectEventsFromOneDay();
    }

    @FXML
    public void initialize(){
        setBorderPane();
    }

}