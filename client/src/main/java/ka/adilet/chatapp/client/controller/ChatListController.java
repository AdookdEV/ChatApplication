package ka.adilet.chatapp.client.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import ka.adilet.chatapp.client.ChatApplication;
import ka.adilet.chatapp.client.model.ChatModel;


public class ChatListController {
    @FXML
    private ListView<ChatModel> chatListView;
    @FXML
    private TextField searchChatTextField;

    private ObservableList<ChatModel> chatModels;

    @FXML
    public void initialize() {
        chatListView.setCellFactory(param -> new ChatListCell());
    }

    public ListView<ChatModel> getChatListView() {
        return chatListView;
    }

    public void setChatModels(ObservableList<ChatModel> items) {
        chatListView.setItems(items);
    }

    private static class ChatListCell extends ListCell<ChatModel> {
        private final ImageView avatarImageView;
        private final Label lastMessageLabel;
        private final Label chatNameLabel;

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

        @Override
        protected void updateItem(ChatModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (item.getLastMessage() != null) {
                    lastMessageLabel.setText(item.getLastMessage().getContent());
                }
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

