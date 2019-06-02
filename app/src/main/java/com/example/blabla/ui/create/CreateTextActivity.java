package com.example.blabla.ui.create;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.blabla.R;
import com.example.blabla.exception.InternetException;
import com.example.blabla.model.TextProject;
import com.example.blabla.ui.settings.SettingsActivity;
import com.example.blabla.util.DialogUtil;
import com.example.blabla.util.Result;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateTextActivity extends AppCompatActivity {

    private static final int BROWSE_REQUEST = 2;
    public static final String TEXTPROJECT = "textProject";

    @BindView(R.id.file_path)
    EditText filePath;
    @BindView(R.id.button_browse)
    Button browseButton;
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
    @BindView(R.id.file_path_layout)
    TextInputLayout filePathInput;
    @BindView(R.id.button_import)
    Button importButton;
    Boolean newText;
    private CreateTextViewModel model;

    public static Intent newIntent(Context context, TextProject textProject) {
        Intent intent = new Intent(context, CreateTextActivity.class);
        intent.putExtra(TEXTPROJECT, textProject);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this, new CreateTextViewModelFactory(getIntent().getParcelableExtra(TEXTPROJECT))).get(CreateTextViewModel.class);
        setContentView(R.layout.activity_createtext);
        ButterKnife.bind(this);
        onNewIntent(getIntent());

        model.text.observe(this, result -> {
            if (result.isSuccess()) {
                textBody.setText(result.getData());
            } else {
                DialogUtil.createAlert(CreateTextActivity.this, getString(R.string.error_message), null);
            }
        });

        model.textProject.observe(this, new Observer<TextProject>() {
            @Override
            public void onChanged(TextProject textProject) {
                newText = textProject.getTextId() == null;
                if (!newText) {
                    textTitle.setText(textProject.getTextTitle());
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(getString(R.string.edit_text_title));
                    }
                }
            }
        });

        model.saveResult.observe(this, new Observer<Result<TextProject>>() {
            @Override
            public void onChanged(Result<TextProject> result) {
                if (result.isSuccess()) {
                    progressBar.hide();
                    if (!newText) {
                        Intent intent = SettingsActivity.newIntent(getApplicationContext(), result.getData());
                        startActivity(intent);
                    }
                    onBackPressed();
                } else {
                    String message;
                    if (result.getException() instanceof InternetException) {
                        message = getString(R.string.no_internet_error_message);
                    } else {
                        message = getString(R.string.error_message);
                    }
                    DialogUtil.createAlert(CreateTextActivity.this, message, null);
                    progressBar.hide();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });

        setListeners();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            if (validateText(titleInput) && validateText(bodyInput)) {
                progressBar.show();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                model.saveToFirebaseStorage(textBody.getText().toString(), textTitle.getText().toString(), this);
            }
            return true;
        } else if (id == R.id.action_clear) {
            clearTextFields();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        browseButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, BROWSE_REQUEST);
        });

        importButton.setOnClickListener(v -> {
            String url = filePath.getText().toString().trim();
            if (url.isEmpty()) {
                filePathInput.setError(getResources().getString(R.string.required_field));
            } else if (!url.endsWith(".txt")) {
                filePathInput.setError(getResources().getString(R.string.required_field_txt));
            } else {
                model.startNetworkCall(url);
            }
        });

        filePath.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                filePathInput.setError(null);
            }
        });

        textTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateText(titleInput);
            }
        });

        textBody.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateText(bodyInput);
            }
        });
    }

    private void clearTextFields() {
        textTitle.setText("");
        textBody.setText("");
        filePath.setText("");
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
            textInput.setError(getResources().getString(R.string.required_field));
            return false;
        } else {
            textInput.setError(null);
            return true;
        }
    }




}
