package com.tdt.neumaticos.Clases;

public class WSValorRetorno {
    private boolean valor;
    private String mensaje;

    public WSValorRetorno(boolean valor, String mensaje) {
        this.valor = valor;
        this.mensaje = mensaje;
    }

    public void setValor(boolean valor) {
        this.valor = valor;
    }

    public boolean isValor() {
        return valor;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return "Valor:" + this.valor + " |Mensaje:" + this.mensaje;
    }
}
