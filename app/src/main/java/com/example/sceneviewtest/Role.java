package com.example.sceneviewtest;

import androidx.annotation.NonNull;

public class Role {
    private int id;
    private String title;
    private int is_deleted;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

    @NonNull
    @Override
    public String toString() {
        return (title != null && !title.equals("")) ?
                title.substring(0, 1).toUpperCase() + title.substring(1) : "";
    }
}
