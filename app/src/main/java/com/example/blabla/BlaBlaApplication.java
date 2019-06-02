package com.example.blabla;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import timber.log.Timber;

public class BlaBlaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            FirebaseFirestore.setLoggingEnabled(true);
            FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(10000);
            FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(10000);
        }
    }
}
