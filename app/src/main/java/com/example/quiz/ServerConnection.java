package com.example.quiz;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ServerConnection {

    class Token{
        String key;
        int id;

        @SerializedName("body")
        String text;

        public Token(String token){
            key=token;
        }

    }

    private OkHttpClient client;

    private final class EchoWebSocketListener extends WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS=1000;
        int index=0;

        @Override
        public void onOpen(WebSocket webSocket, Response response){
            String message;
            message="{\"token\": \""+MainActivity.server.token.key+"\"}";
            webSocket.send(message);
            Log.d("WebSocket","Send: "+message+" Index: "+index);
            index++;
            Log.d("WebSocket","Response: "+response.toString()+" Index: "+index);
            index++;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text){
            Log.d("WebSocket","OnReceivingString: "+text+" Index: "+index);
            index++;
            responseInterpreter(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes){
            Log.d("WebSocket","OnReceivingByte: "+bytes.hex()+" Index: "+index);
            index++;
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason){
            webSocket.close(NORMAL_CLOSURE_STATUS,null);
            Log.d("WebSocket","OnClosing: "+code+" / "+reason+" Index: "+index);
            index++;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response){
            Log.d("WebSocket","OnError: "+t.getMessage()+" Index: "+index);
            index++;
        }
    }

    //OGÓLNE
    Token token;
    String username;
    String address="http://192.168.1.34:1025/";//"http://127.0.0.1:1025/";
    WebSocket myWebSocket;

    //GRA
    QuizActivity gameWindow;

    //ZAPROSZENIA
    InvitationActivity invitationWindow=null;
    ArrayList<String> invitations=new ArrayList<>();

    NewGameActivity newGameWindow;

    void makeConnection(){
        client=new OkHttpClient();
        Request request=new Request.Builder().url("ws://192.168.1.34:1025/ws/main").build();
        EchoWebSocketListener listener=new EchoWebSocketListener();
        myWebSocket=client.newWebSocket(request,listener);

        client.dispatcher().executorService().shutdown();
    }

    private void responseInterpreter(String response){
        JsonParser parser=new JsonParser();
        JsonObject json=(JsonObject) parser.parse(response);

        String type=json.get("type").toString();
        type=type.substring(1,type.length()-1);
        if(type.equals("question")) {
            responseInterpreterQuestion(json);
        }
        else if(type.equals("question_multi")){
            responseInterpreterQuestionMulti(json);
        }
        else if(type.equals("correct_answers")){
            responseInterpreterAnswers(json);
        }
        else if(type.equals("correct_answers_multi")){
            responseInterpreterAnswersMulti(json);
        }
        else if(type.equals("result")){
            responseInterpreterResult(json);
        }
        else if(type.equals("invitation")){
            responseInterpreterInvitation(json);
        }
        else if(type.equals("invitation_answer")){
            responseInterpreterInvitationAnswer(json);
        }
        else if(type.equals("quiz_no_permission")){
            responseInterpreterNoPermission(json);
        }
    }

    //przetworzenie pytania, ustawienie pytania i odpowiedzi
    private void responseInterpreterQuestion(JsonObject json){
        String question=json.get("question_content").toString();
        question=question.substring(1,question.length()-1);
        JsonArray answers=json.getAsJsonArray("answers");
        gameWindow.answersList.clear();
        for(JsonElement j:answers){
            JsonObject obj=j.getAsJsonObject();
            int id=obj.get("id").getAsInt();
            String content=obj.get("answer_content").toString();
            content=content.substring(1,content.length()-1);
            gameWindow.answersList.add(new Answer(id,0,content,"",0));
        }

        gameWindow.question=new Question(question,gameWindow.answersList.get(0).content,gameWindow.answersList.get(1).content,gameWindow.answersList.get(2).content,gameWindow.answersList.get(3).content);
        gameWindow.printQuestion();
    }

    private void responseInterpreterQuestionMulti(JsonObject json){
        String question=json.get("question_content").toString();
        question=question.substring(1,question.length()-1);
        JsonArray answers=json.getAsJsonArray("answers");
        gameWindow.answersList.clear();
        for(JsonElement j:answers){
            JsonObject obj=j.getAsJsonObject();
            int id=obj.get("id").getAsInt();
            String content=obj.get("answer_content").toString();
            content=content.substring(1,content.length()-1);
            gameWindow.answersList.add(new Answer(id,0,content,"",0));
        }

        gameWindow.question=new Question(question,gameWindow.answersList.get(0).content,gameWindow.answersList.get(1).content,gameWindow.answersList.get(2).content,gameWindow.answersList.get(3).content);
        gameWindow.printQuestion();
    }

    //przetworzenie poprwanych odpowiedzi, dodanie punktów
    private void responseInterpreterAnswers(JsonObject json){

        JsonArray answers=json.getAsJsonArray("answers");
        int index=0;
        int oldScore=gameWindow.score;
        for(JsonElement j:answers){
            JsonObject obj=j.getAsJsonObject();
            int isCorrect=obj.get("answer").getAsInt();
            Log.d("APP","MY: "+gameWindow.answersList.get(index).is_correct+" isCorrect: "+isCorrect);
            if(gameWindow.answersList.get(index).is_correct==isCorrect && 1==isCorrect)
                gameWindow.score += 10;

            index++;
        }
        gameWindow.answers.add("Q"+gameWindow.questionId+": "+gameWindow.question.question);
        if(oldScore!=gameWindow.score)
            gameWindow.answers.add("GOOD");
        else
            gameWindow.answers.add("BAD");
        gameWindow.getNextQuestion();
    }

    private void responseInterpreterAnswersMulti(JsonObject json){

        JsonArray answers=json.getAsJsonArray("answers");
        int index=0;
        int oldScore=gameWindow.score;
        for(JsonElement j:answers){
            JsonObject obj=j.getAsJsonObject();
            int isCorrect=obj.get("answer").getAsInt();
            Log.d("APP","MY: "+gameWindow.answersList.get(index).is_correct+" isCorrect: "+isCorrect);
            if(gameWindow.answersList.get(index).is_correct==isCorrect && 1==isCorrect)
                gameWindow.score += 10;

            index++;
        }
        gameWindow.answers.add("Q"+gameWindow.questionId+": "+gameWindow.question.question);
        if(oldScore!=gameWindow.score)
            gameWindow.answers.add("GOOD");
        else
            gameWindow.answers.add("BAD");
        gameWindow.getNextQuestionMulti();
    }

    //przetworzenie podsumowania quizu
    private void responseInterpreterResult(JsonObject json){
        gameWindow.resultPoints=json.get("result").getAsInt();
        if(gameWindow.isMulti==1)
            gameWindow.opponentResultPoints=json.get("result_opponent").getAsInt();
        gameWindow.printQuestion();
    }

    //przetwarzanie zaproszeń
    private void responseInterpreterInvitation(JsonObject json){
        String playerName;
        Date now=new Date();
        playerName=json.get("user").getAsString();
        invitations.add(playerName+":"+now.getTime());
        if(invitationWindow!=null)
            invitationWindow.printInvitations();
    }

    private void responseInterpreterInvitationAnswer(JsonObject json){
        String answer=json.get("answer").getAsString();
        if(answer.equals("true"))
            newGameWindow.createGame();
        else if(answer.equals("false"))
            newGameWindow.printMessage("Zaproszenie odrzucone");
        else if(answer.equals("busy"))
            newGameWindow.printMessage("Użytkownik zajęty");

    }

    private void responseInterpreterNoPermission(JsonObject json){

    }
}
