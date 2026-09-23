package com.example.pps.ui.classement;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static final String BASE_URL = "https://gpgxhmuotatvlbvitiro.supabase.co/";
    public static final String API_KEY = "sb_publishable_OrRRAe_P7F8ROOjyB8MTRA_1KUPSzkj";

    private static Retrofit retrofit;

    public static SupabaseApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApi.class);
    }
}