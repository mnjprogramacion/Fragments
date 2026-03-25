package com.example.Fragments;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        NewsItem item = (NewsItem) getIntent().getSerializableExtra("news");
        if (item == null) return;

        TextView txtTitulo = findViewById(R.id.txtTitulo);
        TextView txtDescripcion = findViewById(R.id.txtDescripcion);
        TextView txtContenido = findViewById(R.id.txtContenido);
        ImageView imgDetalle = findViewById(R.id.imgDetalle);

        txtTitulo.setText(item.titulo);
        txtDescripcion.setText(item.descripcion);
        txtContenido.setText(item.contenido);

        if (item.imagenGrandeUrl != null && !item.imagenGrandeUrl.isEmpty()) {
            Picasso.get().load(item.imagenGrandeUrl).placeholder(android.R.drawable.ic_menu_gallery).into(imgDetalle);
        }
    }
}
