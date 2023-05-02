package ka.adilet.chatapp.client.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;


import java.io.InputStream;

public class ChatMessageView extends GridPane {
    private final HBox containerHbox = new HBox();
    private final ImageView avatarImageView = new ImageView();
    private final Label messageContentLabel = new Label();
    private final VBox vbox = new VBox();
    private final Label messageOwnerLabel = new Label();
    private final Label timeLabel = new Label();
    private final Boolean alignToLeft;
    private final Boolean isPrivate;


    public ChatMessageView(String imagePath,
                           String text,
                           String ownerName,
                           String sentTime,
                           Boolean alignToLeft,
                           Boolean isPrivate) {
        super();
        this.alignToLeft = alignToLeft;
        this.isPrivate = isPrivate;
        this.messageOwnerLabel.setText(ownerName);
        this.timeLabel.setText(sentTime);
        init();
        setAvatar(imagePath);
        setMessageContent(text);
    }

    public ChatMessageView(Boolean alignToLeft) {
        super();
        this.alignToLeft = alignToLeft;
        this.isPrivate = false;
        init();
        setAvatar("img/avatar.png");
        setMessageContent("Hello friend");
    }

    private void init() {
        setHgap(5);
        initStyle();

        messageContentLabel.setWrapText(true);

        messageContentLabel.setPrefHeight(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setPrefWidth(Control.USE_COMPUTED_SIZE);

        messageContentLabel.setMaxHeight(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setMaxWidth(300);

        messageContentLabel.setMinWidth(Control.USE_COMPUTED_SIZE);
        messageContentLabel.setMinHeight(Control.USE_COMPUTED_SIZE);

        timeLabel.setMaxWidth(Double.MAX_VALUE);
        timeLabel.setAlignment(Pos.CENTER_RIGHT);

        avatarImageView.setFitWidth(32);
        avatarImageView.setFitHeight(32);
        HBox.setMargin(avatarImageView, new Insets(10, 0, 0, 0));

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(-1);
        col.setHgrow(Priority.ALWAYS);
        col.setFillWidth(false);
        getColumnConstraints().add(col);

        vbox.getChildren().add(messageOwnerLabel);
        vbox.getChildren().add(messageContentLabel);
        vbox.getChildren().add(timeLabel);

        if (alignToLeft) {
            col.setHalignment(HPos.LEFT);
            if (!isPrivate) containerHbox.getChildren().add(avatarImageView);
            containerHbox.getChildren().add(vbox);
            GridPane.setHalignment(containerHbox, HPos.LEFT);
        } else {
            col.setHalignment(HPos.RIGHT);
            containerHbox.getChildren().add(vbox);
            if (!isPrivate) containerHbox.getChildren().add(avatarImageView);
        }
        containerHbox.setSpacing(5);

        add(containerHbox, 0, 0, 1, 1);
    }

    public void initStyle() {
        vbox.setStyle("-fx-background-color: #33393F;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 2 10 5 10;");

        timeLabel.setStyle("-fx-text-fill: #acacac;" +
                "-fx-font-size: 9;");

        messageOwnerLabel.setStyle("-fx-text-fill:  #30AB9B;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13;");
        messageContentLabel.setStyle("-fx-text-fill: white;");
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
