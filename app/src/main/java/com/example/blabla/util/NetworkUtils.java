package com.example.blabla.util;

import android.content.Context;
import android.net.ConnectivityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class NetworkUtils {

    public static String downloadData(String urlPath) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int response = connection.getResponseCode();
            Timber.d("downloadData response code is %s", response);

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
            Timber.e("downloadData: Invalid URL %s", e.getMessage());
        } catch (IOException e) {
            Timber.e("downloadData: IO exception reading data %s", e.getMessage());
        } catch (SecurityException e) {
            Timber.e("downloadData: Security exception: needs permission %s", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }
}
