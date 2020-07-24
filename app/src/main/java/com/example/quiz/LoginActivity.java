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

public class LoginActivity extends AppCompatActivity {

    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
                buttonLogin.setEnabled(false);
            }
        });
    }

    private void login(){

        EditText editTextLogin = (EditText)findViewById(R.id.edit_text_login);
        String login = editTextLogin.getText().toString();

        EditText editTextPassword = (EditText)findViewById(R.id.edit_text_password);
        String password=editTextPassword.getText().toString();

        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolder jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);

        Call<ServerConnection.Token> call=jsonPlaceHolderApi.login(login,password);
        Log.d("REQUEST",call.request().toString());

        call.enqueue(new Callback<ServerConnection.Token>() {
            @Override
            public void onResponse(Call<ServerConnection.Token> call, Response<ServerConnection.Token> response) {
                if(!response.isSuccessful()){
                    Log.d("Server pLogin","Code: "+response.toString());
                    Log.d("Server pLogin","Code: "+response.headers());
                    loginBad();
                    return;
                }

                MainActivity.server.token=response.body();
                MainActivity.server.username=login;
                Log.d("Token",MainActivity.server.token.key);
                Log.d("Token",Integer.toString(MainActivity.server.token.id));
                Log.d("Token",response.message());
                loginGood();
                return;
            }

            @Override
            public void onFailure(Call<ServerConnection.Token> call, Throwable t) {
                Log.d("Server pLogin",t.getMessage());
                loginBad();
                return;
            }
        });


        /*
        if(MainActivity.server.login(login,password)){
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            this.finish();
        }
        else{
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

            dlgAlert.setTitle("Error...");
            dlgAlert.setMessage("Zly login lub haslo");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }

         */
    }

    private void loginGood(){
        MainActivity.server.makeConnection();
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void loginBad(){
        buttonLogin.setEnabled(true);
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Error...");
        dlgAlert.setMessage("Zly login lub haslo");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }
}
