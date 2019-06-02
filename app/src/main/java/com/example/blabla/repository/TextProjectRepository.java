package com.example.blabla.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.blabla.model.TextProject;
import com.example.blabla.util.Result;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import timber.log.Timber;

public class TextProjectRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    public void saveToFirestore(String documentId, TextProject project, MutableLiveData<Result<TextProject>> resultLiveData) {
        db.collection("users")
                .document(userId)
                .collection("textprojects")
                .document(documentId)
                .set(project)
                .addOnFailureListener(e -> {
                    Timber.d("onFailure: ");
                    resultLiveData.postValue(Result.error(e));
                })
                .addOnSuccessListener(aVoid -> {
                    Timber.d("onSuccess: ");
                    resultLiveData.postValue(Result.success(project));
                });
    }

    public void saveToStorage(String textID,
                              String textToUpload,
                              MutableLiveData<Result<TextProject>> resultLiveData,
                              OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        StorageReference textRef = storageRef.child(userId).child(textID);
        byte[] data = textToUpload.getBytes();
        UploadTask uploadTask = textRef.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            Timber.d("onFailure: ");
            resultLiveData.postValue(Result.error(e));
        }).addOnSuccessListener(onSuccessListener);
    }

    public void populateText(String documentReference, MutableLiveData<Result<String>> resultLiveData) {
        StorageReference textRef = storageRef.child(userId).child(documentReference);
        textRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            resultLiveData.postValue(Result.success(new String(bytes)));
        }).addOnFailureListener(e -> resultLiveData.postValue(Result.error(e)));
    }

    public void deleteDocumentOnFirebaseStorage(String textReference) {
        storageRef.child(userId).child(textReference).delete()
                .addOnFailureListener(e -> Timber.d("onFailure: "))
                .addOnSuccessListener(aVoid -> Timber.d("onSuccess: "));
    }
}
