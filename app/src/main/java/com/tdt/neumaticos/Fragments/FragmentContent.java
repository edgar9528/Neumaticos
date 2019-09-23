package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tdt.neumaticos.R;

public class FragmentContent extends Fragment {

    private static final String KEY_TITLE="Content";

    String id_user="", nombre_user="", correo_user="",contrasena_user="",edad_user="",genero_user="";

    public FragmentContent() {
        // Required empty public constructor
    }

    public static FragmentContent newInstance(String param1) {
        FragmentContent fragment = new FragmentContent();
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
        View view = inflater.inflate(R.layout.fragment_fragment_content, container, false);

        obtenerUsuario();

        TextView[] textViews = new TextView[5];

        textViews[0] = view.findViewById(R.id.PerfilNombre);
        textViews[1] = view.findViewById(R.id.PerfilCorreo);
        textViews[2] = view.findViewById(R.id.PerfilContraseña);
        textViews[3] = view.findViewById(R.id.PerfilEdad);
        textViews[4] = view.findViewById(R.id.PerfilGenero);

        textViews[0].setText(nombre_user);
        textViews[1].setText(correo_user);
        textViews[2].setText(contrasena_user);
        textViews[3].setText(edad_user);
        textViews[4].setText(genero_user);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        id_user = sharedPref.getString("user_id","null");
        nombre_user= sharedPref.getString("user_nombre","null");
        correo_user=sharedPref.getString("user_correo","null");
        contrasena_user=sharedPref.getString("user_contrasena","null");
        edad_user=sharedPref.getString("user_edad","null");
        genero_user=sharedPref.getString("user_genero","null");
    }
}
