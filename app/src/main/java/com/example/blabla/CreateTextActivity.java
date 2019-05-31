package com.example.blabla;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
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
    public static final String TEXTPROJECT = "textProject";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef = storage.getReference();
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private TextProject textProject;


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


    public static Intent newIntent(Context context, TextProject textProject) {
        Intent intent = new Intent(context, CreateTextActivity.class);
        intent.putExtra(TEXTPROJECT, textProject);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtext);
        ButterKnife.bind(this);
        onNewIntent(getIntent());

        if (textProject.getTextId() != null) {
            textTitle.setText(textProject.getTextTitle());
            populateText(textProject);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit text");
            }
        }

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
        if (textProject.getTextId() != null) {
            storageRef.child(userId).child(textProject.getTextReference()).delete()
                    .addOnFailureListener(e -> Log.d("Failed", "onFailure: "))
                    .addOnSuccessListener(aVoid -> Log.d("Success", "onSuccess: "));
        }
        String textID = UUID.randomUUID().toString();
        StorageReference textRef = storageRef.child(userId).child(textID);
        byte[] data = textBody.getText().toString().getBytes();
        UploadTask uploadTask = textRef.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            Log.d("Failed", "onFailure: ");
            progressBar.hide();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d("Success", "onSuccess: ");
            saveToFirebaseFirestore(textID);
            progressBar.hide();
        });
    }

    private void saveToFirebaseFirestore(String textID) {
        textProject.setTextTitle(textTitle.getText().toString());
        textProject.setTextReference(textID);
        if (textProject.getTextId() != null) {
            db.collection("users")
                    .document(userId)
                    .collection("textprojects")
                    .document(textProject.getTextId())
                    .update("textReference", textProject.getTextReference(),
                            "textTitle", textProject.getTextTitle())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Success", "onSuccess: ");
                        Intent intent = SettingsActivity.newIntent(this, textProject);
                        startActivity(intent);
//                        finish();
                        onBackPressed();
                    })
                    .addOnFailureListener(e -> Log.d("Failure", "onFailure: "));

        } else {
            String documentId = UUID.randomUUID().toString();
            textProject.setTextId(documentId);
            textProject.setCreationDate(new Timestamp(new Date()));

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
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Success", "onSuccess: ");
                        finish();
                    });
        }

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

    @Override
    protected void onNewIntent(Intent intent) {
        textProject = intent.getParcelableExtra(TEXTPROJECT);
        super.onNewIntent(intent);
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

    private void populateText(TextProject textProject) {
        StorageReference textRef = storageRef.child(userId).child(textProject.getTextReference());
        textRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            String text = new String(bytes);
            textBody.setText(text);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
