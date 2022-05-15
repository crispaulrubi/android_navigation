package com.example.sceneviewtest;

public class Path {

    protected String start;
    protected String end;
    protected int id;
    protected int mapID;
    protected String uniquePathID;
    protected int startLocation;
    protected int endLocation;
    protected float xCoord;
    protected float yCoord;
    protected int coordOrder;
    protected boolean isDeleted;

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

    public int getMapID() {
        return mapID;
    }

    public void setMapID(int mapID) {
        this.mapID = mapID;
    }

    public String getUniquePathID() {
        return uniquePathID;
    }

    public void setUniquePathID(String uniquePathID) {
        this.uniquePathID = uniquePathID;
    }

    public int getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(int startLocation) {
        this.startLocation = startLocation;
    }

    public int getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(int endLocation) {
        this.endLocation = endLocation;
    }

    public float getxCoord() {
        return xCoord;
    }

    public void setxCoord(float xCoord) {
        this.xCoord = xCoord;
    }

    public float getyCoord() {
        return yCoord;
    }

    public void setyCoord(float yCoord) {
        this.yCoord = yCoord;
    }

    public int getCoordOrder() {
        return coordOrder;
    }

    public void setCoordOrder(int coordOrder) {
        this.coordOrder = coordOrder;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
