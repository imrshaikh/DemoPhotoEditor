package com.example.editor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class Prefs {
    public static final String KEY_LAST_IMAGE_URI = "last_image_uri";

    public final SharedPreferences prefs;

    @Inject
    public Prefs(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences("prefs-private", Context.MODE_PRIVATE);
    }

}
