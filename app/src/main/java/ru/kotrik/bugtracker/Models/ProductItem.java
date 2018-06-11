package ru.kotrik.bugtracker.Models;

public class ProductItem {

    public String image_url;
    public String name_product;
    public String count_of_reports;
    public String product_url;
    public String version;

    public ProductItem(String url, String name, String count, String version, String product_url) {
        this.image_url = url;
        this.name_product = name;
        this.count_of_reports = count;
        this.version = version;
        this.product_url = product_url;
    }

}
