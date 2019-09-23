package com.tdt.neumaticos;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.tdt.neumaticos.Adapter.PeliculasAdapterRecyclerView;
import com.tdt.neumaticos.Model.PeliculaCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PeliculasVistasActivity extends AppCompatActivity {

    private static String SERVIDOR;

    RequestQueue requestQueue;

    String id_usuarioRecibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Amigos");

        obtenerServidor();

        setContentView(R.layout.activity_peliculas_vistas);
        id_usuarioRecibido = getIntent().getExtras().getString("id");

        BuscarPeliculas(SERVIDOR +"buscarPeliculasUsuario.php?idusuario="+id_usuarioRecibido);



    }

    private void BuscarPeliculas(String URL)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                if(cantidad>0)
                {
                    crearListView(response);
                }
                else
                {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "NO SE ENCONTRARON COINCIDENCIAS", Toast.LENGTH_LONG).show();
            }
        }
        );
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }


    public void crearListView(JSONArray response)
    {
        RecyclerView peliculasRecyclerView = findViewById(R.id.peliculsVistasRecycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        peliculasRecyclerView.setLayoutManager(linearLayoutManager);

        PeliculasAdapterRecyclerView peliculasAdapterRecyclerView = new PeliculasAdapterRecyclerView(buildPictures(response),R.layout.cardview_pelicula,this);

        peliculasRecyclerView.setAdapter(peliculasAdapterRecyclerView);
    }

    public ArrayList<PeliculaCardView> buildPictures(JSONArray response)
    {
        ArrayList<PeliculaCardView> peliculaCardViews = new ArrayList<>();

        JSONObject jsonObject = null;
        String titulo, autor, genero,id_pelicula;
        for (int i = 0; i < response.length(); i++) {
            try {
                jsonObject = response.getJSONObject(i);
                titulo= jsonObject.getString("TITULO");
                autor= jsonObject.getString("DIRECTORES");
                genero=jsonObject.getString("GENERO");
                id_pelicula=jsonObject.getString("ID_PELICULA");

                String nombreImagen="";
                for(int j=0; j<6-id_pelicula.length();j++)
                    nombreImagen+="0";
                nombreImagen+=id_pelicula+".jpg";


                peliculaCardViews.add(new PeliculaCardView( SERVIDOR+"/Caratulas/"+nombreImagen ,titulo,autor,genero,id_pelicula,id_usuarioRecibido));

            } catch (JSONException e) {
                Log.d("SALIDA",e.toString());
            }
        }

        return peliculaCardViews;
    }

    public void obtenerServidor()
    {
        SharedPreferences sharedPref =  getSharedPreferences("DireccionIP",Context.MODE_PRIVATE);
        SERVIDOR = sharedPref.getString("ip","null");
    }

}
