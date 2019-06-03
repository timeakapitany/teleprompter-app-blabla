package com.example.blabla.ui.create;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.blabla.model.TextProject;

class CreateTextViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    final TextProject textProject;

    public CreateTextViewModelFactory(TextProject textProject) {
        this.textProject = textProject;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        CreateTextViewModel createViewModel = new CreateTextViewModel(textProject);
        return (T) createViewModel;
    }

}
