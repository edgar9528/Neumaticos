package com.tdt.neumaticos.Model;

public class AmigoCardView {
    private String correo;
    private String nombre;
    private String id_usuario;
    private String id_usuarioActual;
    private String identificador;

    public AmigoCardView(String correo, String nombre, String id_usuario, String id_usuarioActual, String identificador) {
        this.correo = correo;
        this.nombre = nombre;
        this.id_usuario = id_usuario;
        this.id_usuarioActual = id_usuarioActual;
        this.identificador = identificador;
    }

    public void setId_usuarioActual(String id_usuarioActual) {
        this.id_usuarioActual = id_usuarioActual;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getId_usuarioActual() {
        return id_usuarioActual;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }
}
