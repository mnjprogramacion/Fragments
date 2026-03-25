package com.example.Fragments;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class NewsBaseAdapter extends BaseAdapter {

    private final List<NewsItem> items;

    public NewsBaseAdapter(List<NewsItem> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NewsItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // La vista la proporciona el wrapper, no el adaptador base
        throw new UnsupportedOperationException("Usa NewsAdapterWrapper para obtener vistas");
    }
}
