package com.example.Fragments;

import java.io.Serializable;

public class NewsItem implements Serializable {
    String titulo;
    String descripcion;
    String thumbnailUrl;
    String contenido;
    String imagenGrandeUrl;

    public NewsItem(String titulo, String descripcion, String thumbnailUrl, String contenido, String imagenGrandeUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.thumbnailUrl = thumbnailUrl;
        this.contenido = contenido;
        this.imagenGrandeUrl = imagenGrandeUrl;
    }
}
