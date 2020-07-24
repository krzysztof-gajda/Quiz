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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CreateQuizActivity extends AppCompatActivity {

    ArrayList<QuestionPanel> listOfQuestions = new ArrayList();
    LinearLayout linearLayout1;
    TextView nameText;
    TextView desText;
    int purpose;
    Quiz.QuizType quiz;
    int quizId;
    boolean isBuild=false;

    @Override
    public void onResume() {

        Log.d("APP","Resume");
        if(isBuild)
            loadQuestions();
        super.onResume();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Wychodzenie");
        dlgAlert.setMessage("Niezapisane zmiany zostaną utracone");
        dlgAlert.setPositiveButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dlgAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CreateQuizActivity.this.finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        linearLayout1 = (LinearLayout) findViewById(R.id.linear_questions);
        nameText=findViewById(R.id.edit_text_quiz_name);
        desText=findViewById(R.id.edit_text_description);

        Button buttonNewQuestion=findViewById(R.id.button_new_question);
        buttonNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newQuestion();
            }
        });

        Button buttonEnd=findViewById(R.id.button_finalize_quiz);
        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity();
            }
        });

        purpose=getIntent().getIntExtra("PURPOSE",0);
        buttonEnd.setText("Zapisz");
        switch (purpose){
            case 0:

                break;
            case 1:
                getQuizData();
                break;
        }
    }

    private void newQuestion(){
        if(quiz==null)
            endActivity();
        else {
            Intent intent = new Intent(CreateQuizActivity.this, NewQuestionActivity.class);
            intent.putExtra("PURPOSE", 0);
            intent.putExtra("ID", quiz.id);
            startActivity(intent);
        }
    }

    private void loadQuestions(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Question.QuestionType>> call=jsonPlaceHolderApi.getQuestions("Token "+MainActivity.server.token.key,null,quiz.id);
        call.enqueue(new Callback<ArrayList<Question.QuestionType>>() {
            @Override
            public void onResponse(Call<ArrayList<Question.QuestionType>> call, Response<ArrayList<Question.QuestionType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadQuestions","Code: "+response.code());
                    Log.d("Server loadQuestions","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadQuestions","got Body");
                ArrayList<Question.QuestionType> quizzes=response.body();
                listOfQuestions.clear();
                for(Question.QuestionType q:quizzes){
                    listOfQuestions.add(new QuestionPanel(q.id,q.content,new TableRow(CreateQuizActivity.this),new TextView(CreateQuizActivity.this),new Button(CreateQuizActivity.this),new Button(CreateQuizActivity.this),CreateQuizActivity.this,quiz.id));
                }
                printQuestions();
            }

            @Override
            public void onFailure(Call<ArrayList<Question.QuestionType>> call, Throwable t) {
                Log.d("Server loadQuestions",t.getMessage());
            }
        });
    }

    private void loadQuiz(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Quiz.QuizType>> call=jsonPlaceHolderApi.getQuizzes("Token "+MainActivity.server.token.key,quizId,MainActivity.server.token.id);
        call.enqueue(new Callback<ArrayList<Quiz.QuizType>>() {
            @Override
            public void onResponse(Call<ArrayList<Quiz.QuizType>> call, Response<ArrayList<Quiz.QuizType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadQuiz","Code: "+response.code());
                    Log.d("Server loadQuiz","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadQuiz","got Body");
                ArrayList<Quiz.QuizType> quizzes=response.body();
                quiz=quizzes.get(0);
                quiz.id=quizId;
                printQuiz();
                loadQuestions();
            }

            @Override
            public void onFailure(Call<ArrayList<Quiz.QuizType>> call, Throwable t) {
                Log.d("Server loadQuiz",t.getMessage());
            }
        });
    }

    private void printQuiz(){
        nameText.setText(quiz.name);
        desText.setText(quiz.description);
    }

    private void printQuestions(){
        linearLayout1.removeAllViews();
        for(QuestionPanel q:listOfQuestions)
            linearLayout1.addView(q.tableRow);

        isBuild=true;
    }

    void getQuizData(){
        TextView pageTitle=findViewById(R.id.text_create_quiz);
        pageTitle.setText("Edycja Quizu");
        quizId=getIntent().getIntExtra("ID",-1);
        if(quizId!=-1) {
            loadQuiz();
        }
    }

    void endActivity(){
        if(purpose==1) {
            //ZAPISANIE ZMIAN
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl("http://192.168.1.34:1025/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

            quiz.name=nameText.getText().toString();
            quiz.description=desText.getText().toString();


            Call<Quiz.QuizType> call=jsonPlaceHolderApi.editQuiz("Token "+MainActivity.server.token.key,quiz.id,quiz);
            call.enqueue(new Callback<Quiz.QuizType>() {
                @Override
                public void onResponse(Call<Quiz.QuizType> call, Response<Quiz.QuizType> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server editQuiz","Code: "+response.code());
                        Log.d("Server editQuiz","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server editQuiz","got Body");
                    Quiz.QuizType quizzes=response.body();
                }

                @Override
                public void onFailure(Call<Quiz.QuizType> call, Throwable t) {
                    Log.d("Server editQuiz",t.getMessage());
                }
            });
            CreateQuizActivity.this.finish();
        }
        else{
            //STWORZENIE QUIZU
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl("http://192.168.1.34:1025/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
            quiz=new Quiz.QuizType(nameText.getText().toString(),desText.getText().toString());

            Call<Quiz.QuizType> call=jsonPlaceHolderApi.createQuiz("Token "+MainActivity.server.token.key,quiz);
            call.enqueue(new Callback<Quiz.QuizType>() {
                @Override
                public void onResponse(Call<Quiz.QuizType> call, Response<Quiz.QuizType> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server createQuiz","Code: "+response.code());
                        Log.d("Server createQuiz","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server createQuiz","got Body");
                    quiz=response.body();
                    newQuestion();
                    isBuild=true;
                }

                @Override
                public void onFailure(Call<Quiz.QuizType> call, Throwable t) {
                    Log.d("Server createQuiz",t.getMessage());
                }
            });
            //CreateQuizActivity.this.finish();
        }
    }
}

class QuestionPanel{
    TableRow tableRow;
    TextView textView;
    Button buttonEdit;
    Button buttonDelete;
    int questionId;
    int quizId;
    String question;
    AppCompatActivity parent;

    public QuestionPanel(int id, String question,TableRow row, TextView view, Button buttonE,Button buttonD, AppCompatActivity p,int qId){
        questionId=id;
        this.question=question;
        tableRow=row;
        textView=view;
        buttonEdit=buttonE;
        buttonDelete=buttonD;
        parent=p;
        quizId=qId;
        setUp();
    }

    private void setUp(){
        int stringMax;
        if(question.length()>30) {
            stringMax = 29;
            textView.setText(question.substring(0,stringMax)+"...");
        }
        else {
            stringMax = question.length();
            textView.setText(question.substring(0,stringMax));
        }
        tableRow.setBackgroundColor(Integer.decode("#4e4e4e4e"));
        TableRow.LayoutParams params=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,5,0,5);
        tableRow.setLayoutParams(params);//WIDTH,HEIGHT

        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setWidth(500);
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);

        buttonEdit.setText("Edytuj");
        buttonEdit.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(parent,NewQuestionActivity.class);
                intent.putExtra("PURPOSE",1);
                intent.putExtra("ID",quizId);
                intent.putExtra("ID2",questionId);
                parent.startActivity(intent);
            }
        });

        buttonDelete.setText("Usuń");
        buttonDelete.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(parent,NewQuestionActivity.class);
                intent.putExtra("PURPOSE",2);
                intent.putExtra("ID",quizId);
                intent.putExtra("ID2",questionId);
                parent.startActivity(intent);
            }
        });


        tableRow.addView(textView);
        tableRow.addView(buttonEdit);
        tableRow.addView(buttonDelete);

    }
}


