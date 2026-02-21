package com.eogd.gensokyo.data;

public class Mail {

    private final String sender;
    private final String content;
    private final long timestamp;

    public Mail(String sender, String content, long timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}