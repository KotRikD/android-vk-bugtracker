package ru.kotrik.bugtracker.Models;

/**
 * Created by kotoriku on 27.03.2018.
 */

public class ReportProductsProfile {

    public String icon_url;
    public String name_product;
    public String url_product;
    public String count_reports;
    public String url_report;

    public ReportProductsProfile(String icon, String name, String url_product, String count, String url_report) {
        this.icon_url = icon;
        this.name_product = name;
        this.url_product = url_product;
        this.count_reports = count;
        this.url_report = url_report;
    }
}
