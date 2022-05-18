package com.example.sceneviewtest;

import java.io.Serializable;

public class Location implements Serializable {
    private int id;
    private int map_id;
    private String name;
    private String code;
    private float x_coord;
    private float y_coord;
    private int is_deleted;

    public int getId() {
        return id;
    }

    public int getMap_id() {
        return map_id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public float getX_coord() {
        return x_coord;
    }

    public float getY_coord() {
        return y_coord;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

//    @Override
//    public String toString() {
//        return "Location{" +
//                "id=" + id +
//                ", map_id=" + map_id +
//                ", name='" + name + '\'' +
//                ", code='" + code + '\'' +
//                ", x_coord=" + x_coord +
//                ", y_coord=" + y_coord +
//                ", is_deleted=" + is_deleted +
//                '}';
//    }

    @Override
    public String toString() {
        return name;
    }
}
