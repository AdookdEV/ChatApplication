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
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.client.utils.Screen;
import ka.adilet.chatapp.client.utils.ScreenSwitcher;
import ka.adilet.chatapp.client.view.CustomAnimation;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationController implements Initializable {
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField surnameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signInLink;
    @FXML
    private Label regErrorLabel;

    private Network network = Context.getNetwork();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        regErrorLabel.setVisible(false);
    }

    @FXML
    public void signUp(ActionEvent event) throws IOException {
        CustomAnimation.buttonClick(loginButton);
        if (!validate()) return;
        if (!network.isConnected.getValue()) return;
        String userData = jsonMapper.writeValueAsString(new UserModel(
                null,
                nameTextField.getText(),
                surnameTextField.getText(),
                phoneTextField.getText(),
                passwordTextField.getText()
        ));
        network.sendMessage(new CommunicationMessage(MessageType.REGISTER, userData.toString()));
        Task<CommunicationMessage> task = new Task<>() {
            @Override
            protected CommunicationMessage call() throws Exception {
                CommunicationMessage message = network.listen();
                System.out.println(message.getBody());
                return message;
            }
        };
        task.setOnSucceeded((e) -> {
            JsonNode res=null;
            try {
                res = jsonMapper.readTree(task.getValue().getBody());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            if (res.get("result").asText().equals("OK")) {
                ScreenSwitcher.switchTo(Screen.LOGIN);
            } else {
                regErrorLabel.setVisible(true);
                regErrorLabel.setText(res.get("result").asText());
            }
        });
        new Thread(task).start();
    }

    @FXML
    public void goToLogin(ActionEvent event) throws Exception {
        ScreenSwitcher.switchTo(Screen.LOGIN);
    }

    private boolean validate() {
        if (phoneTextField.getText().length() == 0) {
            regErrorLabel.setVisible(true);
            regErrorLabel.setText("Phone number field mustn't be empty");
            return false;
        }
        if (nameTextField.getText().length() == 0) {
            regErrorLabel.setVisible(true);
            regErrorLabel.setText("Name field mustn't be empty");
            return false;
        }
        if (passwordTextField.getText().length() == 0) {
            regErrorLabel.setVisible(true);
            regErrorLabel.setText("Password field mustn't be empty");
            return false;
        }
        regErrorLabel.setVisible(false);
        regErrorLabel.setText("");
        return true;
    }
}
