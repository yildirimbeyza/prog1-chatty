package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;
import at.ac.hcw.chatty.model.ConnectionInfo;
import at.ac.hcw.chatty.service.ChatConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ConnectionScreenController {
    @FXML
    private TextField ipInput;

    @FXML
    private TextField portInput;

    private ChatConnection connection;

    @FXML
    public void initialize() {
        // Suggest different ports for easier testing
        int suggestedPort = 8080 + (int)(Math.random() * 100);
        portInput.setText(String.valueOf(suggestedPort));

        // Default to localhost for easy testing
        ipInput.setText("localhost");

        // Create new connection instance for this window
        connection = ChatConnection.getNewInstance();
    }

    @FXML
    private void handleConnect() {
        String host = ipInput.getText().trim();
        String portStr = portInput.getText().trim();

        if (host.isEmpty() || portStr.isEmpty()) {
            showError("Eingabefehler", "Bitte IP-Adresse und Port eingeben!");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                showError("Ungültiger Port", "Port muss zwischen 1 und 65535 liegen!");
                return;
            }

            connectToServer(host, port);

        } catch (NumberFormatException e) {
            showError("Ungültiger Port", "Port muss eine Zahl sein!");
        }
    }

    @FXML
    private void handleServerMode() {
        String portStr = portInput.getText().trim();

        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                showError("Ungültiger Port", "Port muss zwischen 1 und 65535 liegen!");
                return;
            }

            startServerMode(port);

        } catch (NumberFormatException e) {
            showError("Ungültiger Port", "Port muss eine Zahl sein!");
        }
    }

    private void connectToServer(String host, int port) {
        ChattyApp.showWaitingScreen();

        new Thread(() -> {
            try {
                connection.connectAsClient(host, port);

                ConnectionInfo.getInstance().setConnection(host, port, false);

                javafx.application.Platform.runLater(() -> {
                    ChattyApp.showChatScreen();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Verbindung fehlgeschlagen",
                            "Konnte keine Verbindung zu " + host + ":" + port + " herstellen.\n\n" +
                                    "Fehler: " + e.getMessage() + "\n\n" +
                                    "Tipp: Starte zuerst eine Instanz im Server-Modus!");
                    ChattyApp.showConnectionScreen();
                });
            }
        }).start();
    }

    private void startServerMode(int port) {
        ChattyApp.showWaitingScreen();

        new Thread(() -> {
            try {
                connection.connectAsServer(port);

                String clientAddress = connection.getRemoteAddress();
                ConnectionInfo.getInstance().setConnection(clientAddress, port, true);

                javafx.application.Platform.runLater(() -> {
                    ChattyApp.showChatScreen();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Server-Modus fehlgeschlagen",
                            "Konnte Server nicht auf Port " + port + " starten.\n\n" +
                                    "Fehler: " + e.getMessage() + "\n\n" +
                                    "Tipp: Port ist möglicherweise bereits in Benutzung. Versuche einen anderen Port!");
                    ChattyApp.showConnectionScreen();
                });
            }
        }).start();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chatty - " + title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
