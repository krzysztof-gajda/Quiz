package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class SingleplayerActivity extends AppCompatActivity {

    ArrayList<Quiz> quizList=new ArrayList<>();
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleplayer);

        linearLayout = (LinearLayout) findViewById(R.id.linear_quiz_buttons);

        loadQuizzes();
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
                    Log.d("Server gQuiz1","Code: "+response.code());
                    Log.d("Server gQuiz1","Code: "+response.headers());
                    return;
                }

                Log.d("Server gQuiz1","got Body");
                ArrayList<Quiz.QuizType> quizzes=response.body();
                for(Quiz.QuizType q:quizzes){
                    quizList.add(new Quiz(q,new Button(SingleplayerActivity.this),SingleplayerActivity.this));
                }
                printQuizzes();
            }

            @Override
            public void onFailure(Call<ArrayList<Quiz.QuizType>> call, Throwable t) {
                Log.d("Server gQuiz1",t.getMessage());
            }
        });
    }

    private void printQuizzes(){
        for(Quiz q:quizList){
            linearLayout.addView(q.startQuizButton);
        }
    }

}
