package at.ac.hcw.chatty.service;

import at.ac.hcw.chatty.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ChatRoom {
    private String roomId;
    private String roomName;
    private Set<String> participants;
    private List<ChatMessage> messageHistory;
    private Consumer<ChatMessage> onMessageReceived;
    private Consumer<String> onUserJoined;
    private Consumer<String> onUserLeft;

    public ChatRoom(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.participants = new HashSet<>();
        this.messageHistory = new ArrayList<>();
    }

    public void addParticipant(String username) {
        participants.add(username);
        if (onUserJoined != null) {
            onUserJoined.accept(username);
        }
    }

    public void removeParticipant(String username) {
        participants.remove(username);
        if (onUserLeft != null && !participants.isEmpty()) {
            onUserLeft.accept(username);
        }
    }

    public void addMessage(ChatMessage message) {
        messageHistory.add(message);
        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Set<String> getParticipants() {
        return new HashSet<>(participants);
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    public void setOnMessageReceived(Consumer<ChatMessage> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnUserJoined(Consumer<String> callback) {
        this.onUserJoined = callback;
    }

    public void setOnUserLeft(Consumer<String> callback) {
        this.onUserLeft = callback;
    }
}
