package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.JsonSetter;

public class MessageModel {
    private long chatRoomId;
    private long id;
    private long senderId;
    private String content;
    private String sentTime;


    public MessageModel(long senderId, long id, String content, String sentTime) {
        this.senderId = senderId;
        this.id = id;
        this.content = content;
        this.sentTime = sentTime;
    }

    public MessageModel() {
        this.senderId = 0;
        this.id = 0;
        this.content = "Message";
        this.sentTime = "12:20";
    }

    public long getChatRoomId() {
        return chatRoomId;
    }
    public long getSenderId() {
        return senderId;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSentTime() {
        return sentTime;
    }

    @JsonSetter("sender_id")
    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }
    @JsonSetter("id")
    public void setId(long id) {
        this.id = id;
    }
    @JsonSetter("sent_time")
    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
    @JsonSetter("content")
    public void setContent(String text) {
        this.content = text;
    }

    @JsonSetter("chat_room_id")
    public void setChatRoomId(long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
