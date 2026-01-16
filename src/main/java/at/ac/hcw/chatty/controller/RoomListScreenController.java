package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;

public class RoomListScreenController {
    @FXML
    public TextField newRoomInput;

    @FXML
    private ListView<String> roomListView;



    @FXML
    public javafx.scene.control.Label usernameLabel;

    private static String username;
    private static String host;
    private static int port;

    public static void setConnectionInfo(String user, String serverHost, int serverPort) {
        username = user;
        host = serverHost;
        port = serverPort;
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    @FXML
    public void initialize() {
        usernameLabel.setText("Eingeloggt als: " + username);

        // Add some default rooms
        roomListView.getItems().addAll(
                "General",
                "Random",
                "Tech Talk",
                "Gaming"
        );
    }

    @FXML
    private void handleJoinRoom() {
        String selected = roomListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String roomName = selected.split(" \\(")[0]; // Remove user count
            joinRoom(roomName);
        }
    }

    @FXML
    private void handleCreateRoom() {
        String roomName = newRoomInput.getText().trim();
        if (!roomName.isEmpty()) {
            if (!roomListView.getItems().contains(roomName)) {
                roomListView.getItems().add(roomName);
            }
            joinRoom(roomName);
            //TODO: .clear()
            newRoomInput = null;
        }
    }

    private void joinRoom(String roomName) {
        Stage chatWindow = ChattyApp.openNewChatWindow(roomName, username);

        // Don't close room list - allow joining multiple rooms!
        // User can have multiple chat windows open
    }
}
