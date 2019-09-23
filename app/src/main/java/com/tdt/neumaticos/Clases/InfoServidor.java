package com.tdt.neumaticos.Clases;

import android.content.Context;
import android.content.SharedPreferences;

public class InfoServidor {

    public String servidor;
    public int puerto;
    public Context context;

    public InfoServidor(Context context) {
        this.context = context;

        SharedPreferences sharedPref = context.getSharedPreferences("ServidorPreferences",Context.MODE_PRIVATE);
        servidor = sharedPref.getString("servidor","null");
        puerto = Integer.parseInt( sharedPref.getString("puerto","0") );

    }

    public String getServidor() {
        return servidor;
    }

    public int getPuerto() {
        return puerto;
    }
}
