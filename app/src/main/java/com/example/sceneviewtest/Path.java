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

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMap_id() {
        return map_id;
    }

    public void setMap_id(int map_id) {
        this.map_id = map_id;
    }

    public String getUnique_path_id() {
        return unique_path_id;
    }

    public void setUnique_path_id(String unique_path_id) {
        this.unique_path_id = unique_path_id;
    }

    public int getStart_location() {
        return start_location;
    }

    public void setStart_location(int start_location) {
        this.start_location = start_location;
    }

    public int getEnd_location() {
        return end_location;
    }

    public void setEnd_location(int end_location) {
        this.end_location = end_location;
    }

    public float getX_coord() {
        return x_coord;
    }

    public void setX_coord(float x_coord) {
        this.x_coord = x_coord;
    }

    public float getY_coord() {
        return y_coord;
    }

    public void setY_coord(float y_coord) {
        this.y_coord = y_coord;
    }

    public int getCoord_order() {
        return coord_order;
    }

    public void setCoord_order(int coord_order) {
        this.coord_order = coord_order;
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
