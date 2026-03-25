package com.example.Fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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
