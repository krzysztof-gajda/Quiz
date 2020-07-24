package com.example.quiz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class NewQuestionActivity extends AppCompatActivity {

    ArrayList<Answer> answersList=new ArrayList<>();
    int purpose;
    Question.QuestionType question;
    int quizId;
    int questionId;
    ArrayList<EditText> answersText=new ArrayList<>();
    ArrayList<RadioButton> answersIs=new ArrayList<>();
    EditText editQuestion;

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Wychodznie");
        dlgAlert.setMessage("Niezapisane zmiany zostaną utracone");
        dlgAlert.setPositiveButton("Anuluj",null);
        dlgAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NewQuestionActivity.this.finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);
        editQuestion = findViewById(R.id.edit_text_question_name);
        answersText.add(findViewById(R.id.edit_text_question_1));
        answersText.add(findViewById(R.id.edit_text_question_2));
        answersText.add(findViewById(R.id.edit_text_question_3));
        answersText.add(findViewById(R.id.edit_text_question_4));

        answersIs.add(findViewById(R.id.is_correct_question_1));
        answersIs.add(findViewById(R.id.is_correct_question_2));
        answersIs.add(findViewById(R.id.is_correct_question_3));
        answersIs.add(findViewById(R.id.is_correct_question_4));


        Button buttonNewQuestion = findViewById(R.id.button_add_question);
        buttonNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(purpose==1) {
                    int alS = answersList.size();
                    int atS = answersText.size();
                    if (alS != atS) {
                        for (int i = alS; i < 4; i++)
                            answersList.add(new Answer(null, question.id, answersText.get(i).getText().toString(), "", 0));
                    }
                }
                boolean flag=false;
                for(RadioButton rb: answersIs)
                    if(rb.isChecked())
                        flag=true;

                if(flag)
                    endActivity();
                else{
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(NewQuestionActivity.this);

                    dlgAlert.setTitle("Zapisywanie");
                    dlgAlert.setMessage("Zaznacz poprawną odpowiedź");
                    dlgAlert.setNegativeButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            }
        });

        purpose=getIntent().getIntExtra("PURPOSE",0);
        quizId=getIntent().getIntExtra("ID",0);
        if(purpose==1) {
            buttonNewQuestion.setText("Zapisz zmiany");
            TextView title=findViewById(R.id.text_create_question);
            title.setText("Edycja Pytania");
            questionId=getIntent().getIntExtra("ID2",-1);
            if(questionId!=-1)
                loadQuestion();
        }
        else if(purpose==2){
            TextView title=findViewById(R.id.text_create_question);
            title.setText("Usuwanie Pytania");
            questionId=getIntent().getIntExtra("ID2",-1);
            if(questionId!=-1)
                loadQuestion();
        }
    }

    private void loadQuestion(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Question.QuestionType>> call=jsonPlaceHolderApi.getQuestions("Token "+MainActivity.server.token.key,questionId,quizId);
        call.enqueue(new Callback<ArrayList<Question.QuestionType>>() {
            @Override
            public void onResponse(Call<ArrayList<Question.QuestionType>> call, Response<ArrayList<Question.QuestionType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadAnswers","Code: "+response.code());
                    Log.d("Server loadAnswers","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadAnswers","got Body");
                ArrayList<Question.QuestionType> questions=response.body();
                question=questions.get(0);
                editQuestion.setText(question.content);
                Log.d("Server loadAnswers","Question: "+question.toString());
                loadAnswers();
            }

            @Override
            public void onFailure(Call<ArrayList<Question.QuestionType>> call, Throwable t) {
                Log.d("Server loadAnswers",t.getMessage());
            }
        });
    }

    private void endActivity(){

        if(purpose==1) {
            //ZAPISANIE ZMIAN
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl("http://192.168.1.34:1025/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
            question.content=editQuestion.getText().toString();

            Call<Question.QuestionType> call=jsonPlaceHolderApi.editQuestion("Token "+MainActivity.server.token.key,question.id,question);
            call.enqueue(new Callback<Question.QuestionType>() {
                @Override
                public void onResponse(Call<Question.QuestionType> call, Response<Question.QuestionType> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server editQuestion","Code: "+response.code());
                        Log.d("Server editQuestion","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server editQuestion","got Body");
                    Question.QuestionType questionR=response.body();
                    question=questionR;
                    editAnswers();
                    createNewAnswers();
                }

                @Override
                public void onFailure(Call<Question.QuestionType> call, Throwable t) {
                    Log.d("Server editQuestion",t.getMessage());
                }
            });
            this.finish();
        }
        else{
            //STWORZENIE PYTANIA
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl("http://192.168.1.34:1025/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
            question=new Question.QuestionType(quizId,editQuestion.getText().toString(),"");

            Call<Question.QuestionType> call=jsonPlaceHolderApi.createQuestion("Token "+MainActivity.server.token.key,question);
            call.enqueue(new Callback<Question.QuestionType>() {
                @Override
                public void onResponse(Call<Question.QuestionType> call, Response<Question.QuestionType> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server createQuestion","Code: "+response.code());
                        Log.d("Server createQuestion","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server createQuestion","got Body");
                    Question.QuestionType questionR=response.body();
                    question=questionR;
                    createAnswers();
                }

                @Override
                public void onFailure(Call<Question.QuestionType> call, Throwable t) {
                    Log.d("Server createQuestion",t.getMessage());
                }
            });
            this.finish();
        }
    }

    private void loadAnswers(){
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
                ArrayList<Answer> answers=response.body();
                int index=0;
                for(Answer a:answers){
                    answersList.add(a);
                    answersText.get(index).setText(a.content);
                    if(a.is_correct==1)
                        answersIs.get(index).setChecked(true);
                    index++;
                }
                if(purpose==2)
                    deleteAnswers();
            }

            @Override
            public void onFailure(Call<ArrayList<Answer>> call, Throwable t) {
                Log.d("Server loadAnswers",t.getMessage());
            }
        });
    }

    private void createAnswers(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
        int index=0;
        Answer answer;
        for(EditText et: answersText){

            if(answersIs.get(index).isChecked())
                answer=new Answer(null,question.id,et.getText().toString(),"",1);
            else
                answer=new Answer(null,question.id,et.getText().toString(),"",0);

            index++;
            Call<Answer> call=jsonPlaceHolderApi.createAnswer("Token "+MainActivity.server.token.key,answer);
            call.enqueue(new Callback<Answer>() {
                @Override
                public void onResponse(Call<Answer> call, Response<Answer> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server createAnswer","Code: "+response.code());
                        Log.d("Server createAnswer","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server createAnswer","got Body");
                    Answer answer=response.body();
                }

                @Override
                public void onFailure(Call<Answer> call, Throwable t) {
                    Log.d("Server createAnswer",t.getMessage());
                }
            });
        }
    }

    private void createNewAnswers(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        for(Answer answer:answersList) {
            if(answer.id==null) {
                Call<Answer> call = jsonPlaceHolderApi.createAnswer("Token " + MainActivity.server.token.key, answer);
                call.enqueue(new Callback<Answer>() {
                    @Override
                    public void onResponse(Call<Answer> call, Response<Answer> response) {
                        if (!response.isSuccessful()) {
                            Log.d("Server createNewAnswer", "Code: " + response.code());
                            Log.d("Server createNewAnswer", "Code: " + response.headers());
                            return;
                        }

                        Log.d("Server createNewAnswer", "got Body");
                        Answer answer = response.body();
                    }

                    @Override
                    public void onFailure(Call<Answer> call, Throwable t) {
                        Log.d("Server createNewAnswer", t.getMessage());
                    }
                });
            }
        }
    }

    private void editAnswers(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
        int index=0;
        for(Answer a: answersList){
            if(a.id!=null) {
                a.content = answersText.get(index).getText().toString();
                if(answersIs.get(index).isChecked())
                    a.is_correct=1;
                else
                    a.is_correct=0;
                index++;
                Call<Answer> call = jsonPlaceHolderApi.editAnswer("Token " + MainActivity.server.token.key, a.id, a);
                call.enqueue(new Callback<Answer>() {
                    @Override
                    public void onResponse(Call<Answer> call, Response<Answer> response) {
                        if (!response.isSuccessful()) {
                            Log.d("Server editAnswer", "Code: " + response.code());
                            Log.d("Server editAnswer", "Code: " + response.headers());
                            return;
                        }

                        Log.d("Server editAnswer", "got Body");
                        Answer answer = response.body();
                    }

                    @Override
                    public void onFailure(Call<Answer> call, Throwable t) {
                        Log.d("Server editAnswer", t.getMessage());
                    }
                });
            }
        }
    }

    private void deleteAnswers(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
        for(Answer answer: answersList) {
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
                    if(answersList.indexOf(answer)==answersList.size()-1)
                        deleteQuestion();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("Server deleteAnswer",t.getMessage());
                }
            });
        }
    }

    private void deleteQuestion(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

            Call<Void> call=jsonPlaceHolderApi.deleteQuestion("Token "+MainActivity.server.token.key,question.id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(!response.isSuccessful()){
                        Log.d("Server deleteQuestion","Code: "+response.code());
                        Log.d("Server deleteQuestion","Code: "+response.headers());
                        return;
                    }

                    Log.d("Server deleteQuestion","GOOD");
                    Log.d("Server deleteQuestion","Code: "+response.code());
                    Log.d("Server deleteQuestion","Code: "+response.headers());
                    NewQuestionActivity.this.finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("Server deleteQuestion",t.getMessage());
                }
            });
    }
}
