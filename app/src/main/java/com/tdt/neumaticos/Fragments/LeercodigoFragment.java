package com.tdt.neumaticos.Fragments;

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
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

public class LeercodigoFragment extends Fragment implements AsyncResponse {

    String tipo,codigo;
    String ubicacion,ubicacion_id;

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
        if(tipo.equals("Cambia"))
            tv_mensaje.setText("cambiar ubicación");
        else
        if(tipo.equals("Baja"))
            tv_mensaje.setText("dar de baja");
        else
        if(tipo.equals("Mantenimiento"))
            tv_mensaje.setText("dar mantenimiento");

        button_codigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                codigo="CODPRUEBA2";

                verificaCodigo();
            }
        });

        return view;
    }

    public void verificaCodigo()
    {
        String command="";

        if(tipo.equals("Alta"))
            command = "08|"+codigo+"\u001a";
        else
        if(tipo.equals("Cambia")|| tipo.equals("Baja")||tipo.equals("Mantenimiento"))
            command = "11|"+codigo+"\u001a";


        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = LeercodigoFragment.this.getActivity();
        conexionSocket2.delegate = this;
        conexionSocket2.execute();

    }


    @Override
    public void processFinish(String output){
        try
        {
            String clave = output.substring(0,2);
            String mensaje = output.substring(2,output.length());
            mensaje=mensaje.trim(); // elimina espacios en blanco al principio y final

            if(clave.equals("BC"))
            {
                if(tipo.equals("Alta"))
                {
                    cambiarFragment();
                }
                else
                if(tipo.equals("Cambia"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
                }
                else
                if(tipo.equals("Baja"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
                }
                else
                if(tipo.equals("Mantenimiento"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
                }
            }
            else
            {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Error: "+e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void cambiarFragment()
    {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = null;

        if(tipo.equals("Alta"))
            ft = fm.beginTransaction().replace(R.id.container, AltaFragment.newInstance(codigo) );
        else
        if(tipo.equals("Cambia"))
            ft = fm.beginTransaction().replace(R.id.container, CambiaubiFragment.newInstance(codigo,ubicacion,ubicacion_id) );
        else
        if(tipo.equals("Baja"))
            ft = fm.beginTransaction().replace(R.id.container, BajamatenimientoFragment.newInstance(codigo,ubicacion,ubicacion_id) );
        else
        if(tipo.equals("Mantenimiento"))
            ft = fm.beginTransaction().replace(R.id.container, BajamatenimientoFragment.newInstance(codigo,ubicacion,ubicacion_id) );

        ft.addToBackStack(null);
        if (false || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();
    }

}
