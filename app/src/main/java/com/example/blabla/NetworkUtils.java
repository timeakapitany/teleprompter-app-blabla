package com.example.blabla;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static String downloadData(String urlPath) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int response = connection.getResponseCode();
            Timber.d("downloadData response code is " + response);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            int charsRead;
            char[] inputBuffer = new char[500];
            while (true) {
                charsRead = reader.read(inputBuffer);
                if (charsRead < 0) {
                    break;
                }
                if (charsRead > 0) {
                    stringBuilder.append(String.copyValueOf(inputBuffer), 0, charsRead);
                }
            }
            reader.close();
            return stringBuilder.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "downloadData: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "downloadData: IO exception reading data " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "downloadData: Security exception: needs permission " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
