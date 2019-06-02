package com.example.blabla.ui.settings;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProviders;

import com.example.blabla.R;
import com.example.blabla.model.TextProject;
import com.example.blabla.ui.create.CreateTextActivity;
import com.example.blabla.util.SimpleSeekBarChangeListener;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    public static final int MAX = 30;
    public static final String TEXTPROJECT = "textProject";
    private static final int SIZE = 14;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int scrollSpeedDelay = MAX;
    private SettingsViewModel model;

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
    @BindView(R.id.edit_text)
    ImageView edit;

    public static Intent newIntent(Context context, TextProject textProject) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(TEXTPROJECT, textProject);
        return intent;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        model = ViewModelProviders.of(this).get(SettingsViewModel.class);
        onNewIntent(getIntent());
        model.populateText();
        model.text.observe(this, s -> previewText.setText(s));

        model.textProject.observe(this, textProject -> setupUI(textProject));

        seekbarScrollingSpeed.setMax(MAX - 1);
        runnable = () -> {
            scrollView.smoothScrollBy(0, 1);
            handler.postDelayed(runnable, scrollSpeedDelay);
        };
        handler.postDelayed(runnable, scrollSpeedDelay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        model.textProject.setValue(intent.getParcelableExtra(TEXTPROJECT));
        model.context = this;
        super.onNewIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            model.saveSettings(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.color_picker_text})
    public void onTextColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            model.setTextColor("#" + envelope.getHexCode());
            int color = envelope.getColor();
            view.setBackgroundColor(color);
            previewText.setTextColor(color);
        });
    }

    private int getTextSizeByProgress(int progress) {
        return progress + SIZE;
    }

    @OnClick({R.id.color_picker_background})
    public void onBackgroundColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            model.setBackgroundColor("#" + envelope.getHexCode());
            int color = envelope.getColor();
            view.setBackgroundColor(color);
            scrollView.setBackgroundColor(color);
        });
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

    private void setupUI(TextProject textProject) {
        setUpEditButton(textProject);
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
    }

    private void setUpEditButton(TextProject textProject) {
        if (textProject.getTextId() != null) {
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(v -> {
                Intent intent = CreateTextActivity.newIntent(getApplicationContext(), textProject);
                startActivity(intent);
            });
        }
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
}

