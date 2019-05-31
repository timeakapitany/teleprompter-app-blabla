package com.example.blabla;

import android.app.Application;

import timber.log.Timber;

public class BlaBlaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
