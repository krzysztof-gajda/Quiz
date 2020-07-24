package com.example.quiz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button buttonSingleplayerQuiz = findViewById(R.id.button_singleplayer);
        buttonSingleplayerQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizList();
            }
        });

        Button buttonMultiplayerQuiz = findViewById(R.id.button_multiplayer);
        buttonMultiplayerQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiplayer();
            }
        });

        Button buttonStartCreate = findViewById(R.id.button_create_quiz);
        buttonStartCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuiz();
            }
        });
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Aplikacja");
        dlgAlert.setMessage("Wylogować?");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                    }});
        dlgAlert.setNegativeButton("NIE",null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void quizList(){
        Intent intent = new Intent(MenuActivity.this, SingleplayerActivity.class);
        startActivity(intent);
    }

    private void multiplayer(){
        Intent intent = new Intent(MenuActivity.this, MultiplayerActivity.class);
        startActivity(intent);
    }

    private void createQuiz(){
        Intent intent = new Intent(MenuActivity.this, CreateQuizMenuActivity.class);
        startActivity(intent);
    }

    private void logOut(){
        Log.d("TAG","wylogowano");
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ServerConnection.Token> call=jsonPlaceHolderApi.logout();
        Log.d("REQUEST",call.request().toString());

        call.enqueue(new Callback<ServerConnection.Token>() {
            @Override
            public void onResponse(Call<ServerConnection.Token> call, Response<ServerConnection.Token> response) {
                if(!response.isSuccessful()){
                    Log.d("Server pLogin","Code: "+response.toString());
                    Log.d("Server pLogin","Code: "+response.headers());
                    logoutBad();
                    return;
                }

                Log.d("RESPONSE",response.message());
                logoutGood();
                return;
            }

            @Override
            public void onFailure(Call<ServerConnection.Token> call, Throwable t) {
                Log.d("Server pLogin",t.getMessage());
                logoutBad();
                return;
            }
        });
    }

    private void logoutGood(){
        MainActivity.server.token=null;
        MainActivity.server.username=null;
        this.finish();
    }

    private void logoutBad(){

    }
}