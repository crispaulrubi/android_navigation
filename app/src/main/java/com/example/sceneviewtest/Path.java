package com.example.sceneviewtest;

import java.io.Serializable;

public class Path implements Serializable {

    protected String start;
    protected String end;
    protected int id;
    protected int map_id;
    protected String unique_path_id;
    protected int start_location;
    protected int end_location;
    protected float x_coord;
    protected float y_coord;
    protected int coord_order;
    protected int is_deleted;

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public int getId() {
        return id;
    }

    public int getMap_id() {
        return map_id;
    }

    public String getUnique_path_id() {
        return unique_path_id;
    }

    public int getStart_location() {
        return start_location;
    }

    public int getEnd_location() {
        return end_location;
    }

    public float getX_coord() {
        return x_coord;
    }

    public float getY_coord() {
        return y_coord;
    }

    public int getCoord_order() {
        return coord_order;
    }

    @Override
    public String toString() {
        return "Path{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", id=" + id +
                ", map_id=" + map_id +
                ", unique_path_id='" + unique_path_id + '\'' +
                ", start_location=" + start_location +
                ", end_location=" + end_location +
                ", x_coord=" + x_coord +
                ", y_coord=" + y_coord +
                ", coord_order=" + coord_order +
                '}';
    }
}
