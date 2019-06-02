package com.example.blabla.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blabla.R;
import com.example.blabla.exception.InternetException;
import com.example.blabla.model.TextProject;
import com.example.blabla.repository.TextProjectRepository;
import com.example.blabla.util.NetworkUtils;
import com.example.blabla.util.Result;

class SettingsViewModel extends ViewModel {

    private Context context;
    private TextProjectRepository repository = new TextProjectRepository();
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
                repository.saveToFirestore(project.getTextId(), project, saveResult);
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
            repository.populateText(textProject.getTextReference(), text);
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
