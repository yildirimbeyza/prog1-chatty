package at.ac.hcw.chatty.service;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;
    private Consumer<String> onMessageReceived;
    private Runnable onDisconnected;
    private String username;
    private String roomName;

    public void connect(String host, int port, String roomName, String username) throws IOException {
        this.roomName = roomName;
        this.username = username;

        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send JOIN command
        out.println("JOIN:" + roomName + ":" + username);

        System.out.println("✅ Connected to " + roomName + " as " + username);
    }

    public void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                String message;
                System.out.println("📡 Empfangs-Thread gestartet für " + roomName);
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    System.out.println("📩 [" + roomName + "] " + msg);
                    if (onMessageReceived != null) {
                        Platform.runLater(() -> onMessageReceived.accept(msg));
                    }
                }
            } catch (IOException e) {
                System.out.println("⚠️ Disconnected from " + roomName);
                if (onDisconnected != null) {
                    Platform.runLater(() -> onDisconnected.run());
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void sendMessage(String message) {
        if (out != null && !message.trim().isEmpty()) {
            out.println(message);
            System.out.println("📤 [" + roomName + "] Sent: " + message);
        }
    }

    public void sendSticker(String stickerEmoji) {
        sendMessage("STICKER:" + stickerEmoji);
    }

    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            if (receiveThread != null) receiveThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnDisconnected(Runnable callback) {
        this.onDisconnected = callback;
    }

    public String getUsername() {
        return username;
    }

    public String getRoomName() {
        return roomName;
    }
}
