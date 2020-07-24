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

public class RegisterActivity extends AppCompatActivity {

    JsonPlaceHolder jsonPlaceHolderApi;
    Retrofit retrofit;
    Button buttonRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                buttonRegister.setEnabled(false);
            }
        });
        retrofit=new Retrofit.Builder()
                .baseUrl("http://192.168.1.34:1025/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private boolean checkData(String login, String password, String passwordConfirm) {
        if(!password.equals(passwordConfirm)) {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

            dlgAlert.setMessage("Podaj to samo hasło");
            dlgAlert.setTitle("Bad data:");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            return false;
        }

        return  true;
    }

    private void register(){

        EditText editTextLogin = (EditText)findViewById(R.id.edit_text_login_register);
        String login = editTextLogin.getText().toString();

        EditText editTextPassword = (EditText)findViewById(R.id.edit_text_password_register);
        String password=editTextPassword.getText().toString();

        EditText editTextPasswordConfirm = (EditText)findViewById(R.id.edit_text_password_register_confirm);
        String passwordConfirm=editTextPasswordConfirm.getText().toString();


        if(checkData(login, password, passwordConfirm)){
            FriendLogin user=new FriendLogin(login,password);

            jsonPlaceHolderApi=retrofit.create(JsonPlaceHolder.class);
            Call<FriendLogin> call=jsonPlaceHolderApi.register(user);
            Log.d("REQUEST",call.request().toString());
            call.enqueue(new Callback<FriendLogin>() {
                @Override
                public void onResponse(Call<FriendLogin> call, Response<FriendLogin> response) {
                    if (!response.isSuccessful()) {
                        Log.d("RESPONSE", response.toString());
                        Log.d("RESPONSE", response.headers().toString());
                        registerBad();
                        return;
                    }
                    FriendLogin user = response.body();
                    Log.d("RESPONSE", "Message: " + response.message());
                    Log.d("DATA", "ID: " + user.id+" Username: "+user.username);
                    registerGood(login,password);
                }
                @Override
                public void onFailure(Call<FriendLogin> call, Throwable t) {
                    Log.d("RESPONSE", t.getMessage());
                    registerBad();
                }
            });
            /*
            if(MainActivity.server.register(login,password)) {
                Intent intent = new Intent(RegisterActivity.this, MenuActivity.class);
                startActivity(intent);
                this.finish();
            }

             */
        }
    }

    private void registerGood(String login, String password){
        Call<ServerConnection.Token> call=jsonPlaceHolderApi.login(login,password);
        Log.d("REQUEST",call.request().toString());

        call.enqueue(new Callback<ServerConnection.Token>() {
            @Override
            public void onResponse(Call<ServerConnection.Token> call, Response<ServerConnection.Token> response) {
                if(!response.isSuccessful()){
                    Log.d("Server pLogin","Code: "+response.toString());
                    Log.d("Server pLogin","Code: "+response.headers());
                    return;
                }

                MainActivity.server.token=response.body();
                Log.d("Token",MainActivity.server.token.key);
                Log.d("Token",response.message());
                Intent intent = new Intent(RegisterActivity.this, MenuActivity.class);
                startActivity(intent);
                RegisterActivity.this.finish();
                return;
            }

            @Override
            public void onFailure(Call<ServerConnection.Token> call, Throwable t) {
                Log.d("Server pLogin",t.getMessage());
                return;
            }
        });
    }

    private void registerBad(){
        buttonRegister.setEnabled(true);
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Error");
        dlgAlert.setMessage("Spróbuj się zalogować");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
