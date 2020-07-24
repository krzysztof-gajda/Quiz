package com.example.quiz;

import com.google.gson.annotations.SerializedName;

public class FriendLogin{
    Integer id;
    String username;
    String password;

    @SerializedName("body")
    String text;

    public FriendLogin(String n,String p){
        username=n;
        password=p;
    }
}
