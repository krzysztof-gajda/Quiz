package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.Locale;

public class WaitingActivity extends AppCompatActivity {

    private static final long COUNTDOWN_IN_MILLIS = 300000;
    private TextView textViewCountDown;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        textViewCountDown = findViewById(R.id.text_waiting_timer);

        timeLeftInMillis = COUNTDOWN_IN_MILLIS;
        startCountdown();
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

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }
}
