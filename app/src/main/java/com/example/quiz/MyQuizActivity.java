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
import android.view.autofill.AutofillId;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MyQuizActivity extends AppCompatActivity {

    ArrayList<Quiz> quizList = new ArrayList<>();
    ArrayList<Question.QuestionType> questions=new ArrayList<>();
    ArrayList<Answer> answers=new ArrayList<>();
    int deleteQuizId;
    RadioGroup radioGroup1;
    boolean isBuild=false;


    @Override
    public void onResume() {

        Log.d("APP","Returning");
        if(isBuild)
            loadQuizzes();

        super.onResume();
    }


    @Override
    public void onBackPressed(){
        radioGroup1.removeAllViews();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_quiz);


        radioGroup1 = (RadioGroup) findViewById(R.id.my_quizes_rg1);

        Button buttonDeleteQuiz = findViewById(R.id.button_delete_quiz);
        buttonDeleteQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteQuiz();
            }
        });

        Button buttonShareQuiz = findViewById(R.id.button_share_quiz);
        buttonShareQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = new EditText(MyQuizActivity.this);
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MyQuizActivity.this);

                dlgAlert.setTitle("Podziel się quizem");
                dlgAlert.setMessage("Podaj nazwę użytkownika");
                dlgAlert.setView(input);
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String friend=input.getText().toString();
                                if(friend!=""){

                                    int quizId=0;
                                    for(Quiz q: quizList)
                                        if(q.choiceQuizButton.isChecked())
                                            quizId=q.quizId;

                                    shareQuiz(friend,quizList.get(quizId-1).quizId);
                                }
                            }});
                dlgAlert.setNegativeButton("Anuluj",null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });

        Button buttonEditQuiz = findViewById(R.id.button_edit_quiz);
        buttonEditQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editQuiz();
            }
        });

        loadQuizzes();
        isBuild=true;
    }

    void loadQuizzes(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Quiz.QuizType>> call=jsonPlaceHolderApi.getQuizzes("Token "+MainActivity.server.token.key,null,MainActivity.server.token.id);
        call.enqueue(new Callback<ArrayList<Quiz.QuizType>>() {
            @Override
            public void onResponse(Call<ArrayList<Quiz.QuizType>> call, Response<ArrayList<Quiz.QuizType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadQuizzes","Code: "+response.code());
                    Log.d("Server loadQuizzes","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadQuizzes","got Body");
                ArrayList<Quiz.QuizType> quizzes=response.body();
                quizList.clear();
                for(Quiz.QuizType q:quizzes){
                    quizList.add(new Quiz(q,new RadioButton(MyQuizActivity.this),MyQuizActivity.this));
                }
                printQuizzes();
            }

            @Override
            public void onFailure(Call<ArrayList<Quiz.QuizType>> call, Throwable t) {
                Log.d("Server loadQuizzes",t.getMessage());
            }
        });
    }

    void printQuizzes(){
        int index=0;
        radioGroup1.removeAllViews();
        for(Quiz q:quizList) {
            radioGroup1.addView(q.choiceQuizButton,index);
            index++;
            Log.d("App loadQuizzes","radioGroupId: "+radioGroup1.getId());
        }
        Log.d("App printQuizzes","radioGroupChildren: "+radioGroup1.getChildCount());
        Log.d("App printQuizzes","radioGroupChild: "+radioGroup1.getChildAt(radioGroup1.getChildCount()-1).toString());
        Log.d("App printQuizzes","radioGroupChildID: "+radioGroup1.getChildAt(radioGroup1.getChildCount()-1).getId());
    }

    void shareQuiz(String friend, int quizId){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(MainActivity.server.address)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Log.d("App","ID: "+quizId+" User: "+friend);
        Share share=new Share(quizId,friend);
        Call<Void> call=jsonPlaceHolderApi.shareQuiz("Token "+MainActivity.server.token.key,share);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()){
                    Log.d("Server share","Code: "+response.code());
                    Log.d("Server share","Code: "+response.headers());
                    return;
                }
                Log.d("Server share","Code: "+response.code());
                Log.d("Server share","Code: "+response.headers());
                Log.d("Server share","Body: "+response.body());
                Log.d("Server share","good share");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("Server share",t.getMessage());
            }
        });
    }

    void deleteQuiz(){
        for(Quiz q: quizList)
            if(q.choiceQuizButton.isChecked())
                deleteQuizId=q.quizId;

        loadQuestions(deleteQuizId);
    }

    void deleteQuizz(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
            Call<Void> call=jsonPlaceHolderApi.deleteQuiz("Token "+MainActivity.server.token.key,deleteQuizId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server deleteQuiz","Code: "+response.code());
                        Log.d("Server deleteQuiz","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server deleteQuiz","Code: "+response.code());
                    Log.d("Server deleteQuiz","Code: "+response.headers());
                    Log.d("Server deleteQuiz","GOOD");
                    MyQuizActivity.this.onResume();

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("Server deleteQuiz",t.getMessage());
                }
            });
    }

    private void loadQuestions(int quizId){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Question.QuestionType>> call=jsonPlaceHolderApi.getQuestions("Token "+MainActivity.server.token.key,null,quizId);
        call.enqueue(new Callback<ArrayList<Question.QuestionType>>() {
            @Override
            public void onResponse(Call<ArrayList<Question.QuestionType>> call, Response<ArrayList<Question.QuestionType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadQuestions","Code: "+response.code());
                    Log.d("Server loadQuestions","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadQuestions","got Body");
                questions=response.body();
                for(Question.QuestionType question:questions){
                    loadAnswers(question);
                }
                if(questions.size()==0)
                    deleteQuizz();
            }

            @Override
            public void onFailure(Call<ArrayList<Question.QuestionType>> call, Throwable t) {
                Log.d("Server loadQuestions",t.getMessage());
            }
        });
    }

    private void deleteQuestions(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        for(Question.QuestionType question: questions) {
            Call<Void> call = jsonPlaceHolderApi.deleteQuestion("Token " + MainActivity.server.token.key, question.id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Log.d("Server deleteQuestion", "Code: " + response.code());
                        Log.d("Server deleteQuestion", "Code: " + response.headers());
                        return;
                    }

                    Log.d("Server deleteQuestion", "GOOD");
                    Log.d("Server deleteQuestion", "Code: " + response.code());
                    Log.d("Server deleteQuestion", "Code: " + response.headers());
                    if(questions.indexOf(question)==questions.size()-1)
                        deleteQuiz();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("Server deleteQuestion", t.getMessage());
                }
            });
        }
    }

    private void loadAnswers(Question.QuestionType question){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Answer>> call=jsonPlaceHolderApi.getAnswers("Token "+MainActivity.server.token.key,question.id);
        call.enqueue(new Callback<ArrayList<Answer>>() {
            @Override
            public void onResponse(Call<ArrayList<Answer>> call, Response<ArrayList<Answer>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadAnswers","Code: "+response.code());
                    Log.d("Server loadAnswers","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadAnswers","got Body");
                ArrayList<Answer> answers2=response.body();
                answers.addAll(answers2);
                if(questions.indexOf(question)==questions.size()-1) {
                    Log.d("Server loadQuestions","got all Answers: "+answers.size());
                    deleteAnswers();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Answer>> call, Throwable t) {
                Log.d("Server loadAnswers",t.getMessage());
            }
        });
    }

    private void deleteAnswers(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
        for(Answer answer: answers) {
            Call<Void> call=jsonPlaceHolderApi.deleteAnswer("Token "+MainActivity.server.token.key,answer.id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server deleteAnswer","Code: "+response.code());
                        Log.d("Server deleteAnswer","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server deleteAnswer","Code: "+response.code());
                    Log.d("Server deleteAnswer","Code: "+response.headers());
                    Log.d("Server deleteAnswer","GOOD");
                    Log.d("Server deleteAnswer","Answer: "+answer.content+" id: "+answer.id);
                    if(answers.indexOf(answer)==answers.size()-1)
                        deleteQuestions();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("Server deleteAnswer",t.getMessage());
                }
            });
        }
    }

    void editQuiz(){
        int quizId=0;
        for(Quiz q: quizList)
            if(q.choiceQuizButton.isChecked())
                quizId=q.quizId;

        Log.d("App editQuiz","quizId: "+quizId);


        Intent intent = new Intent(MyQuizActivity.this, CreateQuizActivity.class);
        intent.putExtra("ID", quizId);
        intent.putExtra("PURPOSE", 1);
        startActivity(intent);

    }

    class Share{
        String username;
        int id_quiz;

        @SerializedName("body")
        String text;

        Share(int i, String u){
            id_quiz=i;
            username=u;
        }
    }
}
