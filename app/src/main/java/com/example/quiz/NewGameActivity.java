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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class NewGameActivity extends AppCompatActivity {

    ArrayList<Quiz> quizList=new ArrayList<>();
    RadioGroup radioGroup1;
    int quizId;
    Button buttonCreateGame;
    boolean waiting=false;

    @Override
    public void onBackPressed(){
        if(!waiting)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        radioGroup1 = (RadioGroup) findViewById(R.id.rg1);

        MainActivity.server.newGameWindow=this;

        buttonCreateGame = findViewById(R.id.button_create_game);
        buttonCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = new EditText(NewGameActivity.this);
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(NewGameActivity.this);

                dlgAlert.setTitle("Zaproś do quizu");
                dlgAlert.setMessage("Podaj nazwę użytkownika");
                dlgAlert.setView(input);
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String friend=input.getText().toString();
                                if(friend!=""){
                                    for(Quiz q: quizList)
                                        if(q.choiceQuizButton.isChecked())
                                            quizId=q.quizId;

                                    sendInvitations(friend);
                                    buttonCreateGame.setEnabled(false);
                                    waiting=true;

                                }
                            }});
                dlgAlert.setNegativeButton("Anuluj",null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });

        loadQuizzes();
        printQuizzes();
    }

    private void loadQuizzes(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Quiz.QuizType>> call=jsonPlaceHolderApi.getQuizzes("Token "+MainActivity.server.token.key,null,null);
        call.enqueue(new Callback<ArrayList<Quiz.QuizType>>() {
            @Override
            public void onResponse(Call<ArrayList<Quiz.QuizType>> call, Response<ArrayList<Quiz.QuizType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server gQuiz2","Code: "+response.code());
                    Log.d("Server gQuiz2","Code: "+response.headers());
                    return;
                }

                Log.d("Server gQuiz2","got Body");
                ArrayList<Quiz.QuizType> quizzes=response.body();
                for(Quiz.QuizType q:quizzes){
                    quizList.add(new Quiz(q,new RadioButton(NewGameActivity.this),NewGameActivity.this));
                }
                printQuizzes();
            }

            @Override
            public void onFailure(Call<ArrayList<Quiz.QuizType>> call, Throwable t) {
                Log.d("Server gQuiz2",t.getMessage());
            }
        });
    }

    private void printQuizzes(){
        for(Quiz q:quizList)
            radioGroup1.addView(q.choiceQuizButton);
    }

    private void sendInvitations(String userName){
        String message;
        message="{\"type\": \"invitation\", \"user\": \""+userName+"\", \"quiz\": "+quizId+"}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message);
    }

    void createGame(){
        buttonCreateGame.setEnabled(true);
        waiting=false;
        Intent intent = new Intent(NewGameActivity.this, QuizActivity.class);
        intent.putExtra("QuizID", quizId);
        intent.putExtra("MULTI",1);
        startActivity(intent);
    }

    void printMessage(String message){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(NewGameActivity.this);

                dlgAlert.setTitle("Zaproszenie");
                dlgAlert.setMessage(message);
                dlgAlert.setPositiveButton("Ok", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                buttonCreateGame.setEnabled(true);
                waiting=false;
            }
        });
    }
}
