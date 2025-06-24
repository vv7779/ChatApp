package com.example.chatapp.model;

import com.google.firebase.Timestamp;

public class UserModel {

    public String phone;
    public String userName;
    public Timestamp createdTimetamp;
    public String userId;
    public String fcmToken;

    public UserModel() {
    }

    public UserModel(String phone, String userName, Timestamp createdTimetamp,String userId) {
        this.phone = phone;
        this.userName = userName;
        this.createdTimetamp = createdTimetamp;
        this.userId=userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getCreatedTimetamp() {
        return createdTimetamp;
    }

    public void setCreatedTimetamp(Timestamp createdTimetamp) {
        this.createdTimetamp = createdTimetamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
