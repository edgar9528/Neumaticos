package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.tdt.neumaticos.Adapter.AmigosAdapterRecyclerView;
import com.tdt.neumaticos.Model.AmigoCardView;
import com.tdt.neumaticos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentAmigos extends Fragment {

    private static final String KEY_TITLE="Categorías";
    private static String SERVIDOR;
    private static String identificador;
    String correoBuscar;
    String user_id="",user_nombre="",user_correo="",user_contrasena="";

    RequestQueue requestQueue;

    public FragmentAmigos() {
        // Required empty public constructor
    }

    public static FragmentAmigos newInstance(String param1,String param2) {
        FragmentAmigos fragment = new FragmentAmigos();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, param1);
        identificador=param2;
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
        final View view = inflater.inflate(R.layout.fragment_fragment_amigos, container, false);

        //OBTENER SERVIDOR
        obtenerServidor();
        obtenerUsuario();

        correoBuscar="%";

        if(identificador.equals("buscar"))
        {
            BuscarAmigos(SERVIDOR +"buscarUsuarios.php?correo="+correoBuscar+"&idusuario="+user_id,view);
        }
        else
        {
            BuscarAmigos(SERVIDOR +"buscarUsuariosAmigos.php?correo="+correoBuscar+"&idusuario="+user_id,view);
        }

        final TextInputEditText textInputEditText = view.findViewById(R.id.TIbuscarAmigos);
        Button button = view.findViewById(R.id.buttonBuscarAmigos);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textInputEditText.getText().toString().isEmpty() )
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Debe escribir un correo a buscar", Toast.LENGTH_LONG).show();
                }
                else
                {
                    correoBuscar=textInputEditText.getText().toString();
                    if(identificador.equals("buscar"))
                    {
                        BuscarAmigos(SERVIDOR +"buscarUsuarios.php?correo="+correoBuscar+"&idusuario="+user_id,view);
                    }
                    else
                    {
                        BuscarAmigos(SERVIDOR +"buscarUsuariosAmigos.php?correo="+correoBuscar+"&idusuario="+user_id,view);
                    }
                }
            }
        });

        return view;
    }


    private void BuscarAmigos(String URL, final View view)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //INFORMACION RECUPERADA DE LA DB

                int cantidad=response.length();

                if(cantidad>0)
                {
                    crearListView(view,response);
                }
                else
                {
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
        RecyclerView amigosRecyclerView = view.findViewById(R.id.amigosRecycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        amigosRecyclerView.setLayoutManager(linearLayoutManager);

        AmigosAdapterRecyclerView amigosAdapterRecyclerView = new AmigosAdapterRecyclerView(buildPictures(response),R.layout.cardview_amigo,getActivity());

        amigosRecyclerView.setAdapter(amigosAdapterRecyclerView);
    }

    public ArrayList buildPictures(JSONArray response)
    {
        ArrayList<AmigoCardView> amigoCardViews = new ArrayList<>();

        JSONObject jsonObject = null;
        String correo, nombre, id_usuario;
        for (int i = 0; i < response.length(); i++) {
            try {
                jsonObject = response.getJSONObject(i);
                correo= jsonObject.getString("CORREO");
                nombre= jsonObject.getString("NOMBRE");
                id_usuario=jsonObject.getString("ID_USUARIO");

                amigoCardViews.add(new AmigoCardView(correo,nombre,id_usuario,user_id,identificador));

            } catch (JSONException e) {
                Log.d("SALIDA",e.toString());
            }
        }

        return amigoCardViews;
    }

    public void obtenerServidor()
    {
        SharedPreferences sharedPref =  getContext().getSharedPreferences("DireccionIP",Context.MODE_PRIVATE);
        SERVIDOR = sharedPref.getString("ip","null");
    }

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        user_id = sharedPref.getString("user_id","null");
        user_nombre = sharedPref.getString("user_nombre","null");
        user_correo = sharedPref.getString("user_correo","null");
        user_contrasena = sharedPref.getString("user_contrasena","null");
    }



}
