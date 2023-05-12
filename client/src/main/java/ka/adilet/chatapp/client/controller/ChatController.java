package ka.adilet.chatapp.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.MessageModel;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.communication.CommunicationMessage;
import ka.adilet.chatapp.communication.MessageType;


public class ChatController {
    private final BooleanProperty isChatSelected = new SimpleBooleanProperty();
    private ListView<ChatModel> chatListView;
    private final Network network = Context.getNetwork();
    private final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML Parent chatListSection;
    @FXML Parent chattingSection;
    @FXML Pane shadowPane;
    @FXML VBox newChatDialog;
    @FXML ListView<JsonNode> dialogListView;
    @FXML ChatListController chatListSectionController;
    @FXML ChattingSectionController chattingSectionController;

    @FXML
    public void initialize() {
        chattingSectionController.setNetwork(network);

        chatListView = chatListSectionController.getChatListView();
        chatListView.setItems(Context.getChatModels());

        chatListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chattingSectionController.setChatListSelection(chatListView.getSelectionModel());
 

        isChatSelected.bind(chatListView.getSelectionModel().selectedItemProperty().isNotNull());
        isChatSelected.addListener((observable, oldValue, newValue) -> {
            chattingSection.setVisible(newValue);
            chattingSection.setDisable(oldValue);
        });

        chatListView.getSelectionModel().selectedItemProperty()
                .addListener(((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                chattingSectionController.switchChat(newValue);
            }
        }));

        chatListView.getSelectionModel().clearSelection();
        chattingSection.setVisible(false);
        chattingSectionController.setUserModel(Context.getUserModel());

        chatListSectionController.getAddChatButton().setOnAction((e) -> openNewChatDialog());

        chatListView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (!newChatDialog.isDisabled()) {
                    closeNewChatDialog();
                }
                else {
                    chatListView.getSelectionModel().clearSelection();
                }
            }
        });

        dialogListView.setOnMouseClicked((e) -> selectUserToChat());
        listenForServer();
    }

    @FXML
    private void closeNewChatDialog() {
        shadowPane.setVisible(false);
        shadowPane.setDisable(true);
        newChatDialog.setVisible(false);
        newChatDialog.setDisable(true);
    }

    private void openNewChatDialog() {
        shadowPane.setDisable(false);
        shadowPane.setVisible(true);
        newChatDialog.setVisible(true);
        newChatDialog.setDisable(false);
        ObjectNode requestBody = jsonMapper.createObjectNode();
        requestBody.put("all", "true");
        requestBody.put("ids", "[]");
        network.sendMessage(new CommunicationMessage(MessageType.GET_USERS, requestBody.toString()));
    }

    private void addMessageModel(CommunicationMessage response) {
        // Adds received messages from other users to corresponding chat models and updates UI
        MessageModel message;
        JsonNode respNode;
        ChatModel chat;
        ChatModel selectedChat = chatListView.getSelectionModel().getSelectedItem();
        try {
            respNode = jsonMapper.readTree(response.getBody());
            chat = jsonMapper.readValue(respNode.get("chat").toString(), ChatModel.class);
            message = jsonMapper.readValue(respNode.get("message").toString(), MessageModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // Searches for corresponding chat model
        boolean found = false;
        for (int i = 0; i < Context.getChatModels().size(); i++) {
            if (Context.getChatModels().get(i).getChatRoomId() != message.getChatRoomId()) continue;
            Context.getChatModels().get(i).addMessage(message);
            found = true;
        }
        if (!found) {
            ChatModel.formatChatName(chat);
            chatListView.getItems().add(0, chat);
        }
        // Updates UI
        if (selectedChat != null) {
            if (selectedChat.getChatRoomId() == message.getChatRoomId()) {
                chattingSectionController.addChatMessageView(message);
            }
        } else {
            chatListView.getSelectionModel().clearSelection();
        }
    }

    private void addUsersDataToNewChatDialog(CommunicationMessage response) {
        ObservableList<JsonNode> users = FXCollections.observableArrayList();
        try {
            for (JsonNode user : jsonMapper.readTree(response.getBody()).get("users")) {
                if (Context.getUserModel().getId() == user.get("id").asLong()) continue;
                users.add(user);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        dialogListView.setCellFactory(listView -> new ListCell<JsonNode>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final Label lastSeenLabel = new Label();

            @Override
            public void updateItem(JsonNode user, boolean empty) {
                super.updateItem(user, empty);
                if (user == null || empty) {
                    setText(null);
                    setGraphic(null);
                }
                if (user != null) {
                    setText(null);
                    setGraphic(buildGraphic(user));
                }
            }

            public Pane buildGraphic(JsonNode user) {
                HBox hbox = new HBox();
                VBox vbox = new VBox();
                vbox.getChildren().add(nameLabel);
                vbox.getChildren().add(lastSeenLabel);
                nameLabel.setStyle("-fx-text-fill: white;" +
                        "-fx-font-weight: bold;");
                lastSeenLabel.setStyle("-fx-text-fill: #777777;");
                nameLabel.setText(user.get("name").asText());
                lastSeenLabel.setText("lass seen recently");
                Image avatar = new Image(ChatApplication.class.getResourceAsStream("img/avatar.png"));
                imageView.setImage(avatar);
                imageView.setFitHeight(36);
                imageView.setFitWidth(36);
                Circle clip = new Circle(18, 18, 18);
                imageView.setClip(clip);
                hbox.getChildren().add(imageView);
                hbox.getChildren().add(vbox);
                hbox.setSpacing(5);
                return hbox;
            }
        });
        dialogListView.setItems(users);
        dialogListView.getSelectionModel().clearSelection();
    }

    private void selectUserToChat() {
        JsonNode user = dialogListView.getSelectionModel().getSelectedItem();
        if (user == null) return;
        // Check if selected user had conversation with current user
        for (ChatModel chatModel : Context.getChatModels()) {
            if (!chatModel.isPrivateChat()) continue;
            if (chatModel.getChatName().equals(user.get("name").asText())) {
                closeNewChatDialog();
                chatListView.getSelectionModel().select(chatModel);
                return;
            }
        }

        // Making a request to create new chat;
        ObjectNode newChat = jsonMapper.createObjectNode();
        newChat.put("is_private", "true");
        String chatName = String.format("%d:%s %s, %d:%s",
                Context.getUserModel().getId(),
                Context.getUserModel().getName(),
                Context.getUserModel().getSurname(),
                user.get("id").asLong(),
                user.get("name").asText());
        newChat.put("name", chatName);
        ArrayNode members = jsonMapper.createArrayNode()
                .add(user.get("id").asLong())
                .add(Context.getUserModel().getId());
        newChat.set("members", members);
        network.sendMessage(new CommunicationMessage(
                MessageType.NEW_CHAT,
                newChat.toString()
        ));
        chatListView.getSelectionModel().clearSelection();
        closeNewChatDialog();
    }
    
    private void listenForServer() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                CommunicationMessage response;
                while ((response = network.listen()) != null) {
                    System.out.println("listening for server...");
                    System.out.println("[SERVER] " + response.getType());
                    CommunicationMessage finalResponse = response;
                    Platform.runLater(() -> {
                        switch (finalResponse.getType()) {
                            case CHAT -> addMessageModel(finalResponse);
                            case GET_USERS -> addUsersDataToNewChatDialog(finalResponse);
                            case NEW_CHAT -> {
                                try {
                                    JsonNode responseJson = jsonMapper.readTree(finalResponse.getBody());
                                    ChatModel chat = jsonMapper.readValue(responseJson.get("chat").toString(), ChatModel.class);
                                    ChatModel.formatChatName(chat);
                                    chattingSectionController.switchChat(chat);
                                    chattingSection.setDisable(false);
                                    chattingSection.setVisible(true);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}
