package ka.adilet.chatapp.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ka.adilet.chatapp.client.network.Network;
import ka.adilet.chatapp.client.utils.Context;
import ka.adilet.chatapp.client.utils.Screen;
import ka.adilet.chatapp.client.utils.ScreenSwitcher;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Network network = new Network("localhost", 1234);
        Context.setNetwork(network);
        System.setProperty("prism.lcdtext", "false");
        Scene scene = new Scene(new Pane());
        ScreenSwitcher.setScene(scene);
        ScreenSwitcher.switchTo(Screen.LOGIN);
        stage.setTitle("Chat App");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindow);
        stage.show();
    }

    private void closeWindow(WindowEvent e) {
        System.out.println("Close the window");
        Context.getNetwork().stopConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}