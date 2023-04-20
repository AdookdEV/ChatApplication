package ka.adilet.chatapp.client.model;


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

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setMessageModels(ArrayList<MessageModel> messageModels) {
        this.messageModels = messageModels;
    }

    public void setPrivateChat(Boolean aPrivate) {
        isPrivateChat = aPrivate;
    }

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
