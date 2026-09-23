package com.example.pps.ui.classement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassementViewModel extends ViewModel {

    private final MutableLiveData<List<Participant>> ciblesClassement = new MutableLiveData<>();

    public LiveData<List<Participant>> getClassementData() {
        return ciblesClassement;
    }

    public void chargerClassement() {
        String bearerToken = "Bearer " + SupabaseClient.API_KEY;

        SupabaseClient.getApi()
                .getClassement(SupabaseClient.API_KEY, bearerToken, "rang.asc")
                .enqueue(new Callback<List<Participant>>() {
                    @Override
                    public void onResponse(Call<List<Participant>> call, Response<List<Participant>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ciblesClassement.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Participant>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}