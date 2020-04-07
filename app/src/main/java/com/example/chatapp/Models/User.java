package com.example.chatapp.Models;


public class User {

    private String displayName;
    private String status;
    private String image;
    private String userID;
    private String tokenID;

    public User() {
    }

    public User(String displayName, String status, String image, String userID) {
        this.displayName = displayName;
        this.status = status;
        this.image = image;
        this.userID = userID;
    }

    public User(String displayName, String status, String image, String userID, String tokenID) {
        this.displayName = displayName;
        this.status = status;
        this.image = image;
        this.userID = userID;
        this.tokenID = tokenID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }
}
