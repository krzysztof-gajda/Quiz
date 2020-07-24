package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;

public class InvitationActivity extends AppCompatActivity {

    ArrayList<Invitation> invitations=new ArrayList<>();
    LinearLayout linearLayout;

    @Override
    public void onBackPressed(){
        MainActivity.server.invitationWindow=null;
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        linearLayout = (LinearLayout) findViewById(R.id.linear_invitation_buttons);
        MainActivity.server.invitationWindow=this;
        loadInvitations();
        printInvitations();

    }

    void loadInvitations(){
        ArrayList<String> players=MainActivity.server.invitations;
        Date now=new Date();
        invitations.clear();
        invitations.add(new Invitation("user2", new Button(this), this));
        for(String p: players) {
            String data[]=p.split(":");
            if(Long.getLong(data[1])-now.getTime()>15000)
            invitations.add(new Invitation(data[0], new Button(this), this));
        }
    }

    void printInvitations(){
        linearLayout.removeAllViews();
        for(Invitation i: invitations)
            linearLayout.addView(i.acceptInvitation);
    }


    class Invitation{
        String playerName;
        Button acceptInvitation;

        Invitation(String name, Button b, final AppCompatActivity parent){
            playerName=name;
            acceptInvitation=b;

            b.setText(playerName);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =new Intent(parent,QuizActivity.class);
                    parent.startActivity(intent);
                }
            });
        }
    }
}
