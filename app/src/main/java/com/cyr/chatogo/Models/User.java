package com.cyr.chatogo.Models;

import java.io.Serializable;

/**
 * Created by roger on 3/10/2017.
 */
public class User implements Serializable {
    private int id;
    private String status;
    private String username;
    private String location;
    private String image;

    public User(){};

    public User(int id, String status, String username, String location, String image) {
        this.id = id;
        this.status = status;
        this.username = username;
        this.location = location;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

