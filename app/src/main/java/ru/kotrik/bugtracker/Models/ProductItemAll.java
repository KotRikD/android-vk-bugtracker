package ru.kotrik.bugtracker.Models;


public class ProductItemAll {

    public String id;
    public String img;
    public String name;

    public boolean isRequest;
    public String btHash;

    public ProductItemAll(String id, String img, String name, boolean isRequest, String hash) {
        this.id = id;
        this.img = img;
        this.isRequest = isRequest;
        this.btHash = hash;
        this.name = name;
    }

    public void setHash(String Hash) {
        this.btHash = Hash;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

}
