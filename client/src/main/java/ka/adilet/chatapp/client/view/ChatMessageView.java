package ka.adilet.chatapp.client.view;

import javafx.geometry.HPos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;


import java.io.InputStream;
import java.util.Objects;

public class ChatMessageView extends GridPane {
    private final HBox containerHbox = new HBox();
    private final ImageView avatarImageView = new ImageView();
    private final Label messageContentLabel = new Label();
    private final Boolean alignToLeft;

    public ChatMessageView(String imagePath, String text, Boolean alignToLeft) {
        super();
        this.alignToLeft = alignToLeft;
        init();
        setAvatar(imagePath);
        setMessageContent(text);

    }

    public ChatMessageView(Boolean alignToLeft) {
        super();
        this.alignToLeft = alignToLeft;
        init();
        setAvatar("img/avatar.png");
        setMessageContent("Hello friend");
    }

    private void init() {
        setHgap(5);
        messageContentLabel.setWrapText(true);
        messageContentLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: #33393F;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 7 10 7 10;");
        messageContentLabel.setPrefHeight(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setPrefWidth(Control.USE_COMPUTED_SIZE);

        messageContentLabel.setMaxHeight(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setMaxWidth(300);

        messageContentLabel.setMinWidth(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setMinHeight(Control.USE_COMPUTED_SIZE);

        avatarImageView.setFitWidth(32);
        avatarImageView.setFitHeight(32);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(-1);
        col.setHgrow(Priority.ALWAYS);
        col.setFillWidth(false);
        getColumnConstraints().add(col);

        if (alignToLeft) {
            col.setHalignment(HPos.LEFT);
            containerHbox.getChildren().add(avatarImageView);
            containerHbox.getChildren().add(messageContentLabel);
            GridPane.setHalignment(containerHbox, HPos.LEFT);
        } else {
            col.setHalignment(HPos.RIGHT);
            containerHbox.getChildren().add(messageContentLabel);
            containerHbox.getChildren().add(avatarImageView);
        }
        containerHbox.setSpacing(5);

        add(containerHbox, 0, 0, 1, 1);
    }

    public void setMessageContent(String text) {
        messageContentLabel.setText(text);
    }

    public void setAvatar(String imagePath) {
        InputStream imageStream = ChatApplication.class.getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.err.println("Couldn't load avatar of the message");
            return;
        }
        Image avatarImage = new Image(imageStream);
        avatarImageView.setImage(avatarImage);
        double center_x = avatarImageView.getX() + avatarImageView.getFitWidth() / 2;
        double center_y = avatarImageView.getY() + avatarImageView.getFitWidth() / 2;
        avatarImageView.setClip(new Circle(center_x, center_y, 16));
    }

}
