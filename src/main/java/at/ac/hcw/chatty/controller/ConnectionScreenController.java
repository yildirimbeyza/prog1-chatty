package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;
import at.ac.hcw.chatty.model.ConnectionInfo;

import at.ac.hcw.chatty.service.ChatServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ConnectionScreenController {
    @FXML
    private TextField usernameInput;

    @FXML
    private TextField portInput;

    @FXML
    private RadioButton clientMode;

    @FXML
    private RadioButton serverMode;

    private ToggleGroup modeGroup;

    @FXML
    public void initialize() {
        modeGroup = new ToggleGroup();
        clientMode.setToggleGroup(modeGroup);
        serverMode.setToggleGroup(modeGroup);
        clientMode.setSelected(true);

        int randomPort = 8080 + (int)(Math.random() * 100);
        portInput.setText(String.valueOf(randomPort));
        usernameInput.setText("User" + (int)(Math.random() * 1000));
    }

    @FXML
    private void handleStart() {
        String username = usernameInput.getText().trim();
        String portStr = portInput.getText().trim();

        if (username.isEmpty()) {
            showError("Eingabefehler", "Bitte Username eingeben!");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);

            if (serverMode.isSelected()) {
                startServer(port);
            } else {
                showRoomList(username, "localhost", port);
            }
        } catch (NumberFormatException e) {
            showError("Ungültiger Port", "Port muss eine Zahl sein!");
        }
    }

    private void startServer(int port) {
        try {
            ChatServer.getInstance().start(port);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Server gestartet");
            alert.setHeaderText("Multi-Room Server aktiv");
            alert.setContentText("Server läuft auf Port " + port + "\nClients können sich verbinden!");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Server-Fehler", "Konnte Server nicht starten: " + e.getMessage());
        }
    }

    private void showRoomList(String username, String host, int port) {
        RoomListScreenController.setConnectionInfo(username, host, port);
        Stage stage = (Stage) usernameInput.getScene().getWindow();
        ChattyApp.showRoomListScreen(stage);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chatty - " + title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
