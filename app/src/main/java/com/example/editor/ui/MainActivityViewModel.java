package com.example.editor.ui;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.editor.Prefs;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainActivityViewModel extends ViewModel {

    // exposing Immutable LiveData
    public final LiveData<Uri> lastProcessedImageUri;

    // internally changing it through MutableLiveData
    private final MutableLiveData<Uri> mutableUri;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (sharedPreferences, s) -> {
                if (Prefs.KEY_LAST_IMAGE_URI.equals(s)) {
                    readAndPostValue(sharedPreferences);
                }
            };

    @Inject
    public MainActivityViewModel(Prefs prefs) {
        this.mutableUri = new MutableLiveData<>(null);
        this.lastProcessedImageUri = mutableUri;

        readAndPostValue(prefs.prefs);
        prefs.prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void readAndPostValue(SharedPreferences preferences) {
        String  uriString = preferences.getString(Prefs.KEY_LAST_IMAGE_URI, null);
        if (uriString != null) {
            mutableUri.postValue(Uri.parse(uriString));
        }
    }


}
