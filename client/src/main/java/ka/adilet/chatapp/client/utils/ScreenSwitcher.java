package ka.adilet.chatapp.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import ka.adilet.chatapp.client.ChatApplication;


import java.io.IOException;

public class ScreenSwitcher {
    private static Scene scene;

    public static void setScene(Scene scene) {
        ScreenSwitcher.scene = scene;
    }

    public static void switchTo(Screen screen) {
        if (scene == null) {
            System.out.println("No scene was set");
            return;
        }
        try {
            Parent root = FXMLLoader.load(ChatApplication.class.getResource(screen.getFileName()));
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println(e);
//            throw new RuntimeException(e);
        }
    }
}
