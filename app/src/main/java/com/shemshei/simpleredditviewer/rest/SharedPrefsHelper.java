package com.shemshei.simpleredditviewer.rest;

import android.content.Context;
import android.content.SharedPreferences;

import com.shemshei.simpleredditviewer.BuildConfig;

/**
 * Created by romanshemshei on 6/5/17.
 */

class SharedPrefsHelper {

    private static final String PREFERENCES = BuildConfig.APPLICATION_ID + ".private.preferences";
    //
    private final SharedPreferences mPrefs;

    SharedPrefsHelper(Context context) {
        this.mPrefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    void saveToPrefs(String key, String value) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    String readFromPrefs(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }
}
