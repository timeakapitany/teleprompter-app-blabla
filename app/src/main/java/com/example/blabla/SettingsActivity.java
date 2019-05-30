package com.example.blabla;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    public static final int MAX = 30;
    public static final String TEXTPROJECT = "textProject";
    private static final int SIZE = 14;
    SharedPreferences sharedPreferences;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @BindView(R.id.text_preview)
    TextView previewText;
    @BindView(R.id.scrollview_text_preview)
    ScrollView scrollView;
    @BindView(R.id.switch_mirror_mode)
    SwitchCompat switchMirror;
    @BindView(R.id.seekbar_text_size)
    SeekBar seekbarTextSize;
    @BindView(R.id.seekbar_scrolling_speed)
    SeekBar seekbarScrollingSpeed;
    @BindView(R.id.color_picker_text)
    View colorPickerTextView;
    @BindView(R.id.color_picker_background)
    View colorPickerBackgroundView;
    @BindView(R.id.button_save)
    Button saveButton;
    private TextProject textProject;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int scrollSpeedDelay = MAX;

    public static Intent newIntent(Context context, TextProject textProject) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(TEXTPROJECT, textProject);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences("blabla", Context.MODE_PRIVATE);
        onNewIntent(getIntent());
        seekbarScrollingSpeed.setMax(MAX - 1);
        populateText(textProject);
        previewText.setTextSize(getTextSizeByProgress(textProject.getTextSize()));
        seekbarTextSize.setProgress(textProject.getTextSize());
        scrollView.setBackgroundColor(Color.parseColor(textProject.getBackgroundColor()));
        colorPickerBackgroundView.setBackgroundColor(Color.parseColor(textProject.getBackgroundColor()));
        previewText.setTextColor(Color.parseColor(textProject.getTextColor()));
        colorPickerTextView.setBackgroundColor(Color.parseColor(textProject.getTextColor()));
        switchMirror.setChecked(textProject.getMirrorMode());
        setMirrorMode(previewText, textProject.getMirrorMode());
        seekbarScrollingSpeed.setProgress(textProject.getScrollSpeed());
        scrollSpeedDelay = MAX - textProject.getScrollSpeed();


        runnable = () -> {
            scrollView.smoothScrollBy(0, 1);
            handler.postDelayed(runnable, scrollSpeedDelay);
        };
        handler.postDelayed(runnable, scrollSpeedDelay);

        switchMirror.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.getId() == R.id.switch_mirror_mode) {
                setMirrorMode(previewText, isChecked);
                textProject.setMirrorMode(isChecked);
            }
        });

        seekbarTextSize.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previewText.setTextSize(getTextSizeByProgress(progress));
                textProject.setTextSize(progress);
            }
        });

        seekbarScrollingSpeed.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scrollSpeedDelay = MAX - progress;
                textProject.setScrollSpeed(progress);
            }
        });

        saveButton.setOnClickListener(v -> {
            if (textProject.getTextId() == null) {
                sharedPreferences.edit().putString(getString(R.string.preference_background_color), textProject.getBackgroundColor()).apply();
                sharedPreferences.edit().putString(getString(R.string.preference_text_color), textProject.getTextColor()).apply();
                sharedPreferences.edit().putInt(getString(R.string.preference_text_size), textProject.getTextSize()).apply();
                sharedPreferences.edit().putInt(getString(R.string.preference_scroll_speed), textProject.getScrollSpeed()).apply();
                sharedPreferences.edit().putBoolean(getString(R.string.preference_mirror_mode), textProject.getMirrorMode()).apply();
            } else {
                db.collection("users")
                        .document(userId)
                        .collection("textprojects")
                        .document(textProject.getTextId())
                        .update("backgroundColor", textProject.getBackgroundColor(),
                                "textColor", textProject.getTextColor(),
                                "textSize", textProject.getTextSize(),
                                "scrollSpeed", textProject.getScrollSpeed(),
                                "mirrorMode", textProject.getMirrorMode())
                        .addOnSuccessListener(aVoid -> Log.d("Success", "onSuccess: "))
                        .addOnFailureListener(e -> Log.d("Failure", "onFailure: "));
            }
        });


    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @OnClick({R.id.color_picker_text})
    public void onTextColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            textProject.setTextColor("#" + envelope.getHexCode());
            int color = envelope.getColor();
            view.setBackgroundColor(color);
            previewText.setTextColor(color);
        });
    }

    @OnClick({R.id.color_picker_background})
    public void onBackgroundColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            textProject.setBackgroundColor("#" + envelope.getHexCode());
            int color = envelope.getColor();
            view.setBackgroundColor(color);
            scrollView.setBackgroundColor(color);
        });
    }

    private void showColorPickerDialog(View view, ColorEnvelopeListener listener) {
        new ColorPickerDialog.Builder(this)
                .setTitle(R.string.title_color_picker)
                .setPositiveButton(R.string.select, listener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        textProject = intent.getParcelableExtra(TEXTPROJECT);
        super.onNewIntent(intent);
    }

    private int getTextSizeByProgress(int progress) {
        return progress + SIZE;
    }

    private void setMirrorMode(View view, boolean mirrorMode) {
        if (mirrorMode) {
            view.setScaleX(-1);
            view.setScaleY(1);
        } else {
            view.setScaleX(1);
            view.setScaleY(1);
        }
    }

    private void populateText(TextProject textProject) {
        String id = textProject.getTextId();
        if (id == null) {
            previewText.setText(R.string.test);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference textRef = storageRef.child(userId).child(textProject.getTextReference());
            textRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                String text = new String(bytes);
                previewText.setText(text);
            });
        }
    }
}

