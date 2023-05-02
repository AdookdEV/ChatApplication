package ka.adilet.chatapp.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.MessageModel;
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.client.view.ChatMessageView;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ChattingSectionController {
    private ChatModel chatModel;
    private UserModel userModel;
    private Network network;
    private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private SelectionModel<ChatModel> chatListSelection;


    @FXML
    private Button sendMessageButton;
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
        sendMessageButton.setOnAction((ActionEvent event) -> sendMessage());
        messagesContainer.getChildren().addListener((ListChangeListener<Node>) change -> {
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }


    public void setChatListSelection(SelectionModel<ChatModel> chatListSelection) {
        this.chatListSelection = chatListSelection;
    }

    public void switchChat(ChatModel chatModel) {
        this.chatModel = chatModel;
        scrollPane.lookup(".increment-button").setStyle("visibility: false");
        updateView();
    }

    private void sendMessage() {
        String messageContent = messageTextField.getText();
        if (messageContent.length() == 0) return;
        if (!network.isConnected.getValue()) return;
        if (!Context.getChatModels().contains(this.chatModel)) {
            Context.getChatModels().add(0, this.chatModel);
            chatListSelection.select(this.chatModel);
        }
        MessageModel currMessage = new MessageModel(
                chatModel.getChatRoomId(),
                userModel.getId(),
                userModel.getName() + " " + userModel.getSurname(),
                messageContent,
                LocalDateTime.now()
        );
        chatModel.addMessage(currMessage);
        try {
            network.sendMessage(new CommunicationMessage(
                    MessageType.CHAT,
                    jsonMapper.writeValueAsString(currMessage)
            ));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        messageTextField.clear();
        addChatMessageView(currMessage);
    }

    private void updateView() {
        if (userModel == null) throw new NullPointerException("UserModel is null");
        if (chatModel == null) throw new NullPointerException("ChatModel is null");

        chatNameLabel.setText(chatModel.getChatName());
        messagesContainer.getChildren().clear();
        extraInfoLabel.setText("23 members");
        if (chatModel.isPrivateChat()) {
            extraInfoLabel.setText("last seen 5 minutes ago");
        }

        Task<ArrayList<Pane>> task = new Task<>() {
            @Override
            protected ArrayList<Pane> call() {
                ArrayList<Pane> messageViews = new ArrayList<>();
                for (MessageModel messageModel : chatModel.getMessageModels()) {
                    LocalDateTime dateTime = messageModel.getSentTime();
                    String date = dateTime.getDayOfMonth() + " "
                            +  dateTime.getMonth().name() + " "
                            + dateTime.getHour() + ":"
                            + dateTime.getMinute();
                    Pane chatMessageView = new ChatMessageView("img/avatar.png",
                            messageModel.getContent(),
                            messageModel.getSenderName(),
                            date,
                            userModel.getId() != messageModel.getSenderId(),
                            false);
                    messageViews.add(chatMessageView);
                }
                return messageViews;
            }
        };
        new Thread(task).start();
        task.setOnSucceeded(event -> messagesContainer.getChildren().addAll(task.getValue()));
    }

    public void addChatMessageView(MessageModel messageModel) {
        LocalDateTime dateTime = messageModel.getSentTime();
        String date = dateTime.getDayOfMonth() + " "
                +  dateTime.getMonth().name() + " "
                + dateTime.getHour() + ":"
                + dateTime.getMinute();
        Pane chatMessageView  = new ChatMessageView("img/avatar.png",
                messageModel.getContent(),
                messageModel.getSenderName(),
                date,
                userModel.getId() != messageModel.getSenderId(),
                false);
        messagesContainer.getChildren().add(chatMessageView);
        messagesContainer.layout();
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}
