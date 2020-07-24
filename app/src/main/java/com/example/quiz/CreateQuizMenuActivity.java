package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreateQuizMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz_menu);

        Button buttonMyQuizzes = findViewById(R.id.button_my_quizes);
        buttonMyQuizzes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myQuizzes();
            }
        });

        Button buttonStartCreateQuiz = findViewById(R.id.button_start_create_quiz);
        buttonStartCreateQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateQuiz();
            }
        });
    }

    private void myQuizzes(){
        Intent intent = new Intent(CreateQuizMenuActivity.this, MyQuizActivity.class);
        startActivity(intent);
    }

    private void startCreateQuiz(){
        Intent intent = new Intent(CreateQuizMenuActivity.this, CreateQuizActivity.class);
        intent.putExtra("PURPOSE",0);
        startActivity(intent);
    }
}
