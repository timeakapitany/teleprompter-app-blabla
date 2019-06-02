package com.example.blabla.util;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class DialogUtil {

    public static void createAlert(Context context, String message, DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, clickListener)
                .setCancelable(false)
                .create()
                .show();
    }


}
