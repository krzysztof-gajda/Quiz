package com.example.quiz;

import com.google.gson.annotations.SerializedName;

public class Answer {
    Integer id;
    Integer id_question;
    String content;
    String picture;
    int is_correct;

    @SerializedName("body")
    String text;

    public Answer(Integer i,Integer idQ,String c,String p,int is){
        id=i;
        id_question=idQ;
        content=c;
        picture=p;
        is_correct=is;
    }


}
