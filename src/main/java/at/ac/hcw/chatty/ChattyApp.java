package at.ac.hcw.chatty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChattyApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("💬 Chatty - Simple Chat Tool");
        showConnectionScreen();
        primaryStage.show();
    }

    public static void showConnectionScreen() {
        loadScene("ConnectionScreen.fxml", 800, 700);
    }

    public static void showWaitingScreen() {
        loadScene("WaitingScreen.fxml", 800, 600);
    }

    public static void showChatScreen() {
        loadScene("ChatScreen.fxml", 800, 700);
    }

    private static void loadScene(String fxmlFile, int width, int height) {
        try {
            Parent root = FXMLLoader.load(ChattyApp.class.getResource(fxmlFile));
            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}