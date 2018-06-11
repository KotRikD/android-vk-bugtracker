package ru.kotrik.bugtracker.Models;

import java.util.ArrayList;

/**
 * Created by kotoriku on 21.03.2018.
 */

public class Bug {

    public String name;
    public String time;
    public String status;
    public String link;
    public ArrayList<String> tags;

    public Bug(String text, String time, String type, ArrayList<String> tags, String link) {
        this.name = text;
        this.time = time;
        this.status = type;
        this.tags = tags;
        this.link = link;
    }
}
