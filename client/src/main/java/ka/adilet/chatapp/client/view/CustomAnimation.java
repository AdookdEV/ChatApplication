package ka.adilet.chatapp.client.view;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class CustomAnimation {
    public static void buttonClick(Button button) {
        ScaleTransition transition = new ScaleTransition(Duration.seconds(0.05), button);
        transition.setToX(1.1);
        transition.setToY(1.1);
        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();
    }
}
