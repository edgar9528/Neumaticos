package com.tdt.neumaticos;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zebra.rfid.api3.*;

import java.util.ArrayList;

public class ConfiguracionActivity extends AppCompatActivity {

    EditText et_servidor,et_puerto;
    Button button_regresar,button_guardar;
    String servidor,puerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        this.setTitle("Configuración");

        et_servidor = findViewById(R.id.et_servidor);
        et_puerto = findViewById(R.id.et_puerto);

        button_guardar = findViewById(R.id.button_guardar_conf);
        button_regresar = findViewById(R.id.button_regresar_conf);

        verificarServidor();

        button_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                servidor = et_servidor.getText().toString();
                puerto = et_puerto.getText().toString();
                if(!servidor.isEmpty()&&!puerto.isEmpty())
                {
                    guardarServidor();
                    Toast.makeText(getApplicationContext(), "Información guardada", Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", Toast.LENGTH_LONG).show();
            }
        });

        button_regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void guardarServidor()
    {
        SharedPreferences sharedPref = getSharedPreferences("ServidorPreferences",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("servidor",servidor);
        editor.putString("puerto",puerto);
        editor.apply();
    }

    public void verificarServidor()
    {
        SharedPreferences sharedPref = getSharedPreferences("ServidorPreferences",Context.MODE_PRIVATE);
        servidor = sharedPref.getString("servidor","null");
        puerto = sharedPref.getString("puerto","null");

        if(!servidor.equals("null"))
        {
            et_servidor.setText(servidor);
            et_puerto.setText(puerto);
        }

    }


}
