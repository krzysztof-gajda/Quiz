package com.example.quiz;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JsonPlaceHolder {


    @FormUrlEncoded
    @POST("api/login/")
    Call<ServerConnection.Token> login(@Field("username") String user, @Field("password") String pass);
//============================================================================================================================
    @POST("api/logout/")
    Call<ServerConnection.Token> logout();
//============================================================================================================================
    @POST("api/set_quiz_privilege/")
    Call<Void> shareQuiz(@Header("Authorization")String token,@Body MyQuizActivity.Share share);
//============================================================================================================================
    @POST("api/users/")
    Call<FriendLogin> register(@Body FriendLogin user);
//============================================================================================================================
    @POST("api/quizzes/")
    Call<Quiz.QuizType> createQuiz(@Header("Authorization")String token,@Body Quiz.QuizType quiz);

    @PUT("api/quizzes/{id}/")
    Call<Quiz.QuizType> editQuiz(@Header("Authorization")String token,@Path("id") int quizId, @Body Quiz.QuizType quiz);

    @GET("api/quizzes/")
    Call<ArrayList<Quiz.QuizType>> getQuizzes(@Header("Authorization")String token,@Query("id")Integer quizId,@Query("id_creator")Integer creatorId);

    @DELETE("api/quizzes/{id}/")
    Call<Void> deleteQuiz(@Header("Authorization")String token,@Path("id") int quizId);

//============================================================================================================================
    @POST("api/questions/")
    Call<Question.QuestionType> createQuestion(@Header("Authorization")String token,@Body Question.QuestionType question);

    @PUT("api/questions/{id}/")
    Call<Question.QuestionType> editQuestion(@Header("Authorization")String token,@Path("id") int questionId, @Body Question.QuestionType question);

    @GET("api/questions/")
    Call<ArrayList<Question.QuestionType>> getQuestions(@Header("Authorization")String token,@Query("id")Integer questionId,@Query("id_quiz")Integer quizId);

    @DELETE("api/questions/{id}/")
    Call<Void> deleteQuestion(@Header("Authorization")String token,@Path("id") int questionId);
//============================================================================================================================
    @POST("api/answers/")
    Call<Answer> createAnswer(@Header("Authorization")String token,@Body Answer answer);

    @PUT("api/answers/{id}/")
    Call<Answer> editAnswer(@Header("Authorization")String token,@Path("id") int answerId, @Body Answer answer);

    @GET("api/answers/")
    Call<ArrayList<Answer>> getAnswers(@Header("Authorization")String token,@Query("id_question")int questionId);

    @DELETE("api/answers/{id}/")
    Call<Void> deleteAnswer(@Header("Authorization")String token,@Path("id") int answerId);
//============================================================================================================================
}
