package com.example.chatapp.Models;

public class Friends {

    private String date;
    private String friendImage;
    private String friendName;
    private String userID;
    private String friendID;

    public Friends() {
    }

    public Friends(String date, String friendImage, String friendName, String userID, String friendID) {
        this.date = date;
        this.friendImage = friendImage;
        this.friendName = friendName;
        this.userID = userID;
        this.friendID = friendID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFriendImage() {
        return friendImage;
    }

    public void setFriendImage(String friendImage) {
        this.friendImage = friendImage;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }
}
