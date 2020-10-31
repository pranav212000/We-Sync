package com.example.wesync;

import java.util.ArrayList;

public class Room {
    String roomId;
    String host;
    String songUrl;
    boolean reSync;
    String currentTime;
    ArrayList<User> members;

//REQUIRED EMPTY CONSTRUCTOR
    public Room() {
    }

    public Room(String roomId, String host, String songUrl, boolean reSync, String currentTime, ArrayList<User> members) {
        this.roomId = roomId;
        this.host = host;
        this.songUrl = songUrl;
        this.reSync = reSync;
        this.currentTime = currentTime;
        this.members = members;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public boolean isReSync() {
        return reSync;
    }

    public void setReSync(boolean reSync) {
        this.reSync = reSync;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
