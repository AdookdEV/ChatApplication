package ka.adilet.chatapp.client.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Screen;
import ka.adilet.chatapp.client.utils.ScreenSwitcher;
import ka.adilet.chatapp.client.view.CustomAnimation;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signupLink;
    @FXML
    private Label loginErrorLabel;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField passwordTextField;

    private Network network = new Network("localhost", 1234);
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginErrorLabel.setVisible(false);
    }

    @FXML
    public void singIn(ActionEvent event) {
        CustomAnimation.buttonClick(loginButton);
        if (!validate()) return;
        CommunicationMessage cm = new CommunicationMessage(MessageType.LOGIN,
                String.format("{\"phone-number\": \"%s\", \"password\": \"%s\"}",
                        phoneTextField.getText(), passwordTextField.getText()));
        network.sendMessage(cm);
        Task<CommunicationMessage> task = new Task<>() {
            @Override
            protected CommunicationMessage call() throws Exception {
                CommunicationMessage message = network.listen();
                System.out.println(message.getBody());
                return message;
            }
        };
        task.setOnSucceeded((e) -> {
            System.out.println(task.getValue().getBody());
            try {
                JsonNode node = jsonMapper.readTree(task.getValue().getBody());
                if (node.get("status").asText().equals("OK")) {
                    ScreenSwitcher.switchTo(Screen.CHAT);
                }
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
        new Thread(task).start();
    }

    @FXML
    public void goToRegistration(ActionEvent event) throws Exception {
        ScreenSwitcher.switchTo(Screen.REGISTRATION);
    }

    private boolean validate() {
        if (phoneTextField.getText().length() == 0) {
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setText("Phone number field mustn't be empty");
            return false;
        }
        if (passwordTextField.getText().length() == 0) {
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setText("Password field mustn't be empty");
            return false;
        }
        loginErrorLabel.setVisible(false);
        loginErrorLabel.setText("");
        return true;
    }

}
