package com.example.blabla.ui.create;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blabla.exception.InternetException;
import com.example.blabla.model.TextProject;
import com.example.blabla.repository.TextProjectRepository;
import com.example.blabla.util.NetworkUtils;
import com.example.blabla.util.Result;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.UUID;

import timber.log.Timber;

class CreateTextViewModel extends ViewModel {

    final MutableLiveData<TextProject> textProject = new MutableLiveData<>();
    final MutableLiveData<Result<String>> text = new MutableLiveData<>();
    final MutableLiveData<Result<TextProject>> saveResult = new MutableLiveData<>();

    private TextLoadAsyncTask textLoadAsyncTask;
    private final TextProjectRepository repository = new TextProjectRepository();

    public CreateTextViewModel(TextProject textProject) {
        this.textProject.setValue(textProject);
        if (textProject.getTextId() != null) {
            populateText();
        }
    }

    @Override
    protected void onCleared() {
        if (textLoadAsyncTask != null) {
            textLoadAsyncTask.cancel(true);
        }
        super.onCleared();
    }

    void saveToFirebaseStorage(String textToUpload, String title, Context context) {
        if (!NetworkUtils.isConnected(context)) {
            saveResult.postValue(Result.error(new InternetException()));
            return;
        }
        if (textProject.getValue().getTextId() != null) {
            repository.deleteDocumentOnFirebaseStorage(textProject.getValue().getTextReference());
        }
        String textID = UUID.randomUUID().toString();
        repository.saveToStorage(textID, textToUpload, saveResult, taskSnapshot -> {
            Timber.d("onSuccess: ");
            saveToFirebaseFirestore(textID, title);
        });
    }

    private void saveToFirebaseFirestore(String textID, String title) {
        TextProject project = textProject.getValue();
        project.setTextTitle(title);
        project.setTextReference(textID);
        String documentId;
        if (project.getTextId() == null) {
            documentId = UUID.randomUUID().toString();
            project.setTextId(documentId);
            project.setCreationDate(new Timestamp(new Date()));
        } else {
            documentId = project.getTextId();
        }
        repository.saveToFirestore(documentId, project, saveResult);
    }

    private void populateText() {
        repository.populateText(textProject.getValue().getTextReference(), text);
    }

    void startNetworkCall(String url) {
        textLoadAsyncTask = new TextLoadAsyncTask();
        textLoadAsyncTask.execute(url);
    }

    @SuppressLint("StaticFieldLeak")
    class TextLoadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Timber.d("doInBackground: %s", strings[0]);
            return NetworkUtils.downloadData(strings[0]);
        }

        @Override
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            if (text != null) {
                CreateTextViewModel.this.text.postValue(Result.success(text));
                Timber.d("onPostExecute: ");
            } else {
                CreateTextViewModel.this.text.postValue(Result.error(new Exception()));
            }
        }
    }

}
