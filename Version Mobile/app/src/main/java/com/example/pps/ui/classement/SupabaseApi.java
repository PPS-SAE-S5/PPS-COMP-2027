package com.example.pps.ui.classement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SupabaseApi {

    @GET("rest/v1/classement_complet")
    Call<List<Participant>> getClassement(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("order") String order
    );
}