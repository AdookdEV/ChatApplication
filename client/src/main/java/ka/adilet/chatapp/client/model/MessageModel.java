package ka.adilet.chatapp.client.model;


import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MessageModel {
    private long chatRoomId;
    private long senderId;
    private String senderName;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;
    private Map<String, String> unrecognizedFields = new HashMap<>();

    @JsonCreator
    public MessageModel(@JsonProperty("chat_room_id")long chatRoomId,
                        @JsonProperty("sender_id")long senderId,
                        @JsonProperty("sender_name")String senderName,
                        @JsonProperty("content")String content,
                        @JsonProperty("sent_time")LocalDateTime sentTime) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.sentTime = sentTime;
        this.senderName = senderName;
    }

//    public MessageModel() {
//        this.senderId = 0;
//        this.content = "Message";
//        this.senderName = "Some name";
//        this.sentTime = LocalDateTime.now();
//    }

    @JsonGetter("chat_room_id")
    public long getChatRoomId() {
        return chatRoomId;
    }
    @JsonGetter("sender_id")
    public long getSenderId() {
        return senderId;
    }
    @JsonGetter("content")
    public String getContent() {
        return content;
    }
    @JsonGetter("sent_time")
    public LocalDateTime getSentTime() {
        return sentTime;
    }

    @JsonGetter("sender_name")
    public String getSenderName() {
        return senderName;
    }
    @JsonSetter("sender_name")
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    @JsonSetter("sender_id")
    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }
    @JsonSetter("sent_time")
    public void setSentTime(LocalDateTime sentTime) {
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

    @JsonAnySetter
    public void allSetter(String fieldName, String value) {
        unrecognizedFields.put(fieldName, value);
    }
}
