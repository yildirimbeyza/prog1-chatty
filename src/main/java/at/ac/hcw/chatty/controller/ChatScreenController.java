package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;
import at.ac.hcw.chatty.model.ConnectionInfo;
import at.ac.hcw.chatty.service.ChatConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ChatScreenController {
    @FXML
    private Label connectionLabel;

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Label statusLabel;

    @FXML
    private ScrollPane chatScrollPane;

    private ChatConnection connection;

    @FXML
    public void initialize() {
        connection = ChatConnection.getInstance();

        // Set connection info
        ConnectionInfo info = ConnectionInfo.getInstance();
        connectionLabel.setText("Verbunden: " + info.getDisplayString());

        // Setup callbacks
        connection.setOnMessageReceived(this::addReceivedMessage);
        connection.setOnDisconnected(this::handleDisconnected);

        // Start receiving messages
        connection.startReceiving();

        // Bind scroll to bottom
        chatScrollPane.vvalueProperty().bind(chatContainer.heightProperty());

        // Focus on input field
        messageInput.requestFocus();
    }

    @FXML
    private void handleSend() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;

        connection.sendMessage(message);
        addSentMessage(message);
        messageInput.clear();
    }

    @FXML
    private void handleDisconnect() {
        connection.disconnect();
        ChattyApp.showConnectionScreen();
    }

    private void addReceivedMessage(String message) {
        HBox messageBox = createMessageBubble(message, false);
        chatContainer.getChildren().add(messageBox);
    }

    private void addSentMessage(String message) {
        HBox messageBox = createMessageBubble(message, true);
        chatContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBubble(String message, boolean isSent) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);

        String bgColor = isSent ? "#2563eb" : "#e5e7eb";
        String textColor = isSent ? "white" : "#1f2937";
        String timeColor = isSent ? "#bfdbfe" : "#6b7280";

        bubble.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-background-radius: 10;"
        );

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 12));
        messageLabel.setTextFill(Color.web(textColor));

        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setFont(Font.font("Arial", 9));
        timeLabel.setTextFill(Color.web(timeColor));

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        return messageBox;
    }

    private String getCurrentTime() {
        return java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        );
    }

    private void handleDisconnected() {
        statusLabel.setText("Status: ✗ Verbindung unterbrochen");
        statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageInput.setDisable(true);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Chatty - Verbindung unterbrochen");
        alert.setHeaderText("Verbindung unterbrochen");
        alert.setContentText("Der Chat-Partner hat die Verbindung beendet.");
        alert.showAndWait();
    }
}
