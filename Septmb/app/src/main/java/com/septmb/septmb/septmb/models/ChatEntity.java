package com.septmb.septmb.septmb.models;

/**
 * Created by sonback123456 on 7/19/2017.
 */

public class ChatEntity {
    int _chid = 0;
    String image = "";
    String istyping = "";
    String lat = "";
    String lon = "";
    String message = "";
    String online = "";
    String time = "";
    String user = "";
    String video = "";
    String photo = "";

    public ChatEntity(){

    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public void set_chid(int _chid) {
        this._chid = _chid;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIstyping(String istyping) {
        this.istyping = istyping;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public int get_chid() {
        return _chid;
    }

    public String getImage() {
        return image;
    }

    public String getIstyping() {
        return istyping;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getMessage() {
        return message;
    }

    public String getOnline() {
        return online;
    }

    public String getTime() {
        return time;
    }

    public String getUser() {
        return user;
    }

    public String getVideo() {
        return video;
    }
}
