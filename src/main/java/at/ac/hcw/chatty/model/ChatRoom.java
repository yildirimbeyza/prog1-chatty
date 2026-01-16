package at.ac.hcw.chatty.model;

import at.ac.hcw.chatty.service.ClientHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {
    private String name;
    private List<ClientHandler> members;
    private List<ChatMessage> messageHistory;
    private Date createdAt;

    public ChatRoom(String name) {
        this.name = name;
        this.members = new CopyOnWriteArrayList<>();
        this.messageHistory = new ArrayList<>();
        this.createdAt = new Date();
    }

    public void addMember(ClientHandler member) {
        members.add(member);
    }

    public void removeMember(ClientHandler member) {
        members.remove(member);
    }

    public void broadcast(String message, ClientHandler sender) {
        ChatMessage chatMsg = new ChatMessage(message, false);
        messageHistory.add(chatMsg);

        for (ClientHandler member : members) {
            if (member != sender) {
                member.sendMessage(message);
            }
        }
    }

    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    public int getMemberCount() {
        return members.size();
    }

    public String getName() {
        return name;
    }

    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler member : members) {
            names.add(member.getUsername());
        }
        return names;
    }
}
