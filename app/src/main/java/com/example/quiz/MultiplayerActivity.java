package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MultiplayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        Button buttonCreate = findViewById(R.id.button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create();
            }
        });

        Button buttonShowInvitations = findViewById(R.id.button_show_invitation);
        buttonShowInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInvitations();
            }
        });
    }

    private void create(){
        Intent intent = new Intent(MultiplayerActivity.this, NewGameActivity.class);
        startActivity(intent);
    }

    private void showInvitations(){
        Intent intent = new Intent(MultiplayerActivity.this, InvitationActivity.class);
        startActivity(intent);
    }
}
