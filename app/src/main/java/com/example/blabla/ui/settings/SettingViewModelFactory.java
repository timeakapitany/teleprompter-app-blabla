package com.example.blabla.ui.settings;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.blabla.model.TextProject;

public class SettingViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    TextProject textProject;
    Context context;

    public SettingViewModelFactory(TextProject textProject, Context context) {
        this.textProject = textProject;
        this.context = context;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        SettingsViewModel settingsViewModel = new SettingsViewModel(textProject, context);
        return (T) settingsViewModel;
    }

}
