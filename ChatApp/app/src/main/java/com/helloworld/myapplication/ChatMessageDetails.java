package com.helloworld.myapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatMessageDetails implements Serializable {
    String Uid;
    String firstname;
    String Message;
    String date;
    HashMap<String,Boolean> likedUsers = new HashMap<String ,Boolean>();
    String imageUrl;
    UserCurrentLocation userCurrentLocation=null;

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMessage() {
        return Message;
    }

    public UserCurrentLocation getUserCurrentLocation() {
        return userCurrentLocation;
    }

    public void setUserCurrentLocation(UserCurrentLocation userCurrentLocation) {
        this.userCurrentLocation = userCurrentLocation;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HashMap<String,Boolean> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(HashMap<String,Boolean> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "ChatMessageDetails{" +
                "Uid='" + Uid + '\'' +
                ", firstname='" + firstname + '\'' +
                ", Message='" + Message + '\'' +
                ", date='" + date + '\'' +
                ", likedUsers=" + likedUsers +
                ", imageUrl='" + imageUrl + '\'' +
                ", userCurrentLocation='" + userCurrentLocation + '\'' +
                '}';
    }
}
