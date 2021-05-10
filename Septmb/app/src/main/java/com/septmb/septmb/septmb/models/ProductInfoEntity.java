package com.septmb.septmb.septmb.models;

/**
 * Created by sonback123456 on 9/18/2017.
 */

public class ProductInfoEntity {
    String idx="0";
    String user_id="";
    String title="";
    String brand="";
    String gender="";
    String keyword="";
    String price="";
    String category="";
    String seller="";
    String description="";
    String image_url="";

    public ProductInfoEntity(){

    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdx() {
        return idx;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getTitle() {
        return title;
    }

    public String getGender() {
        return gender;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getSeller() {
        return seller;
    }

    public String getDescription() {
        return description;
    }
}
