package com.example.blabla.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blabla.R;
import com.example.blabla.exception.InternetException;
import com.example.blabla.model.TextProject;
import com.example.blabla.util.NetworkUtils;
import com.example.blabla.util.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

class SettingsViewModel extends ViewModel {

    Context context;
    MutableLiveData<TextProject> textProject = new MutableLiveData<>();
    MutableLiveData<Result<String>> text = new MutableLiveData<>();
    MutableLiveData<Result<TextProject>> saveResult = new MutableLiveData<>();

    public SettingsViewModel(TextProject textProject, Context context) {
        this.textProject.setValue(textProject);
        this.context = context;
        populateText();
    }

    void saveSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blabla", Context.MODE_PRIVATE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextProject project = textProject.getValue();
        if (project != null) {
            if (project.getTextId() == null) {
                sharedPreferences.edit().putString(context.getString(R.string.preference_background_color), project.getBackgroundColor()).apply();
                sharedPreferences.edit().putString(context.getString(R.string.preference_text_color), project.getTextColor()).apply();
                sharedPreferences.edit().putInt(context.getString(R.string.preference_text_size), project.getTextSize()).apply();
                sharedPreferences.edit().putInt(context.getString(R.string.preference_scroll_speed), project.getScrollSpeed()).apply();
                sharedPreferences.edit().putBoolean(context.getString(R.string.preference_mirror_mode), project.getMirrorMode()).apply();
                saveResult.postValue(Result.success(project));
            } else {
                if (!NetworkUtils.isConnected(context)) {
                    saveResult.postValue(Result.error(new InternetException()));
                    return;
                }
                db.collection("users")
                        .document(userId)
                        .collection("textprojects")
                        .document(project.getTextId())
                        .update("backgroundColor", project.getBackgroundColor(),
                                "textColor", project.getTextColor(),
                                "textSize", project.getTextSize(),
                                "scrollSpeed", project.getScrollSpeed(),
                                "mirrorMode", project.getMirrorMode())
                        .addOnSuccessListener(aVoid -> {
                            Timber.d("onSuccess: ");
                            saveResult.postValue(Result.success(project));
                        })
                        .addOnFailureListener(e -> {
                            Timber.d("onFailure: ");
                            saveResult.postValue(Result.error(e));
                        });
            }
        }
    }

    void populateText() {
        TextProject textProject = this.textProject.getValue();
        String id = textProject.getTextId();
        if (id == null) {
            text.postValue(Result.success(context.getString(R.string.test)));
        } else {
            if (!NetworkUtils.isConnected(context)) {
                text.postValue(Result.error(new InternetException()));
                return;
            }
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference textRef = storageRef.child(userId).child(textProject.getTextReference());
            textRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                text.postValue(Result.success(new String(bytes)));
            }).addOnFailureListener(e -> text.postValue(Result.error(e)));
        }
    }

    void setTextColor(String color) {
        TextProject project = textProject.getValue();
        if (project != null) {
            project.setTextColor(color);
            textProject.postValue(project);
        }
    }

    void setBackgroundColor(String color) {
        TextProject project = textProject.getValue();
        if (project != null) {
            project.setBackgroundColor(color);
            textProject.postValue(project);
        }
    }
}
