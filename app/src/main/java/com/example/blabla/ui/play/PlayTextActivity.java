package com.example.blabla.ui.play;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import com.example.blabla.R;
import com.example.blabla.model.TextProject;
import com.example.blabla.util.SimpleSeekBarChangeListener;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayTextActivity extends AppCompatActivity {
    public static final String TEXTPROJECT = "textProject";
    public static final int MAX = 30;
    private static final int SIZE = 14;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int scrollSpeedDelay = MAX;
    private PlayTextViewModel model;
    SharedPreferences sharedPreferences;

    @BindView(R.id.scrollview_play_text)
    ScrollView scrollView;
    @BindView(R.id.switch_mirror_mode)
    SwitchCompat switchMirror;
    @BindView(R.id.seekbar_text_size)
    SeekBar seekbarTextSize;
    @BindView(R.id.play_text)
    TextView playText;
    @BindView(R.id.seekbar_scrolling_speed)
    SeekBar seekbarScrollingSpeed;
    @BindView(R.id.color_picker_text)
    View colorPickerTextView;
    @BindView(R.id.color_picker_background)
    View colorPickerBackgroundView;
    @BindView(R.id.play_control)
    ImageView playControl;
    @BindView(R.id.controls)
    ConstraintLayout controls;

    public static Intent newIntent(Context context, TextProject textProject) {
        Intent intent = new Intent(context, PlayTextActivity.class);
        intent.putExtra(TEXTPROJECT, textProject);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playtext);
        ButterKnife.bind(this);
        model = ViewModelProviders.of(this, new PlayTextViewModelFactory(getIntent().getParcelableExtra(TEXTPROJECT))).get(PlayTextViewModel.class);

        model.text.observe(this, result -> {
            if (result.isSuccess()) {
                playText.setText(result.getData());
            } else {
                playText.setText("");
            }
        });
        sharedPreferences = getSharedPreferences("blabla", Context.MODE_PRIVATE);
        seekbarScrollingSpeed.setMax(MAX - 1);
        setupUI(model.textProject);
        startScrolling(handler, scrollView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(TextProject textProject) {
        playText.setTextSize(getTextSizeByProgress(textProject.getTextSize()));
        seekbarTextSize.setProgress(textProject.getTextSize());
        scrollView.setVerticalScrollbarPosition(model.scrollPosition * scrollView.getHeight());
        scrollView.setBackgroundColor(Color.parseColor(textProject.getBackgroundColor()));
        colorPickerBackgroundView.setBackgroundColor(Color.parseColor(textProject.getBackgroundColor()));
        playText.setTextColor(Color.parseColor(textProject.getTextColor()));
        colorPickerTextView.setBackgroundColor(Color.parseColor(textProject.getTextColor()));
        switchMirror.setChecked(textProject.getMirrorMode());
        setMirrorMode(playText, textProject.getMirrorMode());
        seekbarScrollingSpeed.setProgress(textProject.getScrollSpeed());
        scrollSpeedDelay = MAX - textProject.getScrollSpeed();
        switchMirror.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.getId() == R.id.switch_mirror_mode) {
                setMirrorMode(playText, isChecked);
            }
        });

        seekbarTextSize.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playText.setTextSize(getTextSizeByProgress(progress));
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

        playControl.setOnClickListener(v -> {
            if (runnable != null) {
                handler.removeCallbacksAndMessages(null);
                runnable = null;
                playControl.setImageDrawable(getDrawable(R.drawable.ic_play));
            } else {
                startScrolling(handler, scrollView);
                playControl.setImageDrawable(getDrawable(R.drawable.ic_pause));
            }
        });

        playText.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (runnable != null) {
                        handler.removeCallbacksAndMessages(null);
                        runnable = null;
                        playControl.setImageDrawable(getDrawable(R.drawable.ic_play));
                    } else {
                        startScrolling(handler, scrollView);
                        playControl.setImageDrawable(getDrawable(R.drawable.ic_pause));
                    }
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (controls.getVisibility() == View.VISIBLE) {
                        controls.setVisibility(View.GONE);
                    } else {
                        controls.setVisibility(View.VISIBLE);
                    }
                    return super.onSingleTapConfirmed(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    private void startScrolling(Handler handler, ScrollView scrollView) {
        runnable = () -> {
            scrollView.smoothScrollBy(0, 1);
            handler.postDelayed(runnable, scrollSpeedDelay);
        };
        handler.postDelayed(runnable, scrollSpeedDelay);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        model.scrollPosition = scrollView.getVerticalScrollbarPosition() / scrollView.getHeight();
        super.onDestroy();
    }

    @OnClick({R.id.color_picker_text})
    public void onTextColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            model.textProject.setTextColor("#" + envelope.getHexCode());
            int color = envelope.getColor();
            view.setBackgroundColor(color);
            playText.setTextColor(color);
        });
    }

    @OnClick({R.id.color_picker_background})
    public void onBackgroundColorClicked(View view) {
        showColorPickerDialog(view, (envelope, fromUser) -> {
            model.textProject.setBackgroundColor("#" + envelope.getHexCode());
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
}

