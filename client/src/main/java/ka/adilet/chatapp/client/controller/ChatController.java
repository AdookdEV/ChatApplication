package ka.adilet.chatapp.client.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;


public class ChatController {
    private final BooleanProperty isChatSelected = new SimpleBooleanProperty();
    private ListView<ChatModel> chatListView;
    private Network network;

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

    @FXML
    public void initialize() {
        network = new Network("localhost", 1234);
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
    }
}
