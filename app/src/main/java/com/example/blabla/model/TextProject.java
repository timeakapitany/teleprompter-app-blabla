package com.example.blabla.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.blabla.R;
import com.example.blabla.util.SharedPrefUtil;
import com.google.firebase.Timestamp;

public class TextProject implements Parcelable {

    private String textTitle;
    private String backgroundColor;
    private String textColor;
    private Integer textSize;
    private Integer scrollSpeed;
    private Boolean mirrorMode;
    private String textReference;
    private String textId;
    private Timestamp creationDate;

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public Integer getTextSize() {
        return textSize;
    }

    public void setTextSize(Integer textSize) {
        this.textSize = textSize;
    }

    public Integer getScrollSpeed() {
        return scrollSpeed;
    }

    public void setScrollSpeed(Integer scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public Boolean getMirrorMode() {
        return mirrorMode;
    }

    public void setMirrorMode(Boolean mirrorMode) {
        this.mirrorMode = mirrorMode;
    }

    public String getTextReference() {
        return textReference;
    }

    public void setTextReference(String textReference) {
        this.textReference = textReference;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.textTitle);
        dest.writeString(this.backgroundColor);
        dest.writeString(this.textColor);
        dest.writeValue(this.textSize);
        dest.writeValue(this.scrollSpeed);
        dest.writeValue(this.mirrorMode);
        dest.writeString(this.textReference);
        dest.writeString(this.textId);
        dest.writeParcelable(this.creationDate, flags);
    }

    public TextProject() {
    }

    protected TextProject(Parcel in) {
        this.textTitle = in.readString();
        this.backgroundColor = in.readString();
        this.textColor = in.readString();
        this.textSize = (Integer) in.readValue(Integer.class.getClassLoader());
        this.scrollSpeed = (Integer) in.readValue(Integer.class.getClassLoader());
        this.mirrorMode = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.textReference = in.readString();
        this.textId = in.readString();
        this.creationDate = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<TextProject> CREATOR = new Creator<TextProject>() {
        @Override
        public TextProject createFromParcel(Parcel source) {
            return new TextProject(source);
        }

        @Override
        public TextProject[] newArray(int size) {
            return new TextProject[size];
        }
    };


    public static TextProject createDummyTextProject(Context context) {
        SharedPreferences sharedPreferences = SharedPrefUtil.getSharedPref(context);

        TextProject dummy = new TextProject();
        dummy.setTextId(null);
        dummy.setBackgroundColor(sharedPreferences.getString(context.getString(R.string.preference_background_color), context.getString(R.string.default_background)));
        dummy.setTextColor(sharedPreferences.getString(context.getString(R.string.preference_text_color), context.getString(R.string.default_text_color)));
        dummy.setTextSize(sharedPreferences.getInt(context.getString(R.string.preference_text_size), context.getResources().getInteger(R.integer.default_text_size)));
        dummy.setScrollSpeed(sharedPreferences.getInt(context.getString(R.string.preference_scroll_speed), context.getResources().getInteger(R.integer.default_scroll_speed)));
        dummy.setMirrorMode(sharedPreferences.getBoolean(context.getString(R.string.preference_mirror_mode), context.getResources().getBoolean(R.bool.default_mirror)));
        return dummy;
    }
}
