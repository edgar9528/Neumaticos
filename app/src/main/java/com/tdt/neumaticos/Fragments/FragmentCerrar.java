package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tdt.neumaticos.LoginActivity;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.PeliculasVistasActivity;
import com.tdt.neumaticos.R;

public class FragmentCerrar extends Fragment {

    private static final String KEY_TITLE="Perfil";
    private static String titulo;

    String user_id="",user_nombre="",user_correo="",user_contrasena="";

    public FragmentCerrar() {
        // Required empty public constructor
    }

    public static FragmentCerrar newInstance(String param1) {
        FragmentCerrar fragment = new FragmentCerrar();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, param1);
        fragment.setArguments(args);

        titulo=param1;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_cerrar, container, false);

        obtenerUsuario();

        if(titulo.equals( "Perfil|Cerrar sesión" ) )
        {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(getContext());
            dialogo1.setTitle("Importante");
            dialogo1.setMessage("¿Seguro que desea cerrar sesión?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {


                    //REESTABLECER EL USUARIO

                    SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user_id","null");
                    editor.putString("user_nombre","null");
                    editor.putString("user_correo","null");
                    editor.putString("user_contrasena","null");
                    editor.apply();

                    //REGRESAR A PANTALLA INICIO SESION
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();

                }
            });
            dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
            dialogo1.show();
        }
        else
        {
            Intent intent = new Intent(getActivity(),PeliculasVistasActivity.class);
            intent.putExtra("id", user_id);
            getActivity().startActivity(intent);
        }



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title= getArguments().getString(KEY_TITLE);
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
