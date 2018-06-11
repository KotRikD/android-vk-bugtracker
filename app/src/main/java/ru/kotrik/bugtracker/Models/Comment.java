package ru.kotrik.bugtracker.Models;

/**
 * Created by kotoriku on 23.03.2018.
 */

public class Comment {

    public String name;
    public String desc;
    public String time;
    public String url_avatar;

    public Comment(String name, String desc, String time, String url_avatar) {
        this.name = name;
        this.desc = desc;
        this.time = time;
        this.url_avatar = url_avatar;
    }
}
