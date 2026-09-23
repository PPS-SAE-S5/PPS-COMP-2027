package com.example.pps.ui.config;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.example.pps.ui.classement.SupabaseClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigViewModel extends ViewModel {

    private final MutableLiveData<List<Competition>> competitions = new MutableLiveData<>();
    private final MutableLiveData<List<TypeEpreuve>> typesEpreuve = new MutableLiveData<>();
    private final MutableLiveData<String> resultatCreation = new MutableLiveData<>();

    public LiveData<List<Competition>> getCompetitions() {
        return competitions;
    }

    public LiveData<List<TypeEpreuve>> getTypesEpreuve() {
        return typesEpreuve;
    }

    public LiveData<String> getResultatCreation() {
        return resultatCreation;
    }

    public void chargerListes() {
        String bearerToken = "Bearer " + SupabaseClient.API_KEY;

        SupabaseClient.getApi().getCompetitions(SupabaseClient.API_KEY, bearerToken)
                .enqueue(new Callback<List<Competition>>() {
                    @Override
                    public void onResponse(Call<List<Competition>> call, Response<List<Competition>> response) {
                        if (response.isSuccessful()) {
                            competitions.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Competition>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

        SupabaseClient.getApi().getTypesEpreuve(SupabaseClient.API_KEY, bearerToken)
                .enqueue(new Callback<List<TypeEpreuve>>() {
                    @Override
                    public void onResponse(Call<List<TypeEpreuve>> call, Response<List<TypeEpreuve>> response) {
                        if (response.isSuccessful()) {
                            typesEpreuve.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TypeEpreuve>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    public void creerManche(String competitionId, String typeEpreuveId, int numero,
                            String dateManche, Double distanceRefKm) {
        String bearerToken = "Bearer " + SupabaseClient.API_KEY;

        NouvelleManche manche = new NouvelleManche(competitionId, typeEpreuveId, numero, dateManche, distanceRefKm);

        SupabaseClient.getApi().creerManche(SupabaseClient.API_KEY, bearerToken, manche)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            resultatCreation.setValue("OK");
                        } else {
                            resultatCreation.setValue("Erreur : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        resultatCreation.setValue("Erreur : " + t.getMessage());
                    }
                });
    }
}