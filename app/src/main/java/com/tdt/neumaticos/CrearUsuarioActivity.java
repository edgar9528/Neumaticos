package com.tdt.neumaticos;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioActivity extends AppCompatActivity {

    EditText et_servidor,et_puerto;
    Button button_regresar,button_guardar;
    String servidor,puerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        this.setTitle("Configuración");

        et_servidor = findViewById(R.id.et_servidor);
        et_puerto = findViewById(R.id.et_puerto);

        button_guardar = findViewById(R.id.button_guardar);
        button_regresar = findViewById(R.id.button_regresar);

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
                }
                else
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", Toast.LENGTH_LONG).show();
            }
        });

        button_regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
