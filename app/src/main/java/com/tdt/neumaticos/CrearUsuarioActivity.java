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

    TextView textView[] = new TextView[5];

    public static  String SERVIDOR="";
    public String genero;

    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        this.setTitle("Registrarse");

        obtenerServidor();
        final String URL= SERVIDOR +"insertarUsuario.php";

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] opciones = {"MASCULINO","FEMENINO"};
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                genero= (String) adapterView.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });


        Button button = findViewById(R.id.ButtonCrearUsuario);

        //NOMBRE,CORREO,CONTRASEÑA,CONTRASEÑA2,EDAD, GENERO
        textView[0] = findViewById(R.id.TextViewUserNombre);
        textView[1] = findViewById(R.id.TextViewUserCorreo);
        textView[2] = findViewById(R.id.TextViewUserContraseña);
        textView[3] = findViewById(R.id.TextViewUserVerificarContraseña);
        textView[4] = findViewById(R.id.TextViewUserEdad);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean llenos=true;
                for(int i=0; i<5;i++)
                {
                    if ( textView[i].getText().length()==0 || textView[i].getText()==" "  )
                        llenos=false;
                }

                if(llenos)
                {
                    if(textView[2].getText().toString().equals( textView[3].getText().toString() )  )
                    {
                        if (textView[1].getText().toString().contains("@")) {
                            boolean entero = false;
                            try {
                                Integer.parseInt(textView[4].getText().toString());
                                entero = true;
                            } catch (Exception e) {
                                Log.d("SALIDA", "No se pudo convertir");
                                entero = false;
                            }
                            if (entero) {
                                ejecutarServicio(URL);
                            } else
                                Toast.makeText(CrearUsuarioActivity.this, "Inserte una edad validad", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(CrearUsuarioActivity.this, "No tiene formato de correo", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(CrearUsuarioActivity.this, textView[2].getText().toString()+" "+textView[3].getText().toString(), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(CrearUsuarioActivity.this,"Rellena todos los campos",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void ejecutarServicio(String URL)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Usuario agregado", Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();

                parametros.put("NOMBRE", textView[0].getText().toString());
                parametros.put("CORREO",textView[1].getText().toString());
                parametros.put("CONTRASENA",textView[2].getText().toString());
                parametros.put("EDAD",textView[4].getText().toString());
                parametros.put("GENERO",genero);

                return parametros;
            }
        };
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void obtenerServidor()
    {
        SharedPreferences sharedPref = getSharedPreferences("DireccionIP",Context.MODE_PRIVATE);
        SERVIDOR = sharedPref.getString("ip","null");
    }

}
