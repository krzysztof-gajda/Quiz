package com.example.quiz;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Quiz {

    String quizTitle;
    int quizId;
    int creatorId;
    String description;
    Button startQuizButton;
    RadioButton choiceQuizButton;
    AppCompatActivity parent;

    @SerializedName("body")
    String text;


    public Quiz(QuizType qt, Button b, AppCompatActivity p){
        quizId=qt.id;
        creatorId=qt.id_creator;
        quizTitle=qt.name;
        description=qt.description;
        startQuizButton=b;
        parent=p;
        setUp(0);
    }

    public Quiz(QuizType qt, RadioButton rb,AppCompatActivity p){
        quizTitle=qt.name;
        quizId=qt.id;
        choiceQuizButton=rb;
        parent=p;
        setUp(1);
    }

    void setUp(int x){
        switch (x) {
            case 0:
            startQuizButton.setText(quizTitle);
            startQuizButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(parent, QuizActivity.class);
                    intent.putExtra("ID", quizId);
                    intent.putExtra("MULTI",0);
                    parent.startActivity(intent);
                }
            });
            break;
            case 1:
            choiceQuizButton.setText(quizTitle);
            break;
        }
    }

    static class QuizType{

        Integer id;
        Integer id_creator;
        String name;
        String description;

        @SerializedName("body")
        String text;

        QuizType(String n, String d){
            name=n;
            description=d;
        }

        QuizType(Integer i,Integer ic,String n, String d){
            id=i;
            id_creator=ic;
            name=n;
            description=d;
        }
    }
}
