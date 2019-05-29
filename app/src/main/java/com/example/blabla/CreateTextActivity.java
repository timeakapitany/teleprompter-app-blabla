package com.example.blabla;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateTextActivity extends AppCompatActivity {

    private static final int BROWSE_REQUEST = 2;
    SharedPreferences sharedPreferences;

    @BindView(R.id.button_browse)
    Button browseButton;
    @BindView(R.id.button_clear_all)
    Button clearButton;
    @BindView(R.id.button_save_text)
    Button saveButton;
    @BindView(R.id.text_body)
    EditText textBody;
    @BindView(R.id.title)
    EditText textTitle;
    @BindView(R.id.progress_bar_save)
    ContentLoadingProgressBar progressBar;
    @BindView(R.id.title_layout)
    TextInputLayout titleInput;
    @BindView(R.id.body_layout)
    TextInputLayout bodyInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtext);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences("blabla", Context.MODE_PRIVATE);

        ;

        browseButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, BROWSE_REQUEST);
        });

        clearButton.setOnClickListener(v -> {
            textTitle.setText("");
            textBody.setText("");
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateText(titleInput) && validateText(bodyInput)) {
                    progressBar.show();
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    saveToFirebaseStorage();
                }
            }
        });

        textTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateText(titleInput);
                }
            }
        });

        textBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateText(bodyInput);
                }
            }
        });

    }

    private void saveToFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String textID = UUID.randomUUID().toString();
        StorageReference textRef = storageRef.child(userId).child(textID);
        byte[] data = textBody.getText().toString().getBytes();
        UploadTask uploadTask = textRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Failed", "onFailure: ");
                progressBar.hide();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Success", "onSuccess: ");
                saveToFirebaseFirestore(textID, userId);
                progressBar.hide();
            }
        });
    }

    private void saveToFirebaseFirestore(String textID, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String documentId = UUID.randomUUID().toString();

        TextProject textProject = new TextProject();
        textProject.setTextId(documentId);
        textProject.setTextTitle(textTitle.getText().toString());
        textProject.setTextReference(textID);
        textProject.setCreationDate(new Timestamp(new Date()));
        textProject.setBackgroundColor(sharedPreferences.getString(getString(R.string.preference_background_color), getString(R.string.default_background)));
        textProject.setTextColor(sharedPreferences.getString(getString(R.string.preference_text_color), getString(R.string.default_text_color)));
        textProject.setTextSize(sharedPreferences.getInt(getString(R.string.preference_text_size), getResources().getInteger(R.integer.default_text_size)));
        textProject.setScrollSpeed(sharedPreferences.getInt(getString(R.string.preference_scroll_speed), getResources().getInteger(R.integer.default_scroll_speed)));
        textProject.setMirrorMode(sharedPreferences.getBoolean(getString(R.string.preference_mirror_mode), getResources().getBoolean(R.bool.default_mirror)));

        db.collection("users")
                .document(userId)
                .collection("textprojects")
                .document(documentId)
                .set(textProject)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", "onFailure: ");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Success", "onSuccess: ");
                        finish();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == BROWSE_REQUEST) {
            Uri uri = data.getData();
            try {
                String path = URLDecoder.decode(uri.toString(), "UTF-8");
                textTitle.setText(path.substring(path.lastIndexOf("/") + 1));
                String text = readTextFromUri(uri);
                textBody.setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    private Boolean validateText(TextInputLayout textInput) {
        String text = textInput.getEditText().getText().toString().trim();
        if (text.isEmpty()) {
            textInput.setError("Field cannot be empty");
            return false;
        } else {
            textInput.setError(null);
            return true;
        }
    }
}
