package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity {

    ArrayList<TextView> questionList=new ArrayList<>();
    ArrayList<String> results=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        LinearLayout linearLayout=findViewById(R.id.linear_questions);

        TextView punktacja=findViewById(R.id.text_score);

        Button buttonConfirm=findViewById(R.id.button_ok);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeQuiz();
            }
        });

        results=getIntent().getStringArrayListExtra("result");
        punktacja.setText("Punktacja: "+getIntent().getStringExtra("score"));
        loadQuestions();
        printQuestions(linearLayout);

    }

    void loadQuestions(){
        int index=0;
        for(String s:results) {
            questionList.add(new TextView(this));
            questionList.get(index).setText(s);
            index++;
        }

    }

    void printQuestions(LinearLayout ll){
        for(TextView t :questionList)
            ll.addView(t);

    }

    void closeQuiz(){
        this.finish();
    }
}
