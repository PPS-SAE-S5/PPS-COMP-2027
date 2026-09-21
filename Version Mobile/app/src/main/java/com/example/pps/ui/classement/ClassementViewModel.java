package com.example.pps.ui.classement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassementViewModel extends ViewModel {

    private final MutableLiveData<List<String>> ciblesClassement = new MutableLiveData<>();

    public LiveData<List<String>> getClassementData() {
        return ciblesClassement;
    }

    public void chargerClassement() {
        List<String> simulationDonnees = new ArrayList<>();
        simulationDonnees.add("1. Alice - 2500 pts");
        simulationDonnees.add("2. Alain - 1250 pts");
        simulationDonnees.add("3. Mel - 100 pts");

        ciblesClassement.setValue(simulationDonnees);
    }
}