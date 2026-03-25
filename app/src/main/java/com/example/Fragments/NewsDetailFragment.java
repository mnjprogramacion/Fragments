package com.example.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class NewsDetailFragment extends Fragment {

    private static final String ARG_NEWS = "news";

    public static NewsDetailFragment newInstance(NewsItem item) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NEWS, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_detail, container, false);

        NewsItem item = null;
        if (getArguments() != null) {
            item = (NewsItem) getArguments().getSerializable(ARG_NEWS);
        }
        if (item == null) return view;

        TextView txtTitulo = view.findViewById(R.id.txtTitulo);
        TextView txtDescripcion = view.findViewById(R.id.txtDescripcion);
        TextView txtContenido = view.findViewById(R.id.txtContenido);
        ImageView imgDetalle = view.findViewById(R.id.imgDetalle);

        txtTitulo.setText(item.titulo);
        txtDescripcion.setText(item.descripcion);
        txtContenido.setText(item.contenido);

        if (item.imagenGrandeUrl != null && !item.imagenGrandeUrl.isEmpty()) {
            Picasso.get().load(item.imagenGrandeUrl).placeholder(android.R.drawable.ic_menu_gallery).into(imgDetalle);
        }

        return view;
    }
}
