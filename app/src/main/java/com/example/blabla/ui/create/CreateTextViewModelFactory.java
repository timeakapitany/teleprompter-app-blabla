package com.example.blabla.ui.create;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.blabla.model.TextProject;

public class CreateTextViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    TextProject textProject;

    public CreateTextViewModelFactory(TextProject textProject) {
        this.textProject = textProject;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        CreateTextViewModel createViewModel = new CreateTextViewModel(textProject);
        return (T) createViewModel;
    }

}
