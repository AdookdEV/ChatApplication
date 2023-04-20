package ka.adilet.chatapp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;


public class ChatMessageController {
    @FXML
    private Label messageContentLabel;
    @FXML
    private ImageView avatarImageView;
    @FXML
    public void initialize() {
    }

    public void setMessageContent(String text) {
        messageContentLabel.setText(text);
    }

    public void setAvatar(String imagePath) {
        Image avatarImage = new Image(ChatApplication.class.getResourceAsStream(imagePath));
        avatarImageView.setImage(avatarImage);
        double center_x = avatarImageView.getX() + avatarImageView.getFitWidth() / 2;
        double center_y = avatarImageView.getY() + avatarImageView.getFitWidth() / 2;
        avatarImageView.setClip(new Circle(center_x, center_y, 16));
    }
}
