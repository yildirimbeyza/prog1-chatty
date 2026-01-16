package at.ac.hcw.chatty.service;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChatConnection {
    private static ChatConnection currentInstance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;
    private Consumer<String> onMessageReceived;
    private Runnable onDisconnected;

    private ChatConnection() {}

    public static ChatConnection getInstance() {
        if (currentInstance == null) {
            currentInstance = new ChatConnection();
        }
        return currentInstance;
    }

    // Get instance by ID (for multiple windows in same app)
    public static ChatConnection getNewInstance() {
        ChatConnection connection = new ChatConnection();
        currentInstance = connection;
        return connection;
    }

    public void connectAsClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        initializeStreams();
    }

    public void connectAsServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server gestartet auf Port " + port + " - Warte auf Verbindung...");
        socket = serverSocket.accept();
        System.out.println("Client verbunden: " + socket.getInetAddress().getHostAddress());
        initializeStreams();
        serverSocket.close();
    }

    private void initializeStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                String message;
                System.out.println("📡 Empfangs-Thread gestartet...");
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    System.out.println("📩 Nachricht empfangen: " + msg);
                    if (onMessageReceived != null) {
                        Platform.runLater(() -> onMessageReceived.accept(msg));
                    } else {
                        System.out.println("⚠️ onMessageReceived ist null!");
                    }
                }
            } catch (IOException e) {
                System.out.println("⚠️ Empfangs-Thread beendet: " + e.getMessage());
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
            System.out.println("📤 Nachricht gesendet: " + message);
        }
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

    public String getRemoteAddress() {
        return socket != null ? socket.getInetAddress().getHostAddress() : "Unknown";
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnDisconnected(Runnable callback) {
        this.onDisconnected = callback;
    }
}
