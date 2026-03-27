package com.example.Fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsCheckWorker extends Worker {

    private static final String CHANNEL_ID = "news_channel";
    private static final String CSV_BASE_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRr3-LdTANTCbj0ojujZRxBttAdkwjLtwIIY7XT4SMCDZoBLSW8NqAQPS7BfuO2nn5AByiqS31x6kaJ/pub?output=csv&gid=";

    public NewsCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String gid = getApplicationContext().getString(R.string.csv_gid);
            URL url = new URL(CSV_BASE_URL + gid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            int count = 0;
            boolean first = true;
            while (reader.readLine() != null) {
                if (first) { first = false; continue; }
                count++;
            }
            reader.close();

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("news_prefs", Context.MODE_PRIVATE);
            int lastCount = prefs.getInt("last_news_count", 0);

            if (lastCount > 0 && count > lastCount) {
                showNotification();
            }
            prefs.edit().putInt("last_news_count", count).apply();

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void showNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getApplicationContext().getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getApplicationContext().getString(R.string.notification_title))
                .setContentText(getApplicationContext().getString(R.string.notification_text))
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
