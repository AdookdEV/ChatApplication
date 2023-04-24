package ka.adilet.chatapp.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.view.ChatMessageView;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class ChattingSectionController {
    private ChatModel chatModel;
    private UserModel userModel;
    private Network network;
    private ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private ObservableList<MessageModel> chatMessages = FXCollections.observableArrayList();

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
        chatMessages.addListener((ListChangeListener<? super MessageModel>) (observable) -> {
            if (!observable.next()) return;
            int size = chatMessages.size();
            MessageModel messageModel = chatMessages.get(size - 1);
            Pane chatMessageView  = new ChatMessageView("img/avatar.png",
                    messageModel.getContent(),
                    userModel.getName(),
                    messageModel.getSentTime().toString(),
                    userModel.getId() != messageModel.getSenderId(),
                    false);
            messagesContainer.getChildren().add(chatMessageView);
            messagesContainer.layout();
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
        if (messageContent.length() == 0) return;
        MessageModel currMessage = new MessageModel(
                chatModel.getChatRoomId(),
                userModel.getId(),
                messageContent,
                LocalDateTime.now()
        );
        currMessage.setContent(messageContent);
        chatModel.addMessage(currMessage);
        try {
            network.sendMessage(new CommunicationMessage(
                    MessageType.CHAT,
                    jsonMapper.writeValueAsString(currMessage)
            ));
        } catch (JsonProcessingException e) {
            System.err.println(e);
            return;
        }
        messageTextField.clear();
        messagesContainer.getChildren().add(
                new ChatMessageView("img/avatar.png", messageContent,
                userModel.getName(),
                LocalDateTime.now().toString(),
                false,
                false));
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
                    Pane chatMessageView  = new ChatMessageView("img/avatar.png",
                            messageModel.getContent(),
                            userModel.getName(),
                            messageModel.getSentTime().toString(),
                            userModel.getId() != messageModel.getSenderId(),
                            false);
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

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void addChatMessage(MessageModel messageModel) {
        chatModel.getMessageModels().add(messageModel);
        chatMessages.add(messageModel);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}
