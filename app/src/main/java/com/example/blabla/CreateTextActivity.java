package com.example.blabla;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateTextActivity extends AppCompatActivity {

    private static final int BROWSE_REQUEST = 2;


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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtext);
        ButterKnife.bind(this);

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
}
