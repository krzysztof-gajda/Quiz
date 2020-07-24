package com.example.quiz;

import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class Friend{
    String name;
    int friendId;
    RadioButton choiceFriendButton;
    AppCompatActivity parent;

    public Friend(String n,int id, RadioButton rg,AppCompatActivity p){
        name=n;
        friendId=id;
        choiceFriendButton=rg;
        parent=p;
        setUp();
    }

    void setUp(){
        choiceFriendButton.setText(name);
    }

}
