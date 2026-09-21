package com.example.pps.ui.classement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClassementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ClassementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the classement fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}