package at.ac.hcw.chatty.service;

import at.ac.hcw.chatty.model.ChatMessage;
import at.ac.hcw.chatty.model.ChatRoom;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatServer {
    private static ChatServer instance;
    private ServerSocket serverSocket;
    private Map<String, ChatRoom> rooms;
    private boolean running;
    private Consumer<String> onLog;
    private Thread serverThread;
    private int port;

    private ChatServer() {
        rooms = new ConcurrentHashMap<>();
    }

    public static ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer();
        }
        return instance;
    }

    public void start(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        running = true;

        log("🟢 Chat-Server gestartet auf Port " + port);

        serverThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientConnectionHandler(clientSocket)).start();
                } catch (IOException e) {
                    if (running) {
                        log("❌ Fehler beim Akzeptieren: " + e.getMessage());
                    }
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public ChatRoom getOrCreateRoom(String roomName) {
        return rooms.computeIfAbsent(roomName, ChatRoom::new);
    }

    public List<String> getRoomList() {
        List<String> roomList = new ArrayList<>();
        for (Map.Entry<String, ChatRoom> entry : rooms.entrySet()) {
            roomList.add(entry.getKey() + " (" + entry.getValue().getMemberCount() + " users)");
        }
        return roomList;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnLog(Consumer<String> callback) {
        this.onLog = callback;
    }

    private void log(String message) {
        System.out.println(message);
        if (onLog != null) {
            Platform.runLater(() -> onLog.accept(message));
        }
    }

    // Handles initial connection and room joining
    private class ClientConnectionHandler implements Runnable {
        private Socket socket;

        public ClientConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Read JOIN command: "JOIN:roomName:username"
                String joinCommand = in.readLine();
                if (joinCommand != null && joinCommand.startsWith("JOIN:")) {
                    String[] parts = joinCommand.split(":", 3);
                    String roomName = parts[1];
                    String username = parts[2];

                    ChatRoom room = getOrCreateRoom(roomName);
                    ClientHandler handler = new ClientHandler(socket, room, username, in, out);
                    room.addMember(handler);

                    log("✅ " + username + " joined room: " + roomName);
                    room.broadcast(">>> " + username + " ist beigetreten", null);

                    // Send message history
                    for (ChatMessage msg : room.getMessageHistory()) {
                        out.println(msg.getContent());
                    }

                    handler.start();
                }
            } catch (IOException e) {
                log("❌ Connection error: " + e.getMessage());
            }
        }
    }

    public int getPort() {
        return port;
    }
}
