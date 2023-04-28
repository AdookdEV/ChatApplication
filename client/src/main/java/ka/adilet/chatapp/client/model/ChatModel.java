package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.*;
import javafx.beans.property.SimpleStringProperty;
import ka.adilet.chatapp.client.utils.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChatModel {
    private Long chatRoomId;
    private final SimpleStringProperty chatName = new SimpleStringProperty();

    private ArrayList<MessageModel> messageModels;
    private String avatarImageName;
    private Boolean isPrivateChat;
    private final Map<String, String> unrecognizedFields = new HashMap<>();

    public ChatModel() {
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = false;
        messageModels = new ArrayList<>();
    }

    @JsonCreator
    public ChatModel(@JsonProperty("id") Long chatRoomId,
                     @JsonProperty("name") String chatName,
                     @JsonProperty("messages") ArrayList<MessageModel> messageModels,
                     @JsonProperty("is_private") Boolean isPrivateChat) {
        this.chatRoomId = chatRoomId;
        this.chatName.set(chatName);
        this.messageModels = messageModels;
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = isPrivateChat;
        if (messageModels == null) {
            this.messageModels = new ArrayList<>();
        }
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


    @JsonGetter("id")
    public Long getChatRoomId() {
        return chatRoomId;
    }
    @JsonGetter("messages")
    public ArrayList<MessageModel> getMessageModels() {
        return messageModels;
    }

    public MessageModel getLastMessage() {
        return (this.messageModels.isEmpty())
                ? null
                : this.messageModels.get(this.messageModels.size() - 1);
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

    @JsonGetter("messages")
    public void setMessageModels(ArrayList<MessageModel> messageModels) {
        if (messageModels == null) {
            this.messageModels = new ArrayList<>();
            return;
        }
        this.messageModels = messageModels;
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
    }

    @JsonAnySetter
    public void allSetter(String fieldName, String value) {
        unrecognizedFields.put(fieldName, value);
    }
}
