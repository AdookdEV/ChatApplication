module client {
    requires javafx.fxml;
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens ka.adilet.chatapp.client to javafx.fxml, javafx.controls;
    exports ka.adilet.chatapp.client;
    opens ka.adilet.chatapp.client.controller to javafx.fxml;
    exports ka.adilet.chatapp.client.controller;
    opens ka.adilet.chatapp.client.model to com.fasterxml.jackson.databind;
    exports ka.adilet.chatapp.client.model;
}