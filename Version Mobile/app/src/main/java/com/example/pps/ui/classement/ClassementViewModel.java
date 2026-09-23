package com.example.pps.ui.classement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassementViewModel extends ViewModel {

    private final MutableLiveData<List<Participant>> ciblesClassement = new MutableLiveData<>();

    public LiveData<List<Participant>> getClassementData() {
        return ciblesClassement;
    }

    public void chargerClassement() {
        List<Participant> simulationDonnees = new ArrayList<>();
        simulationDonnees.add(new Participant(1, "Alice",6, 2500));
        simulationDonnees.add(new Participant(2, "Alain", 7,1250));
        simulationDonnees.add(new Participant(3, "Mel", 2, 100));

        ciblesClassement.setValue(simulationDonnees);
    }
}