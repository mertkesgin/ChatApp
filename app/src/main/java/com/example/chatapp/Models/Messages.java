package com.example.chatapp.Models;

public class Messages {

    private String message,seen,type,from;
    private Long time;

    public Messages() {
    }

    public Messages(String message, String seen, String type, String from, Long time) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.from = from;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
