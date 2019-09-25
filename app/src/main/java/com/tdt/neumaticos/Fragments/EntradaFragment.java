package com.tdt.neumaticos.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tdt.neumaticos.R;

public class EntradaFragment extends Fragment {

    public EntradaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_entrada, container, false);

        TextView tv_alta = view.findViewById(R.id.tv_entrada);

        tv_alta.setText("Hola, soy el fragment ENTRADA");


        return view;
    }

}
