package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;


public class ChatModel {
    private Long chatRoomId;
    private String chatName;
    private ArrayList<MessageModel> messageModels;
    private String avatarImageName;
    private Boolean isPrivateChat;

    public ChatModel() {
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = false;
        this.chatRoomId = 0L;
        this.chatName = "Chat name";
        this.messageModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messageModels.add(new MessageModel());
        }
    }

    public ChatModel(Long chatRoomId,
                     String chatName,
                     ArrayList<MessageModel> messageModels,
                     String avatarImageName,
                     Boolean isPrivateChat) {
        this.chatRoomId = chatRoomId;
        this.chatName = chatName;
        this.messageModels = messageModels;
        this.avatarImageName = avatarImageName;
        this.isPrivateChat = isPrivateChat;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public ArrayList<MessageModel> getMessageModels() {
        return messageModels;
    }

    public MessageModel getLastMessage() {
        return messageModels.get(0);
    }

    public String getChatName() {
        return chatName;
    }

    public String getAvatarImageName() {
        return avatarImageName;
    }

    public Boolean isPrivateChat() {
        return isPrivateChat;
    }

    @JsonSetter("id")
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    @JsonSetter("messages")
    public void setMessageModels(ArrayList<MessageModel> messageModels) {
        this.messageModels = messageModels;
    }

    @JsonSetter("is_private")
    public void setPrivateChat(Boolean aPrivate) {
        isPrivateChat = aPrivate;
    }

    @JsonSetter("name")
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setAvatarImageName(String avatarImageName) {
        this.avatarImageName = avatarImageName;
    }

    public void addMessage(MessageModel messageModel) {
        messageModels.add(messageModel);
    }
}
