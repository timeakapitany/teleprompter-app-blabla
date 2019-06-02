package com.example.blabla.ui.play;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blabla.model.TextProject;
import com.example.blabla.repository.TextProjectRepository;
import com.example.blabla.util.Result;

public class PlayTextViewModel extends ViewModel {

    TextProject textProject;
    int scrollPosition;
    MutableLiveData<Result<String>> text = new MutableLiveData<>();
    private TextProjectRepository repository = new TextProjectRepository();

    public PlayTextViewModel(TextProject textProject) {
        this.textProject = textProject;
        populateText();
    }

    private void populateText() {
        repository.populateText(textProject.getTextReference(), text);
    }

}
