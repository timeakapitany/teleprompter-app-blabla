package com.example.blabla.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.blabla.R;
import com.example.blabla.model.TextProject;
import com.example.blabla.ui.create.CreateTextActivity;


/**
 * Implementation of App Widget functionality.
 */
public class CreateTextAppWidget extends AppWidgetProvider {


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blabla", Context.MODE_PRIVATE);
        TextProject dummyProject = new TextProject();
        dummyProject.setTextId(null);
        dummyProject.setBackgroundColor(sharedPreferences.getString(context.getString(R.string.preference_background_color), context.getString(R.string.default_background)));
        dummyProject.setTextColor(sharedPreferences.getString(context.getString(R.string.preference_text_color), context.getString(R.string.default_text_color)));
        dummyProject.setTextSize(sharedPreferences.getInt(context.getString(R.string.preference_text_size), context.getResources().getInteger(R.integer.default_text_size)));
        dummyProject.setScrollSpeed(sharedPreferences.getInt(context.getString(R.string.preference_scroll_speed), context.getResources().getInteger(R.integer.default_scroll_speed)));
        dummyProject.setMirrorMode(sharedPreferences.getBoolean(context.getString(R.string.preference_mirror_mode), context.getResources().getBoolean(R.bool.default_mirror)));

        Intent intent = CreateTextActivity.newIntent(context, dummyProject);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.create_text_app_widget);

        views.setOnClickPendingIntent(R.id.appwidget_add, pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

