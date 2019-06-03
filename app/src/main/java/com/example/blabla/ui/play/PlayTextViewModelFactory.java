package com.example.blabla.ui.play;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.blabla.model.TextProject;

class PlayTextViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    final TextProject textProject;

    public PlayTextViewModelFactory(TextProject textProject) {
        this.textProject = textProject;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        PlayTextViewModel playTextViewModel = new PlayTextViewModel(textProject);
        return (T) playTextViewModel;
    }

}
