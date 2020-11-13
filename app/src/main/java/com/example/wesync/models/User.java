package com.example.wesync.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("email")
    String email;
    @SerializedName("name")
    String name;
    @SerializedName("userName")
    String userName;

    //required constructor
    public User() {
    }

    public User(String email, String name, String userName) {
        this.email = email;
        this.name = name;
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
