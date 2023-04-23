package ka.adilet.chatapp.client.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.model.UserModel;


public class Context {
    private static UserModel userModel = new UserModel();
    private static ObservableList<ChatModel> chatModels = FXCollections.observableArrayList();

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
}
