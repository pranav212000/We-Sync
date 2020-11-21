package com.example.wesync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;

public class Room implements Serializable    {
    @SerializedName("roomId")
    String roomId;
    @SerializedName("host")
    String host;
    @SerializedName("trackUri")
    String trackUri;
    @SerializedName("reSync")
    boolean reSync;
    @SerializedName("updateTime")
    Timestamp updateTime;
    @SerializedName("isPlaying")
    boolean isPlaying;
    @SerializedName("currentPosition")
    long currentPosition;
//    TODO remove members field from room class! for making it easier for serverTimestamp.
    @SerializedName("members")
    ArrayList<String> members;
    @SerializedName("repeat")
    int repeat;
    @SerializedName("lastUpdateBy")
    String lastUpdateBy;


    //REQUIRED EMPTY CONSTRUCTOR
    public Room() {
    }

    public Room(String roomId, String host, String trackUri, boolean reSync, Timestamp updateTime, boolean isPlaying, long currentPosition, ArrayList<String> members, int repeat, String lastUpdateBy) {
        this.roomId = roomId;
        this.host = host;
        this.trackUri = trackUri;
        this.reSync = reSync;
        this.updateTime = updateTime;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.members = members;
        this.repeat = repeat;
        this.lastUpdateBy = lastUpdateBy;
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

    public String getTrackUri() {
        return trackUri;
    }

    public void setTrackUri(String trackUri) {
        this.trackUri = trackUri;
    }

    public boolean isReSync() {
        return reSync;
    }

    public void setReSync(boolean reSync) {
        this.reSync = reSync;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public String getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(String lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }


//    @Override
//    public String toString() {
//        return "Room{" +
//                "roomId='" + roomId + '\'' +
//                ", host='" + host + '\'' +
//                ", trackUri='" + trackUri + '\'' +
//                ", reSync=" + reSync +
//                ", updateTime='" + updateTime + '\'' +
//                ", isPlaying=" + isPlaying +
//                ", currentPosition=" + currentPosition +
//                ", members=" + members +
//                ", repeat=" + repeat +
//                ", lastUpdateBy='" + lastUpdateBy + '\'' +
//                '}';
//    }


    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", host='" + host + '\'' +
                ", trackUri='" + trackUri + '\'' +
                ", reSync=" + reSync +
                ", updateTime=" + updateTime +
                ", isPlaying=" + isPlaying +
                ", currentPosition=" + currentPosition +
                ", members=" + members +
                ", repeat=" + repeat +
                ", lastUpdateBy='" + lastUpdateBy + '\'' +
                '}';
    }
}
