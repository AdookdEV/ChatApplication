package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ka.adilet.chatapp.client.utils.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatModel {
    private Long chatRoomId;
    private final SimpleStringProperty chatName = new SimpleStringProperty();
    private final ObservableList<MessageModel> messageModels = FXCollections.observableArrayList();
    private String avatarImageName;
    private Boolean isPrivateChat;
    private final Map<String, String> unrecognizedFields = new HashMap<>();
    @JsonIgnore
    private final SimpleStringProperty lastMessage = new SimpleStringProperty();

    public ChatModel() {
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = false;
    }

    @JsonCreator
    public ChatModel(@JsonProperty("id") Long chatRoomId,
                     @JsonProperty("name") String chatName,
                     @JsonProperty("messages") ArrayList<MessageModel> messageModels,
                     @JsonProperty("is_private") Boolean isPrivateChat) {
        this.chatRoomId = chatRoomId;
        this.chatName.set(chatName);
        if (messageModels != null && !messageModels.isEmpty()) {
            this.messageModels.addAll(messageModels);
            this.lastMessageProperty().set(getLastMessageModel().getContent());
        }
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = isPrivateChat;
    }

    public static void formatChatName(ChatModel chat) {
        if (chat.isPrivateChat()) {
            String[] idName = chat.getChatName().split(", ");
            if (idName[0].contains(Context.getUserModel().getId().toString())) {
                chat.setChatName(idName[1].split(":")[1]);
            } else {
                chat.setChatName(idName[0].split(":")[1]);
            }
        }
    }

    public Map<String, String> getUnrecognizedFields() {
        return unrecognizedFields;
    }

    public String getLastMessage() {
        return lastMessage.getValue();
    }

    public ObservableList<MessageModel> observableMessagesList() {
        return messageModels;
    }

    public MessageModel getLastMessageModel() {
        if (messageModels.isEmpty()) return null;
        return messageModels.get(messageModels.size() - 1);
    }

    public SimpleStringProperty lastMessageProperty() {
        return lastMessage;
    }

    @JsonGetter("id")
    public Long getChatRoomId() {
        return chatRoomId;
    }
    @JsonGetter("messages")
    public List<MessageModel> getMessageModels() {
        return  messageModels;
    }

    @JsonGetter("name")
    public String getChatName() {
        return chatName.getValue();
    }

    public String getAvatarImageName() {
        return avatarImageName;
    }

    @JsonGetter("is_private")
    public Boolean isPrivateChat() {
        return isPrivateChat;
    }

    @JsonSetter("id")
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    @JsonSetter("messages")
    public void setMessageModels(List<MessageModel> messageModels) {
        this.messageModels.addAll(messageModels);
    }

    @JsonSetter("is_private")
    public void setPrivateChat(Boolean aPrivate) {
        isPrivateChat = aPrivate;
    }

    @JsonSetter("name")
    public void setChatName(String chatName) {
        this.chatName.set(chatName);
    }

    public void setAvatarImageName(String avatarImageName) {
        this.avatarImageName = avatarImageName;
    }

    public void addMessage(MessageModel messageModel) {
        messageModels.add(messageModel);
        lastMessage.set(messageModel.getContent());
    }

    @JsonAnySetter
    public void allSetter(String fieldName, String value) {
        unrecognizedFields.put(fieldName, value);
    }

}
