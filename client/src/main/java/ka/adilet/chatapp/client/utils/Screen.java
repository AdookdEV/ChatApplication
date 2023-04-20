package ka.adilet.chatapp.client.utils;

public enum Screen {
    LOGIN("fxml/login.fxml"),
    REGISTRATION("fxml/registration.fxml"),
    CHAT("fxml/chat.fxml");

    private String fileName;

    Screen(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
