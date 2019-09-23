package com.tdt.neumaticos.Model;

public class PeliculaCardView {
    private String imagen;
    private String titulo;
    private String  autor;
    private String  genero;
    private String id_pelicula;
    private String id_usuarioEnviar;

    public PeliculaCardView(String imagen, String titulo, String autor, String genero, String id_pelicula, String id_usuarioEnviar) {
        this.imagen = imagen;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.id_pelicula = id_pelicula;
        this.id_usuarioEnviar = id_usuarioEnviar;
    }

    public String getId_usuarioEnviar() {
        return id_usuarioEnviar;
    }

    public void setId_usuarioEnviar(String id_usuarioEnviar) {
        this.id_usuarioEnviar = id_usuarioEnviar;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getId_pelicula() {
        return id_pelicula;
    }

    public void setId_pelicula(String id_pelicula) {
        this.id_pelicula = id_pelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
