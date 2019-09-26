package com.tdt.neumaticos.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

public class LeercodigoFragment extends Fragment {

    String tipo,codigo;

    TextView tv_mensaje,tv_codigo;
    Button button_codigo;

    public LeercodigoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        tipo= activity.getDataFragmento();


        final View view = inflater.inflate(R.layout.fragment_leercodigo, container, false);

        tv_mensaje = view.findViewById(R.id.tv_mensaje);
        tv_codigo = view.findViewById(R.id.tv_codigo);
        button_codigo = view.findViewById(R.id.button_codigo);

        if(tipo.equals("Alta"))
            tv_mensaje.setText("dar de alta");
        else
            tv_mensaje.setText("cambiar ubicación");

        button_codigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                codigo="COD123";
                cambiarFragment();
            }
        });

        return view;
    }

    public void cambiarFragment()
    {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = null;

        if(tipo.equals("Alta"))
            ft = fm.beginTransaction().replace(R.id.container, AltaFragment.newInstance(codigo) );
        else
            ft = fm.beginTransaction().replace(R.id.container, CambiaubiFragment.newInstance(codigo) );

        ft.addToBackStack(null);
        if (false || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();
    }

}
