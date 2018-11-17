package com.appu.gaayathri.bookoman.model;

import java.util.HashMap;
import java.util.Map;

public class Note {

    private String id;
    private String title;
    private String author;
    private String degree;
    private String specialization;
    private String location;
    private String mrp;
    private String price;
    private String entryName;
    private String user;
    private String uid;
    private String sellerMsg;
    private String downloadUri;
    private String email;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public String getSellerMsg() {
        return sellerMsg;
    }

    public void setSellerMsg(String sellerMsg) {
        this.sellerMsg = sellerMsg;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Note() {
    }

    public Note(String id, String title, String author, String degree, String specialization, String location, String mrp, String price, String entryName, String user, String uid, String sellerMsg, String email, String phone) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.degree = degree;
        this.specialization = specialization;
        this.location = location;
        this.mrp = mrp;
        this.price = price;
        this.entryName = entryName;
        this.user = user;
        this.uid = uid;
        this.sellerMsg = sellerMsg;
        this.downloadUri = downloadUri;
        this.email = email;
        this.phone = phone;
    }


    public Note(String title, String author, String degree, String specialization, String location, String mrp, String price, String entryName, String user, String uid, String sellerMsg, String email, String phone) {
        this.title = title;
        this.author = author;
        this.degree = degree;
        this.specialization = specialization;
        this.location = location;
        this.mrp = mrp;
        this.price = price;
        this.entryName = entryName;
        this.user = user;
        this.sellerMsg = sellerMsg;
        this.downloadUri = downloadUri;
        this.uid = uid;
        this.email = email;
        this.phone = phone;
    }

    public String getuser() {
        return user;
    }

    public void setuser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }



    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("title", this.title);
        result.put("author", this.author);
        result.put("degree", this.degree);
        result.put("specialization", this.specialization);
        result.put("location", this.location);
        result.put("mrp", this.mrp);
        result.put("price", this.price);
        result.put("id", this.id);
        result.put("entryName", this.entryName);
        result.put("user", this.user);
        result.put("uid", this.uid);
        result.put("sellerMsg", this.sellerMsg);
        result.put("downloadUri", this.downloadUri);
        result.put("email", this.email);
        result.put("phone", this.phone);

        return result;
    }
}
