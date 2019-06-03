package com.example.blabla.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences("blabla", Context.MODE_PRIVATE);
    }
}
