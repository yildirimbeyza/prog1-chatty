package at.ac.hcw.chatty.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private final String content;
    private final String sender;
    private final LocalTime timestamp;
    private final boolean isSticker;
    private final MessageType type;

    public enum MessageType {
        TEXT, STICKER, SYSTEM
    }

    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalTime.now();
        this.type = type;
        this.isSticker = (type == MessageType.STICKER);
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isSticker() {
        return isSticker;
    }

    public MessageType getType() {
        return type;
    }
}
