package ka.adilet.chatapp.client.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.UserModel;
import ka.adilet.chatapp.client.network.Network;


public class Context {
    private static UserModel userModel = new UserModel();
    private static ObservableList<ChatModel> chatModels = FXCollections.observableArrayList();
    private static Network network;

    private static ChatModel selectedChatModel;

    public static ChatModel getSelectedChatModel() {
        return selectedChatModel;
    }

    public static void setSelectedChatModel(ChatModel selectedChatModel) {
        Context.selectedChatModel = selectedChatModel;
    }

    public static UserModel getUserModel() {
        return userModel;
    }
    public static ObservableList<ChatModel> getChatModels() {
        return chatModels;
    }

    public static void setUserModel(UserModel um) {
        userModel = um;
    }
    public static void setChatModels(ObservableList<ChatModel> cm) {
        chatModels = cm;
    }

    public static void setNetwork(Network network) {
        Context.network = network;
    }

    public static Network getNetwork() {
        return network;
    }

    public static void clear() {
        chatModels.clear();
        userModel = null;
    }
}
