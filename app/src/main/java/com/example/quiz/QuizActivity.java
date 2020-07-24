package com.example.quiz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private static final long COUNTDOWN_IN_MILLIS = 30000;
    private TextView textViewCountDown;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    TextView textQuestion;
    RadioButton answer1;
    RadioButton answer2;
    RadioButton answer3;
    RadioButton answer4;
    TextView scoreCounter;
    TextView questionCounter;
    Button buttonNextQuestion;

    ArrayList<Answer> answersList=new ArrayList<>(); //dane odpowiedzi: treść, czy urzytkownik zaznaczył
    ArrayList<String> answers=new ArrayList<>();
    Question question; //dane pytania: id, treść, treść odpowiedzi
    //Zmienne pomocnicze
    int questionId=0; //aktualny numer pytania (lokaly?)
    int questionsNumber=0; //ilość pytań w quizie
    int score=0; //aktualny wynik gracza
    int quizId; //id quizu
    int resultPoints; //wynik gracza
    int opponentResultPoints;
    int isMulti=0; //czy gra muliplayer

    int index=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewCountDown = findViewById(R.id.text_timer);
        textQuestion = findViewById(R.id.text_question);
        answer1 = findViewById(R.id.radio_button1);
        answer2 = findViewById(R.id.radio_button2);
        answer3 = findViewById(R.id.radio_button3);
        answer4 = findViewById(R.id.radio_button4);
        scoreCounter=findViewById(R.id.text_score);
        questionCounter=findViewById(R.id.text_question_counter);

        timeLeftInMillis = COUNTDOWN_IN_MILLIS;
        startCountdown();

        buttonNextQuestion = findViewById(R.id.button_confirm);
        buttonNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswers();
                countDownTimer.start();
            }
        });

        quizId=getIntent().getIntExtra("ID",-1);
        isMulti=getIntent().getIntExtra("MULTI",0);

        if(quizId!=-1)
            setQuestionsNumber();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Aplikacja");
        dlgAlert.setMessage("Zakończyć quiz?");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        QuizActivity.this.finish();
                    }});
        dlgAlert.setNegativeButton("NIE",null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void setQuestionsNumber(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ArrayList<Question.QuestionType>> call=jsonPlaceHolderApi.getQuestions("Token "+MainActivity.server.token.key,null,quizId);
        call.enqueue(new Callback<ArrayList<Question.QuestionType>>() {
            @Override
            public void onResponse(Call<ArrayList<Question.QuestionType>> call, retrofit2.Response<ArrayList<Question.QuestionType>> response) {
                if(!response.isSuccessful()){
                    Log.d("Server loadQuestions","Code: "+response.code());
                    Log.d("Server loadQuestions","Code: "+response.headers());
                    return;
                }

                Log.d("Server loadQuestions","got Body");
                ArrayList<Question.QuestionType> quizzes=response.body();
                questionsNumber=quizzes.size();
                start();
            }

            @Override
            public void onFailure(Call<ArrayList<Question.QuestionType>> call, Throwable t) {
                Log.d("Server loadQuestions",t.getMessage());
            }
        });
    }

    private void startCountdown(){
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
            }
        }.start();
    }

    private  void updateCountDownText(){
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        textViewCountDown.setText(timeFormatted);
    }

    //ustawienie połączenia z MainActivity.server.singleGame, rozpoczęcie gry
    private void start(){
        MainActivity.server.gameWindow=this;

        String message;
        Log.d("APP","message quizId: "+quizId);
        message="{\"type\": \"play\", \"quiz\": "+quizId+"}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    //pobranie danych pytania, wyświtlenie danych pytania
    void printQuestion(){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(questionId==questionsNumber){
                    Intent intent=new Intent(QuizActivity.this,SummaryActivity.class);
                    intent.putExtra("result",answers);
                    if(isMulti==0)
                        intent.putExtra("score",score+"/"+questionsNumber*10);
                    else
                        intent.putExtra("score",score+"/"+questionsNumber*10+" | "+opponentResultPoints*10+"/"+questionsNumber*10);
                    startActivity(intent);
                    stop();
                    finish();
                }
                else {
                    if(questionId+1==questionsNumber)
                        buttonNextQuestion.setText("Zakończ");

                    questionId++;
                    textQuestion.setText(question.question);
                    answer1.setText(question.answer1);
                    answer2.setText(question.answer2);
                    answer3.setText(question.answer3);
                    answer4.setText(question.answer4);
                    String sc = "Score: " + score;
                    scoreCounter.setText(sc);
                    String qc = "Question " + questionId + "/" + questionsNumber;
                    questionCounter.setText(qc);
                }
            }
        });
    }

    //spisanie odpowidzi gracza
    private void setAnswers(){
        if(answer1.isChecked()) {
            answersList.get(0).is_correct = 1;
            answer1.setChecked(false);
            answer1.setChecked(true);
        }
        if(answer2.isChecked()) {
            answersList.get(1).is_correct = 1;
            answer2.setChecked(false);
            answer2.setChecked(true);
        }
        if(answer3.isChecked()) {
            answersList.get(2).is_correct = 1;
            answer3.setChecked(false);
            answer3.setChecked(true);
        }
        if(answer4.isChecked()) {
            answersList.get(3).is_correct = 1;
            answer4.setChecked(false);
            answer4.setChecked(true);
        }

        if(isMulti==0)
            sendAnswers();
        else
            sendAnswersMulti();
    }

    //wysłanie do MainActivity.server.myWebSocket odpowiedzi gracza
    private void sendAnswers(){
        String message;
        message="{\"type\": \"answer\", \"answers\": [";
        message+="{\"id\": "+answersList.get(0).id+", \"answer\": "+answersList.get(0).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(1).id+", \"answer\": "+answersList.get(1).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(2).id+", \"answer\": "+answersList.get(2).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(3).id+", \"answer\": "+answersList.get(3).is_correct+"}";
        message+="]}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    //wysłanie do MainActivity.server.myWebSocket odpowiedzi gracza w grze multi
    private void sendAnswersMulti(){
        String message;
        message="{\"type\": \"answer_multi\", \"answers\": [";
        message+="{\"id\": "+answersList.get(0).id+", \"answer\": "+answersList.get(0).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(1).id+", \"answer\": "+answersList.get(1).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(2).id+", \"answer\": "+answersList.get(2).is_correct+"}, ";
        message+="{\"id\": "+answersList.get(3).id+", \"answer\": "+answersList.get(3).is_correct+"}";
        message+="]}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    //proźba o kolejne pytanie
    void getNextQuestion(){
        String message;
        message="{\"type\": \"next_question\"}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    //proźba o kolejne pytanie w grze multi
    void getNextQuestionMulti(){
        String message;
        message="{\"type\": \"next_question_multi\"}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    //wysłanie sygnału stop do MainActivity.server.myWebSocket
    private void stop(){
        String message;
        if(isMulti==0)
            message="{\"type\": \"end_game\"}";
        else
            message="{\"type\": \"end_game_multi\"}";
        MainActivity.server.myWebSocket.send(message);
        Log.d("WebSocket","Send: "+message+" Index: "+index);
        index++;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }
}
