package com.example.blabla;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayTextActivity extends AppCompatActivity {
    public static final String CURRENT_TEXTPROJECT = "current_textproject";
    @BindView(R.id.play_text)
    TextView playText;
    private TextProject textProject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playtext);
        ButterKnife.bind(this);

        textProject = getIntent().getParcelableExtra(CURRENT_TEXTPROJECT);

    }
}
