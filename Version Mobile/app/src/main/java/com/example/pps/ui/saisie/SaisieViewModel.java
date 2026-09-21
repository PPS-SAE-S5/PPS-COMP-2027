package com.example.pps.ui.saisie;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SaisieViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SaisieViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is zone de saisie fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}