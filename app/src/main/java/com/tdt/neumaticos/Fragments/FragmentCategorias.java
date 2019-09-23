package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.tdt.neumaticos.Adapter.PeliculasAdapterRecyclerView;
import com.tdt.neumaticos.Clases.Consultas;
import com.tdt.neumaticos.Model.PeliculaCardView;
import com.tdt.neumaticos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FragmentCategorias extends Fragment{

    private static final String KEY_TITLE="Categorías";
    private static String SERVIDOR;

    public View vista;

    String categoria;
    String cat1,cat2,ano1,ano2;
    String id_usuario;

    RequestQueue requestQueue;
    boolean respuestaBD=false;
    boolean tienePeliculas=false;

    boolean encontroPeliculas=false;

    public FragmentCategorias() {
        // Required empty public constructor

    }

    public static FragmentCategorias newInstance(String param1) {
        FragmentCategorias fragment = new FragmentCategorias();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, param1);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view=inflater.inflate(R.layout.fragment_fragment_categorias, container, false);

        vista=view;

        obtenerUsuario();
        obtenerServidor();

        Consultas consultas = new Consultas();

        final TextInputEditText text= view.findViewById(R.id.TIbuscar);
        Button button = view.findViewById(R.id.buttonBuscar);


        categoria=  consultas.categoria(getArguments().getString(KEY_TITLE));

        //OBTENER DATOS PARA EL USUARIO ESPECIFICO
        buscarPeliculasUsuario();


        button.requestFocus();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!text.getText().toString().isEmpty())
                {
                    String pelicula = text.getText().toString();
                    BuscarPeliculas(SERVIDOR +"buscarPeliculaNombre.php?titulo="+pelicula,view);
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Escribe un titulo para buscar", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title= getArguments().getString(KEY_TITLE);
    }



    public void obtenerServidor()
    {
        SharedPreferences sharedPref =  getContext().getSharedPreferences("DireccionIP",Context.MODE_PRIVATE);
        SERVIDOR = sharedPref.getString("ip","null");
    }

    private void BuscarPeliculas(String URL, final View view)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                if(cantidad>0)
                {
                    respuestaBD=true;
                    crearListView(view,response);
                }
                else
                {
                    respuestaBD=false;
                    Log.d("edgarTest","valor entro else: "+String.valueOf(respuestaBD));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), "NO SE ENCONTRARON COINCIDENCIAS", Toast.LENGTH_LONG).show();
            }
        }
        );
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);
    }

    public void crearListView(View view,JSONArray response)
    {
            RecyclerView peliculasRecyclerView = view.findViewById(R.id.peliculasRecycler);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            peliculasRecyclerView.setLayoutManager(linearLayoutManager);

            PeliculasAdapterRecyclerView peliculasAdapterRecyclerView = new PeliculasAdapterRecyclerView(buildPictures(response),R.layout.cardview_pelicula,getActivity());

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

                //Log.d("NOMBRE",nombreImagen);

                peliculaCardViews.add(new PeliculaCardView( SERVIDOR+"/Caratulas/"+nombreImagen ,titulo,autor,genero,id_pelicula,"x"));

            } catch (JSONException e) {
                Log.d("SALIDA",e.toString());
                respuestaBD=false;
            }
        }

        return peliculaCardViews;
    }

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        id_usuario = sharedPref.getString("user_id","null");
    }

    public void buscarPeliculasUsuario()
    {
        String URL= SERVIDOR +"buscarPeliculasUsuario.php?idusuario="+id_usuario;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                String generos="";
                String ano;
                ArrayList<Integer> anos = new ArrayList<>();

                if(cantidad>0)
                {
                    JSONObject jsonObject = null;

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            generos+=jsonObject.getString("GENERO")+", ";
                            ano=jsonObject.getString("ANO");
                            anos.add( Integer.parseInt(ano) );

                        } catch (JSONException e) {
                            Log.d("SALIDA",e.toString());
                        }
                    }

                    encontroPeliculas=true;
                    ejecutarConsulta(generos,anos);
                }
                else
                {
                    encontroPeliculas=false;
                    ejecutarConsulta(generos,anos);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                encontroPeliculas=false;
                ejecutarConsulta(null,null);
            }
        }
        );
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    public void ejecutarConsulta(String lineaGeneros, ArrayList<Integer> arrayAnos)
    {
        cat1=categoria;
        cat2=categoria;
        ano1="%";
        ano2="%";

        if(encontroPeliculas)
        {
            ArrayList<String> arrayGeneros= new ArrayList<>();

            String palabra="";
            for(int i=0;i<lineaGeneros.length();i++)
            {
                if(lineaGeneros.charAt(i)!=',') {
                    palabra += lineaGeneros.charAt(i);
                }
                else
                {
                    arrayGeneros.add(palabra);
                    palabra="";
                    i++;
                }
            }


            String genero1="",genero2="";
            String año1="", año2="";
            if(arrayGeneros.size()>2)
            {
                genero1= arrayGeneros.get(new Random().nextInt(arrayGeneros.size()));
                genero2= arrayGeneros.get(arrayGeneros.size()-1);
            }
            else
            {
                genero1= arrayGeneros.get(arrayGeneros.size()-1);
                genero2= arrayGeneros.get(arrayGeneros.size()-2);
            }

            Collections.sort(arrayAnos);

            if(arrayAnos.size()<2)
            {
                año1 = arrayAnos.get(0).toString();
                año2 = arrayAnos.get(0).toString();
            }
            else
            {
                año1= arrayAnos.get(arrayAnos.size()-1).toString();
                año2= arrayAnos.get(new Random().nextInt(arrayAnos.size())).toString();
            }


            for(int i=0; i<arrayGeneros.size();i++)
                Log.d("FILTRAR",arrayGeneros.get(i));

            for(int i=0; i<arrayAnos.size();i++)
                Log.d("FILTRAR",arrayAnos.get(i).toString());


            Log.d("FILTRAR",genero1 +" "+genero2+" "+año1+" "+año2);

            if(categoria.equals("*"))
            {
                cat1=genero1;
                cat2=genero2;
                ano1=año1;
                ano2=año2;
            }
            else
            {
                if(categoria=="%")
                {
                    ano1="%";
                    cat2="%";
                }
                else
                {
                    ano1=año1;
                    ano2=año2;
                }
            }
        }
        else
        {
            if(categoria.equals("*"))
            {
                cat1="%";
                cat2="%";
            }
        }

        String consulta="buscarCategoria.php?cat1="+cat1+"&cat2="+cat2+"&ano1="+ano1+"&ano2="+ano2;

        BuscarPeliculas(SERVIDOR +consulta,vista);
    }

}
