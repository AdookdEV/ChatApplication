package ka.adilet.chatapp.client.controller;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.MessageModel;
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.view.ChatMessageView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChattingSectionController {
    private ChatModel chatModel;
    private HashMap<Integer, ArrayList<ChatMessageView>> cacheOfMessageViews;
    private UserModel userModel;

    @FXML
    private Button sendMessageButton;
    @FXML
    private Parent root;
    @FXML
    private HBox topBar;
    @FXML
    private Label chatNameLabel;
    @FXML
    private Label extraInfoLabel;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageTextField;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    public void initialize() {
        messageTextField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        sendMessageButton.setOnAction((ActionEvent event) -> {
            sendMessage();
        });
        messagesContainer.getChildren().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> change) {
                scrollPane.applyCss();
                scrollPane.layout();
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        });
    }

    public void switchChat(ChatModel chatModel) {
        this.chatModel = chatModel;
        scrollPane.lookup(".increment-button").setStyle("visibility: false");
        updateView();
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    private void sendMessage() {
        String messageContent = messageTextField.getText();
        messageTextField.clear();
        if (messageContent.length() == 0) return;
        MessageModel currMessage = new MessageModel(
                userModel.getId(),
                chatModel.getChatRoomId(),
                messageContent,
                "12:00"
        );
        currMessage.setContent(messageContent);
        chatModel.addMessage(currMessage);
        messagesContainer.getChildren().add(
                new ChatMessageView("img/avatar.png", currMessage.getContent(), false));
    }

    private void updateView() {
        if (userModel == null) throw new NullPointerException("UserModel is null");
        if (chatModel == null) throw new NullPointerException("ChatModel is null");

        chatNameLabel.setText(chatModel.getChatName());
        messagesContainer.getChildren().clear();
        extraInfoLabel.setText("23 members");

        Task<ArrayList<Pane>> task = new Task<ArrayList<Pane>>() {
            @Override
            protected ArrayList<Pane> call() {
                ArrayList<Pane> messageViews = new ArrayList<>();
                for (MessageModel messageModel : chatModel.getMessageModels()) {
                    Pane chatMessageView  = new ChatMessageView("img/avatar.png", messageModel.getContent(),
                            userModel.getId() == messageModel.getSenderId());
                    messageViews.add(chatMessageView);
                }
                return messageViews;
            }
        };
        new Thread(task).start();
        task.setOnSucceeded(event -> {
            messagesContainer.getChildren().addAll(task.getValue());
        });
    }


}
