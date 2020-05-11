package com.mooc.ppjoke.ui.sofa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SofaViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SofaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is sofa fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}