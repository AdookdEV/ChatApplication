package ka.adilet.chatapp.client.controller;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;
import ka.adilet.chatapp.client.model.ChatModel;
import ka.adilet.chatapp.client.utils.Context;

import java.util.Comparator;


public class ChatListController {
    @FXML
    private ListView<ChatModel> chatListView;
    @FXML
    private TextField searchChatTextField;
    @FXML
    private Button addChatButton;

    @FXML
    public void initialize() {
        chatListView.setCellFactory(param -> new ChatListCell());
        chatListView.setFocusTraversable(true);
    }

    public void sortChats() {
        chatListView.getItems().sort(Comparator.comparing(o -> o.getLastMessageModel().getSentTime()));
    }

    public ListView<ChatModel> getChatListView() {
        return chatListView;
    }

    public Button getAddChatButton() {
        return addChatButton;
    }

    private static class ChatListCell extends ListCell<ChatModel> {
        private final ImageView avatarImageView;
        private final Label lastMessageLabel;
        private final Label chatNameLabel;
        private InvalidationListener listener;

        public ChatListCell() {
            this.avatarImageView = new ImageView();
            this.avatarImageView.setFitHeight(40);
            Circle clip = new Circle(20, 20, 20);
            avatarImageView.setClip(clip);
            this.avatarImageView.setFitWidth(40);

            this.lastMessageLabel = new Label();
            this.chatNameLabel = new Label();

            setGraphic(buildGraphic());
        }

        public void updateLastMessageLabel(ChatModel item) {
            if (item.getLastMessageModel() == null) return;
            String lm = item.getLastMessageModel().getContent();
            if (Context.getUserModel().getId() == item.getLastMessageModel().getSenderId()) {
                lm = "Me: " + lm;
            } else if (!item.isPrivateChat()) {
                lm = item.getLastMessageModel().getSenderName() + ": " + lm;
            }
            lastMessageLabel.setText(lm);
        }

        @Override
        protected void updateItem(ChatModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                updateLastMessageLabel(item);
                item.lastMessageProperty().addListener((o, newV, oldV) -> {
                    updateLastMessageLabel(item);
                });
                chatNameLabel.setText(item.getChatName());
                Image avatarImage = new Image(ChatApplication.class.getResourceAsStream(item.getAvatarImageName()));
                avatarImageView.setImage(avatarImage);
                setGraphic(buildGraphic());
            }
        }

        public GridPane buildGraphic() {
            GridPane grid = new GridPane();
            grid.add(avatarImageView, 0, 0, 1, 2);
            grid.add(chatNameLabel, 1, 0);
            grid.add(lastMessageLabel, 1, 1);
            grid.setHgap(10);

            chatNameLabel.setStyle("-fx-font-size: 1.1em; " +
                    "-fx-font-weight: 700;" +
                    "-fx-text-fill: white;");
            lastMessageLabel.setStyle("-fx-text-fill: white;");

            return grid;
        }
    }
}

