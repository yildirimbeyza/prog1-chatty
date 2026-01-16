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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatConnection {
    private static ChatConnection instance;
    private Map<String, ChatRoom> chatRooms;
    private Map<String, Socket> clientConnections;
    private Map<String, PrintWriter> clientWriters;
    private ServerSocket serverSocket;
    private boolean isServer;
    private String currentUsername;

    private ChatConnection() {
        chatRooms = new ConcurrentHashMap<>();
        clientConnections = new ConcurrentHashMap<>();
        clientWriters = new ConcurrentHashMap<>();
    }

    public static ChatConnection getInstance() {
        if (instance == null) {
            instance = new ChatConnection();
        }
        return instance;
    }

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    public String getUsername() {
        return currentUsername;
    }

    // Server-Modus: Wartet auf mehrere Client-Verbindungen
    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isServer = true;

        new Thread(() -> {
            System.out.println("🟢 Server gestartet auf Port " + port);
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewClient(clientSocket);
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Fehler beim Akzeptieren von Client: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    // Client-Modus: Verbindet sich zu einem Server
    public void connectToServer(String host, int port, String roomId) throws IOException {
        Socket socket = new Socket(host, port);
        isServer = false;

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Sende JOIN-Nachricht
        out.println("JOIN|" + currentUsername + "|" + roomId);

        clientConnections.put(roomId, socket);
        clientWriters.put(roomId, out);

        // Starte Empfangs-Thread
        startClientReceiver(roomId, in);
    }

    private void handleNewClient(Socket clientSocket) {
        new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String firstMessage = in.readLine();
                if (firstMessage != null && firstMessage.startsWith("JOIN|")) {
                    String[] parts = firstMessage.split("\\|", 3);
                    String username = parts[1];
                    String roomId = parts[2];

                    String clientId = username + "_" + System.currentTimeMillis();
                    clientConnections.put(clientId, clientSocket);
                    clientWriters.put(clientId, out);

                    // Erstelle oder hole Chat-Room
                    ChatRoom room = chatRooms.computeIfAbsent(roomId,
                            id -> new ChatRoom(id, "Chat Room " + id));

                    room.addParticipant(username);

                    // Sende System-Nachricht an alle
                    broadcastToRoom(roomId, "SYSTEM|" + username + " ist dem Chat beigetreten");

                    // Empfange Nachrichten von diesem Client
                    startServerReceiver(clientId, roomId, username, in);
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Verarbeiten von Client: " + e.getMessage());
            }
        }).start();
    }

    private void startClientReceiver(String roomId, BufferedReader in) {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(roomId, message);
                }
            } catch (IOException e) {
                System.err.println("Verbindung zu Server verloren");
                handleDisconnect(roomId);
            }
        }).start();
    }

    private void startServerReceiver(String clientId, String roomId, String username, BufferedReader in) {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // Leite Nachricht an alle anderen Clients weiter
                    if (message.startsWith("MSG|") || message.startsWith("STICKER|")) {
                        broadcastToRoom(roomId, message);
                    }
                }
            } catch (IOException e) {
                System.err.println(username + " hat Verbindung verloren");
            } finally {
                // Client hat sich getrennt
                clientConnections.remove(clientId);
                clientWriters.remove(clientId);

                ChatRoom room = chatRooms.get(roomId);
                if (room != null) {
                    room.removeParticipant(username);
                    if (room.getParticipantCount() == 0) {
                        chatRooms.remove(roomId);
                    } else {
                        broadcastToRoom(roomId, "SYSTEM|" + username + " hat den Chat verlassen");
                    }
                }
            }
        }).start();
    }

    private void processMessage(String roomId, String message) {
        ChatRoom room = chatRooms.get(roomId);
        if (room == null) {
            room = new ChatRoom(roomId, "Chat Room " + roomId);
            chatRooms.put(roomId, room);
        }

        if (message.startsWith("MSG|")) {
            String[] parts = message.split("\\|", 3);
            String sender = parts[1];
            String content = parts[2];

            ChatMessage chatMsg = new ChatMessage(content, sender, ChatMessage.MessageType.TEXT);
            room.addMessage(chatMsg);

        } else if (message.startsWith("STICKER|")) {
            String[] parts = message.split("\\|", 3);
            String sender = parts[1];
            String sticker = parts[2];

            ChatMessage chatMsg = new ChatMessage(sticker, sender, ChatMessage.MessageType.STICKER);
            room.addMessage(chatMsg);

        } else if (message.startsWith("SYSTEM|")) {
            String content = message.substring(7);
            ChatMessage chatMsg = new ChatMessage(content, "System", ChatMessage.MessageType.SYSTEM);
            room.addMessage(chatMsg);
        }
    }

    private void broadcastToRoom(String roomId, String message) {
        for (Map.Entry<String, PrintWriter> entry : clientWriters.entrySet()) {
            if (entry.getKey().contains(roomId) || isServer) {
                entry.getValue().println(message);
            }
        }

        // Verarbeite auch lokal
        processMessage(roomId, message);
    }

    public void sendMessage(String roomId, String message) {
        String formattedMsg = "MSG|" + currentUsername + "|" + message;

        if (isServer) {
            broadcastToRoom(roomId, formattedMsg);
        } else {
            PrintWriter out = clientWriters.get(roomId);
            if (out != null) {
                out.println(formattedMsg);
            }
        }

        // Füge eigene Nachricht lokal hinzu
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            ChatMessage chatMsg = new ChatMessage(message, currentUsername, ChatMessage.MessageType.TEXT);
            room.addMessage(chatMsg);
        }
    }

    public void sendSticker(String roomId, String sticker) {
        String formattedMsg = "STICKER|" + currentUsername + "|" + sticker;

        if (isServer) {
            broadcastToRoom(roomId, formattedMsg);
        } else {
            PrintWriter out = clientWriters.get(roomId);
            if (out != null) {
                out.println(formattedMsg);
            }
        }

        // Füge eigenen Sticker lokal hinzu
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            ChatMessage chatMsg = new ChatMessage(sticker, currentUsername, ChatMessage.MessageType.STICKER);
            room.addMessage(chatMsg);
        }
    }

    public ChatRoom getOrCreateRoom(String roomId, String roomName) {
        return chatRooms.computeIfAbsent(roomId, id -> new ChatRoom(id, roomName));
    }

    public ChatRoom getRoom(String roomId) {
        return chatRooms.get(roomId);
    }

    public boolean isServer() {
        return isServer;
    }

    private void handleDisconnect(String roomId) {
        Platform.runLater(() -> {
            // Benachrichtige UI über Verbindungsabbruch
        });
    }

    public void disconnect(String roomId) {
        Socket socket = clientConnections.get(roomId);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientConnections.remove(roomId);
            clientWriters.remove(roomId);
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (Socket socket : clientConnections.values()) {
                socket.close();
            }
            clientConnections.clear();
            clientWriters.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
