package com.tdt.neumaticos.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tdt.neumaticos.R;

public class EntradaFragment extends Fragment {

    private static final String PARAMETRO="codigo";
    private static String tipo;
    private static String tipoVehiculo;
    private static String ruta;

    public EntradaFragment() {
        // Required empty public constructor
    }

    public static EntradaFragment newInstance (String tip,String tipVehi, String rut)
    {
        EntradaFragment fragment = new EntradaFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,rut);
        tipo=tip;
        tipoVehiculo=tipVehi;
        ruta=rut;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_entrada, container, false);

        TextView tv_alta = view.findViewById(R.id.tv_entrada);

        Log.d("salida","creado fragment entrada");

        tv_alta.setText("ENTRADA"+tipo+" "+tipoVehiculo+" "+ruta);


        return view;
    }

}
