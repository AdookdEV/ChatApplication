package ka.adilet.chatapp.client.model;

public class MessageModel {
    private long senderId;
    private long chatRoomId;
    private String content;
    private String sentTime;
    private static int id;

    public MessageModel(long senderId, long chatRoomId, String content, String sentTime) {
        this.senderId = senderId;
        this.chatRoomId = chatRoomId;
        this.content = content;
        this.sentTime = sentTime;
        this.id++;
    }

    public MessageModel() {
        this.senderId = 0;
        this.chatRoomId = 0;
        this.content = "Message" + id;
        this.sentTime = "12:20";
        this.id++;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getChatRoomId() {
        return chatRoomId;
    }

    public String getContent() {
        return content;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setContent(String text) {
        this.content = text;
    }
}
