package com.tdt.neumaticos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetallePeliculaActivity extends AppCompatActivity {

    public static  String SERVIDOR="";

    TextView textView[] = new TextView[7];
    TextView estrellas[]= new TextView[5];
    String datos[];

    String id_pelicula,id_usuario,calificacion;
    String id_usuarioRecibido;

    RequestQueue requestQueue;

    boolean calificada=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pelicula);
        this.setTitle("Detalles de película");

        String id = getIntent().getExtras().getString("id");
        id_usuarioRecibido = getIntent().getExtras().getString("idUsuarioRecibido");



        id_pelicula=id;

        obtenerServidor();
        obtenerUsuario();

        if(id_usuarioRecibido.equals("x"))
            obtenerCalificacion(SERVIDOR +"buscarCalificacion.php?idusuario="+id_usuario+"&idpelicula="+id_pelicula);
        else
            obtenerCalificacion(SERVIDOR +"buscarCalificacion.php?idusuario="+id_usuarioRecibido+"&idpelicula="+id_pelicula);

        datos=new String[7];

        //PONER IMAGEN DE CARATULA
        ImageView imageView = findViewById(R.id.IVimagenPortada);
        String nombreImagen="";
        for(int j=0; j<6-id.length();j++)
            nombreImagen+="0";
        nombreImagen+=id+".jpg";
        Picasso.with(getApplicationContext()).load(SERVIDOR+"Caratulas/"+nombreImagen).into(imageView);


        textView[0]=findViewById(R.id.TVpelicula_titulo);
        textView[1]=findViewById(R.id.TVpelicula_genero);
        textView[2]=findViewById(R.id.TVpelicula_ano);
        textView[3]=findViewById(R.id.TVpelicula_duracion);
        textView[4]=findViewById(R.id.TVpelicula_director);
        textView[5]=findViewById(R.id.TVpelicula_argumento);
        textView[6]=findViewById(R.id.TVpelicula_trailer);

        BuscarPelicula(SERVIDOR +"buscarPelicula.php?id="+id);

        textView[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PRUEBA",datos[6]);
                Uri uri = Uri.parse("http://"+datos[6]);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        estrellas[0]= findViewById(R.id.TVestrella1);
        estrellas[1]= findViewById(R.id.TVestrella2);
        estrellas[2]= findViewById(R.id.TVestrella3);
        estrellas[3]= findViewById(R.id.TVestrella4);
        estrellas[4]= findViewById(R.id.TVestrella5);

        estrellas[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_usuarioRecibido.equals("x"))
                    MensajeEmergente(1);
            }
        });

        estrellas[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_usuarioRecibido.equals("x"))
                    MensajeEmergente(2);
            }
        });

        estrellas[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_usuarioRecibido.equals("x"))
                    MensajeEmergente(3);
            }
        });

        estrellas[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_usuarioRecibido.equals("x"))
                    MensajeEmergente(4);
            }
        });

        estrellas[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_usuarioRecibido.equals("x"))
                    MensajeEmergente(5);
            }
        });

    }

    public void MensajeEmergente(final int cantidad)
    {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this,R.style.CustomAlertDialog);
        dialogo1.setTitle("Puntuación");
        String mensaje="¿Desea calificar con "+cantidad+" estrellas?\n";
        for(int i=0; i<cantidad;i++)
            mensaje+="★";

        for(int i=0; i<5-cantidad;i++)
            mensaje+="☆";

        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                agregarCalificacion(cantidad);
            }
        });
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                //cancelar();
            }
        });
        dialogo1.show();
    }

    private void BuscarPelicula(String URL)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                if(cantidad>0)
                {
                    JSONObject jsonObject = null;

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            datos[0]= jsonObject.getString("TITULO");
                            datos[1]=jsonObject.getString("GENERO");
                            datos[2]= jsonObject.getString("ANO");
                            datos[3]=jsonObject.getString("DURACION");
                            datos[4]=jsonObject.getString("DIRECTORES");
                            datos[5]=jsonObject.getString("ARGUMENTO");
                            datos[6]=jsonObject.getString("TRAILER");
                            //Toast.makeText(getApplication().getApplicationContext(), datos[0], Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplication().getApplicationContext(), "Error "+e, Toast.LENGTH_LONG).show();
                        }
                    }

                    textView[0].setText(datos[0]);
                    textView[1].setText(datos[1]);
                    textView[2].setText(datos[2]);
                    textView[3].setText(datos[3]);
                    textView[4].setText(datos[4]);
                    textView[5].setText(datos[5]);
                    textView[6].setText(datos[6]);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication().getApplicationContext(), "OCURRIO UN ERROR", Toast.LENGTH_LONG).show();
            }
        }
        );
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    public void agregarCalificacion(int cantidad)
    {
        if(!calificada)
        {
            calificacion= String.valueOf(cantidad);
            String URL= SERVIDOR +"insertarCalificacion.php";
            insertarCalificacion(URL);
        }
        else
        {
            calificacion= String.valueOf(cantidad);
            String URL= SERVIDOR +"actualizarCalificacion.php";
            insertarCalificacion(URL);
        }

    }

    private void insertarCalificacion(String URL)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.isEmpty())
                {
                    if(!calificada)
                    {
                        Toast.makeText(getApplicationContext(), "Calificación agregada", Toast.LENGTH_LONG).show();
                        calificada=true;
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Calificación actualizada", Toast.LENGTH_LONG).show();
                    agregarEstrellas();
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

                parametros.put("USUARIO", id_usuario);
                parametros.put("PELICULA",id_pelicula);
                parametros.put("CALIFICACION",calificacion);

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

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        id_usuario = sharedPref.getString("user_id","null");
    }

    public void obtenerCalificacion(String URL)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                if(cantidad>0)
                {
                    JSONObject jsonObject = null;

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            calificacion=jsonObject.getString("CALIFICACION");

                        } catch (JSONException e) {
                            Toast.makeText(getApplication().getApplicationContext(), "Error "+e, Toast.LENGTH_LONG).show();
                        }
                    }
                    agregarEstrellas();
                    calificada=true;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                agregarEstrellas();
            }
        }
        );
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    public void agregarEstrellas()
    {
        if(calificacion!=null)
        {
            int cal= Integer.parseInt(calificacion);
            for(int i=0; i<5;i++)
            {
                if(i<cal)
                    estrellas[i].setText("★");
                else
                    estrellas[i].setText("☆");
            }
        }
        else
        {
            for(int i=0; i<5;i++)
                estrellas[i].setText("☆");
        }

    }

}
