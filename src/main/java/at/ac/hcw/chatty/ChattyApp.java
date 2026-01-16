package at.ac.hcw.chatty;

import at.ac.hcw.chatty.controller.ChatScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChattyApp extends Application {

    private static List<Stage> chatWindows = new ArrayList<>();
    private static int instanceNumber = 0;

    @Override
    public void start(Stage stage) {
        instanceNumber++;
        Stage primaryStage = new Stage();
        primaryStage.setTitle("💬 Chatty #" + instanceNumber);
        showConnectionScreen(primaryStage);
        primaryStage.show();
        chatWindows.add(primaryStage);
    }

    public static void showConnectionScreen(Stage stage) {
        loadScene(stage, "ConnectionScreen.fxml", 600, 750);
    }

    public static void showRoomListScreen(Stage stage) {
        loadScene(stage, "RoomListScreen.fxml", 700, 600);
    }

    public static Stage openNewChatWindow(String roomName, String username) {
        try {
            Stage chatStage = new Stage();
            chatStage.setTitle("💬 Chatty - " + roomName);

            FXMLLoader loader = new FXMLLoader(ChattyApp.class.getResource("ChatScreen.fxml"));
            Parent root = loader.load();

            ChatScreenController controller = loader.getController();
            controller.setRoomInfo(roomName, username);

            Scene scene = new Scene(root, 700, 750);
            chatStage.setScene(scene);
            chatStage.show();
            chatWindows.add(chatStage);

            return chatStage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void loadScene(Stage stage, String fxmlFile, int width, int height) {
        try {
            Parent root = FXMLLoader.load(ChattyApp.class.getResource(fxmlFile));
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}