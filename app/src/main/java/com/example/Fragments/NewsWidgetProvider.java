package com.example.Fragments;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.Random;

public class NewsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_news);

        // TextClock se actualiza automáticamente — no hace falta setear la hora manualmente

        // Noticia aleatoria de la lista guardada por NewsListFragment
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        String allTitles = prefs.getString("all_titles", "");
        String allDescs  = prefs.getString("all_descs",  "");
        String title, desc;
        if (!allTitles.isEmpty()) {
            String[] titles = allTitles.split("§", -1);
            String[] descs  = allDescs.split("§",  -1);
            int idx = new Random().nextInt(titles.length);
            title = titles[idx];
            desc  = (idx < descs.length) ? descs[idx] : "";
        } else {
            title = context.getString(R.string.select_news);
            desc  = "";
        }
        views.setTextViewText(R.id.txtWidgetTitle, title);
        views.setTextViewText(R.id.txtWidgetDesc, desc);

        // Al pulsar el widget, abre la app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, pending);

        manager.updateAppWidget(widgetId, views);
    }
}
