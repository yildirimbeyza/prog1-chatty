package at.ac.hcw.chatty.service;

import at.ac.hcw.chatty.model.ChatRoom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ChatRoom room;
    private String username;

    public ClientHandler(Socket socket, ChatRoom room, String username,
                         BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.room = room;
        this.username = username;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📨 [" + room.getName() + "] " + username + ": " + message);

                String formattedMessage;
                if (message.startsWith("STICKER:")) {
                    formattedMessage = username + " sent: " + message;
                } else {
                    formattedMessage = username + ": " + message;
                }

                room.broadcast(formattedMessage, this);
            }
        } catch (IOException e) {
            System.out.println("⚠️ " + username + " disconnected from " + room.getName());
        } finally {
            cleanup();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    private void cleanup() {
        try {
            room.removeMember(this);
            room.broadcast("<<< " + username + " hat den Raum verlassen", null);
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
