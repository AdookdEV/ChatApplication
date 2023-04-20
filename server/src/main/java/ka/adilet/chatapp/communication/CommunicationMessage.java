package ka.adilet.chatapp.communication;

import java.io.Serializable;

public class CommunicationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private String body;

    public CommunicationMessage() {};

    public CommunicationMessage(MessageType type, String body) {
        this.type = type;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

