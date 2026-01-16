package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;
import at.ac.hcw.chatty.model.ConnectionInfo;
import at.ac.hcw.chatty.service.ChatClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChatScreenController {
    @FXML
    private Label roomLabel;

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Label statusLabel;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private FlowPane stickerPanel;

    private ChatClient client;
    private String roomName;
    private String username;

    private static final String[] STICKERS = {
            "😀", "😂", "😍", "😎", "🤔", "😢", "😡", "👍",
            "👎", "❤️", "🔥", "⭐", "✅", "❌", "🎉", "🚀"
    };

    public void setRoomInfo(String room, String user) {
        this.roomName = room;
        this.username = user;
        initialize();
    }

    private void initialize() {
        roomLabel.setText("Room: " + roomName + " | " + username);

        // Setup sticker panel
        setupStickerPanel();

        // Connect to room
        connectToRoom();
    }

    private void setupStickerPanel() {
        stickerPanel.setHgap(5);
        stickerPanel.setVgap(5);

        for (String sticker : STICKERS) {
            Button stickerBtn = new Button(sticker);
            stickerBtn.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-background-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 5;"
            );
            stickerBtn.setOnAction(e -> sendSticker(sticker));
            stickerPanel.getChildren().add(stickerBtn);
        }
    }

    private void connectToRoom() {
        client = new ChatClient();

        new Thread(() -> {
            try {
                client.connect(
                        RoomListScreenController.getHost(),
                        RoomListScreenController.getPort(),
                        roomName,
                        username
                );


                javafx.application.Platform.runLater(() -> {
                    client.setOnMessageReceived(this::addReceivedMessage);
                    client.setOnDisconnected(this::handleDisconnected);
                    client.startReceiving();

                    statusLabel.setText("Status: ● Verbunden");
                    statusLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Verbindung fehlgeschlagen",
                            "Konnte nicht zu Raum verbinden.\nStelle sicher, dass der Server läuft!");
                    statusLabel.setText("Status: ✗ Nicht verbunden");
                    statusLabel.setStyle("-fx-text-fill: #dc2626;");
                });
            }
        }).start();
    }

    @FXML
    private void handleSend() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;

        client.sendMessage(message);
        addSentMessage(username + ": " + message);
        messageInput.clear();
    }

    private void sendSticker(String sticker) {
        client.sendSticker(sticker);
        addSentMessage(username + " sent: STICKER:" + sticker);
    }

    @FXML
    private void handleLeaveRoom() {
        client.disconnect();
        Stage stage = (Stage) messageInput.getScene().getWindow();
        stage.close();
    }

    private void addReceivedMessage(String message) {
        if (message.contains("STICKER:")) {
            String sticker = message.split("STICKER:")[1];
            addStickerBubble(message.split(" sent:")[0], sticker, false);
        } else if (message.startsWith(">>>") || message.startsWith("<<<")) {
            addSystemMessage(message);
        } else {
            HBox messageBox = createMessageBubble(message, false);
            chatContainer.getChildren().add(messageBox);
        }
    }

    private void addSentMessage(String message) {
        if (message.contains("STICKER:")) {
            String sticker = message.split("STICKER:")[1];
            addStickerBubble(username, sticker, true);
        } else {
            HBox messageBox = createMessageBubble(message, true);
            chatContainer.getChildren().add(messageBox);
        }
    }

    private void addStickerBubble(String sender, String sticker, boolean isSent) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setAlignment(Pos.CENTER);
        bubble.setStyle(
                "-fx-background-color: " + (isSent ? "#2563eb" : "#e5e7eb") + "; " +
                        "-fx-background-radius: 10;"
        );

        if (!isSent) {
            Label senderLabel = new Label(sender);
            senderLabel.setFont(Font.font("Arial", 10));
            senderLabel.setStyle("-fx-font-weight: bold;");
            senderLabel.setTextFill(Color.web("#6b7280"));
            bubble.getChildren().add(senderLabel);
        }

        Label stickerLabel = new Label(sticker);
        stickerLabel.setFont(Font.font("Arial", 40));

        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setFont(Font.font("Arial", 9));
        timeLabel.setTextFill(Color.web(isSent ? "#bfdbfe" : "#6b7280"));

        bubble.getChildren().addAll(stickerLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        chatContainer.getChildren().add(messageBox);
    }

    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5));

        Label systemLabel = new Label(message);
        systemLabel.setFont(Font.font("Arial", 11));
        systemLabel.setTextFill(Color.web("#6b7280"));
        systemLabel.setStyle("-fx-font-style: italic;");

        messageBox.getChildren().add(systemLabel);
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

        addSystemMessage("⚠️ Verbindung zum Server verloren");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chatty - " + title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
