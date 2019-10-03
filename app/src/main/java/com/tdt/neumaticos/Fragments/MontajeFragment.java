package com.tdt.neumaticos.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.R;

import java.util.ArrayList;

public class MontajeFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String tipo;
    private static String tipoVehiculo;
    private static String ruta;

    private int peticion=0;

    int ejesD,ejesT,llanD,llanT;

    public MontajeFragment() {
        // Required empty public constructor
    }

    public static MontajeFragment newInstance (String tip,String tipVehi, String rut)
    {
        MontajeFragment fragment = new MontajeFragment();
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
        final View view = inflater.inflate(R.layout.fragment_montaje, container, false);

        TextView tv_alta = view.findViewById(R.id.tv_montaje);

        tv_alta.setText("MONTAJE "+tipo+"|"+tipoVehiculo+"|"+ruta);

        String command = "06|"+tipoVehiculo+"\u001a";
        peticionSocket(command);


        return view;
    }

    private void peticionSocket(String command)
    {
        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = MontajeFragment.this.getActivity();
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
                if(peticion==0)
                {
                    String[] resultado = mensaje.split(",");

                    ejesD = Integer.parseInt(resultado[0]);
                    ejesT = Integer.parseInt(resultado[1]);
                    llanD = Integer.parseInt(resultado[2]);
                    llanT = Integer.parseInt(resultado[3]);

                    peticion++;

                    //Ejecuta la siguiente peticion
                    String command = "12|"+ruta+"\u001a";
                    peticionSocket(command);

                }
                else
                if(peticion==1)
                {
                    String[] resultado = mensaje.split(",");
                    for (int i=0; i<resultado.length;i++)
                    {
                        Log.d("salida","p2"+resultado[i]+"|");
                    }


                }
            }
            else
            {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
                goFragmentAnterior();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Error: "+e.toString(), Toast.LENGTH_LONG).show();
            goFragmentAnterior();
        }

    }


    public void goFragmentAnterior()
    {
        try
        {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = null;

            Fragment fragment;
            fragment = new SeleccionarutaFragment();

            ft = fm.beginTransaction().replace(R.id.container, fragment);

            ft.addToBackStack(null);
            if (false || !BuildConfig.DEBUG)
                ft.commitAllowingStateLoss();
            else
                ft.commit();
            fm.executePendingTransactions();
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Error: "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
