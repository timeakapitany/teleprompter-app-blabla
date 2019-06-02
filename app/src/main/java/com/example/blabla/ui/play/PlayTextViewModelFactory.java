package com.example.blabla.ui.play;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.blabla.model.TextProject;

public class PlayTextViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    TextProject textProject;

    public PlayTextViewModelFactory(TextProject textProject) {
        this.textProject = textProject;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        PlayTextViewModel playTextViewModel = new PlayTextViewModel(textProject);
        return (T) playTextViewModel;
    }

}
