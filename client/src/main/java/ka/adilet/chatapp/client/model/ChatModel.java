package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;


public class ChatModel {
    private Long chatRoomId;
    private String chatName;
    private ArrayList<MessageModel> messageModels;
    private String avatarImageName;
    private Boolean isPrivateChat;

//    public ChatModel() {
//        this.avatarImageName = "img/avatar.png";
//        this.isPrivateChat = false;
//        this.chatRoomId = 0L;
//        this.chatName = "Chat name";
//    }
    @JsonCreator
    public ChatModel(@JsonProperty("id") Long chatRoomId,
                     @JsonProperty("name") String chatName,
                     @JsonProperty("messages") ArrayList<MessageModel> messageModels,
                     @JsonProperty("is_private") Boolean isPrivateChat) {
        this.chatRoomId = chatRoomId;
        this.chatName = chatName;
        this.messageModels = messageModels;
        this.avatarImageName = "img/avatar.png";
        this.isPrivateChat = isPrivateChat;
        if (messageModels == null) {
            this.messageModels = new ArrayList<>();
        }
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
        return chatName;
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
        this.chatName = chatName;
    }

    public void setAvatarImageName(String avatarImageName) {
        this.avatarImageName = avatarImageName;
    }

    public void addMessage(MessageModel messageModel) {
        messageModels.add(messageModel);
    }
}
