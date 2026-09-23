package com.example.pps.ui.classement;

import com.example.pps.ui.config.Competition;
import com.example.pps.ui.config.NouvelleManche;
import com.example.pps.ui.config.TypeEpreuve;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    @GET("rest/v1/classement_complet")
    Call<List<Participant>> getClassement(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("order") String order
    );
    @GET("rest/v1/competitions")
    Call<List<Competition>> getCompetitions(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken
    );

    @GET("rest/v1/types_epreuve")
    Call<List<TypeEpreuve>> getTypesEpreuve(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken
    );

    @POST("rest/v1/manches")
    Call<Void> creerManche(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken,
            @Body NouvelleManche manche
    );
}