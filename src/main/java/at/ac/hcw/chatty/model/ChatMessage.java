package at.ac.hcw.chatty.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String content;
    private LocalTime timestamp;
    private boolean isSent;
    private MessageType type;

    public enum MessageType {
        TEXT, STICKER, SYSTEM
    }

    public ChatMessage(String content, boolean isSent) {
        this(content, isSent, MessageType.TEXT);
    }

    public ChatMessage(String content, boolean isSent, MessageType type) {
        this.content = content;
        this.timestamp = LocalTime.now();
        this.isSent = isSent;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isSent() {
        return isSent;
    }

    public MessageType getType() {
        return type;
    }

    public boolean isSticker() {
        return type == MessageType.STICKER;
    }
}
