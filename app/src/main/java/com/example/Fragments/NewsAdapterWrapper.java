package com.example.Fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class NewsAdapterWrapper extends BaseAdapter {

    private final Context context;
    private final NewsBaseAdapter wrappedAdapter;
    private final int layoutRes;

    public NewsAdapterWrapper(Context context, NewsBaseAdapter wrappedAdapter, int layoutRes) {
        this.context = context;
        this.wrappedAdapter = wrappedAdapter;
        this.layoutRes = layoutRes;
    }

    @Override
    public int getCount() {
        return wrappedAdapter.getCount();
    }

    @Override
    public NewsItem getItem(int position) {
        return wrappedAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return wrappedAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean isNew = convertView == null;
        if (isNew) {
            convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        }
        // Tween animation (XML): slide desde abajo + fade solo en vistas recién creadas
        if (isNew) {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.item_slide_in);
            convertView.startAnimation(anim);
        }

        NewsItem item = wrappedAdapter.getItem(position);

        TextView txtTitulo = convertView.findViewById(R.id.txtTitulo);
        TextView txtDescripcion = convertView.findViewById(R.id.txtDescripcion);
        ImageView imgThumbnail = convertView.findViewById(R.id.imgThumbnail);

        txtTitulo.setText(item.titulo);
        txtDescripcion.setText(item.descripcion);

        if (item.thumbnailUrl != null && !item.thumbnailUrl.isEmpty()) {
            Picasso.get().load(item.thumbnailUrl)
                    .noPlaceholder()
                    .into(imgThumbnail);
        }

        return convertView;
    }
}
