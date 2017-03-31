package com.cyr.chatogo.Models;

import java.io.Serializable;

/**
 * Created by roger on 3/10/2017.
 */
public class Message implements Serializable {
    private int id;
    private String owner;
    private String username;
    private String chatname;
    private String message;
    private String date;

    public Message(){};

    public Message(int id,String owner, String chatname, String username, String message, String date) {
        this.id = id;
        this.owner = owner;
        this.username = username;
        this.chatname = chatname;
        this.message = message;
        this.date = date;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getChatname() {
        return chatname;
    }

    public void setChatname(String chatname) {
        this.chatname = chatname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
