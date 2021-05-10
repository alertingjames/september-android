package com.septmb.septmb.septmb.models;

/**
 * Created by sonback123456 on 9/18/2017.
 */

public class UserEntity {
    String idx="0";
    String name="";
    String email="";
    String password="";
    String photoUrl="";
    String messages="";
    String date="";
    String phone="";

    public UserEntity(){

    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getMessages() {
        return messages;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdx() {
        return idx;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
