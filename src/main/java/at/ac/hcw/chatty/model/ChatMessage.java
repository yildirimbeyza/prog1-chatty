package at.ac.hcw.chatty.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private final String content;
    private final LocalTime timestamp;
    private final boolean isSent;

    public ChatMessage(String content, boolean isSent) {
        this.content = content;
        this.timestamp = LocalTime.now();
        this.isSent = isSent;
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
}
