package com.helloworld.myapplication;

import java.io.Serializable;

public class UserProfile implements Serializable {
    String firstName;
    String uid;
    String lastName;
    String gender;
    String city;
    String email;
    String profileImage;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUid() {
        return uid;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getCity() {
        return city;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public UserProfile(){

    }

    public UserProfile(String firstName, String lastName, String gender, String email, String city, String profileImage, String uid) {
        this.firstName = firstName;
        this.uid = uid;
        this.lastName = lastName;
        this.gender = gender;
        this.city = city;
        this.email = email;
        this.profileImage = profileImage;
    }


}
