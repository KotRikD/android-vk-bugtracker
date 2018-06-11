package ru.kotrik.bugtracker.Models;

public class NewItem {

    public int id;
    public String name;
    public boolean activated;

    public NewItem(int id, String name, boolean activated) {
        this.id = id;
        this.name = name;
        this.activated = activated;
    }

}
