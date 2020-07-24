package com.example.quiz;

import com.google.gson.annotations.SerializedName;

public class Question {
    int questionId;
    String question;
    String answer1;
    String answer2;
    String answer3;
    String answer4;


    public Question(){}

    public Question(QuestionType qt){
        questionId=qt.id;
        question=qt.content;
    }

    public Question(String q,String a1,String a2,String a3,String a4){
        question=q;
        answer1=a1;
        answer2=a2;
        answer3=a3;
        answer4=a4;
    }

    static class QuestionType{
        Integer id;
        Integer id_quiz;
        String content;
        String picture;

        @SerializedName("body")
        String text;

        QuestionType(Integer i,String c, String p){
            id_quiz=i;
            content=c;
            picture=p;
        }
    }
}
