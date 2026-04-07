package com.example.Fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Make status bar icons dark in light mode, matching the rest of the app
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        boolean isNightMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isNightMode);

        if (savedInstanceState == null) {
            NewsItem item = (NewsItem) getIntent().getSerializableExtra("news");
            if (item != null) {
                NewsDetailFragment detail = NewsDetailFragment.newInstance(item);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailContainer, detail)
                        .commit();
            }
        }
    }
}
