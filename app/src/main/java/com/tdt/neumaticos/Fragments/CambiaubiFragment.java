package com.tdt.neumaticos.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tdt.neumaticos.R;

public class CambiaubiFragment extends Fragment {

    private static final String PARAMETRO="codigo";
    private static String codigo;

    public CambiaubiFragment() {
        // Required empty public constructor
    }

    public static CambiaubiFragment newInstance (String cod)
    {
        CambiaubiFragment fragment = new CambiaubiFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,cod);
        codigo=cod;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cambiaubi, container, false);

        TextView tv_alta = view.findViewById(R.id.tv_cambiaubi);

        tv_alta.setText("Hola, soy el fragment CAMBIA UBICACION"+codigo);


        return view;
    }

}
