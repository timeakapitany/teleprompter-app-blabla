package com.example.blabla.ui.create;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blabla.exception.InternetException;
import com.example.blabla.model.TextProject;
import com.example.blabla.util.NetworkUtils;
import com.example.blabla.util.Result;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.UUID;

import timber.log.Timber;

public class CreateTextViewModel extends ViewModel {

    MutableLiveData<TextProject> textProject = new MutableLiveData<>();
    MutableLiveData<Result<String>> text = new MutableLiveData<>();
    MutableLiveData<Result<TextProject>> saveResult = new MutableLiveData<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private TextLoadAsyncTask textLoadAsyncTask;

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
            storageRef.child(userId).child(textProject.getValue().getTextReference()).delete()
                    .addOnFailureListener(e -> Timber.d("onFailure: "))
                    .addOnSuccessListener(aVoid -> Timber.d("onSuccess: "));
        }
        String textID = UUID.randomUUID().toString();
        StorageReference textRef = storageRef.child(userId).child(textID);
        byte[] data = textToUpload.getBytes();
        UploadTask uploadTask = textRef.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            Timber.d("onFailure: ");
            saveResult.postValue(Result.error(e));
        }).addOnSuccessListener(taskSnapshot -> {
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
        db.collection("users")
                .document(userId)
                .collection("textprojects")
                .document(documentId)
                .set(project)
                .addOnFailureListener(e -> {
                    Timber.d("onFailure: ");
                    saveResult.postValue(Result.error(e));
                })
                .addOnSuccessListener(aVoid -> {
                    Timber.d("onSuccess: ");
                    saveResult.postValue(Result.success(project));
                });
    }

    void populateText() {
        StorageReference textRef = storageRef.child(userId).child(textProject.getValue().getTextReference());
        textRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            text.postValue(Result.success(new String(bytes)));
        }).addOnFailureListener(e -> text.postValue(Result.error(e)));
    }

    void startNetworkCall(String url) {
        textLoadAsyncTask = new TextLoadAsyncTask();
        textLoadAsyncTask.execute(url);
    }

    class TextLoadAsyncTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "TextLoadAsyncTask";


        @Override
        protected String doInBackground(String... strings) {
            Timber.d("doInBackground: %s", strings[0]);
            String textFeed = NetworkUtils.downloadData(strings[0]);
            return textFeed;
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
