package ka.adilet.chatapp.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.MessageModel;
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.communication.CommunicationMessage;


public class ChatController {
    private final BooleanProperty isChatSelected = new SimpleBooleanProperty();
    private ListView<ChatModel> chatListView;
    private Network network = Context.getNetwork();
    private ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    private Parent chatListSection;
    @FXML
    private Parent chattingSection;
    @FXML
    private GridPane root;
    @FXML
    private ChatListController chatListSectionController;
    @FXML
    private ChattingSectionController chattingSectionController;
    private String message;

    @FXML
    public void initialize() {
        chattingSectionController.setNetwork(network);
        chatListView = chatListSectionController.getChatListView();
        chatListView.setItems(Context.getChatModels());
        isChatSelected.bind(chatListView.getSelectionModel().selectedItemProperty().isNotNull());
        isChatSelected.addListener((observable, oldValue, newValue) -> {
            chattingSection.setVisible(newValue);
            chattingSection.setDisable(oldValue);
        });
        chatListView.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldValue, newValue) -> {
            chattingSectionController.switchChat(newValue);
        }));
        chatListView.getSelectionModel().clearSelection();
        chattingSection.setVisible(false);
        chattingSectionController.setUserModel(Context.getUserModel());
        listenForServer();
    }

    private void listenForServer() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                CommunicationMessage reponse;
                while ((reponse = network.listen()) != null) {
                    System.out.println("listening for server...");
                    MessageModel message = jsonMapper.readValue(reponse.getBody(), MessageModel.class);
                    System.out.printf("[SERVER] %s\n", message.getContent());
                    Platform.runLater(() -> {
                        chattingSectionController.addChatMessage(message);
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}
