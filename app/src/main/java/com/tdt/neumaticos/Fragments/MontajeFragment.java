package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    String iv_clave[];
    int iv_ids[];

    View vista;

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

        vista = view;




        //pide la información del vehiculo, despues la información de las llantas de la ruta
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

                    dibujarCamion();

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

    public void dibujarCamion()
    {

        //todos las las claves de las posiciones de llantas (IDS de ImageView)
        iv_clave = new String[] {"ti11","ti12","ti21","ti22","di11","di12","di21","di22",
                "td11","td12","td21","td22","dd11","dd12","dd21","dd22"};

        //Todos los id's de los image view
        iv_ids = new int[] {R.id.ti11,R.id.ti12,R.id.ti21,R.id.ti22,R.id.di11,R.id.di12,R.id.di21,R.id.di22,
                R.id.td11,R.id.td12,R.id.td21,R.id.td22,R.id.dd11,R.id.dd12,R.id.dd21,R.id.dd22};


        for(int i=1; i<=ejesT;i++)
        {
            for(int j=1;j<=llanT;j++)
            {
                dibujarLlanta("ti"+String.valueOf(i)+String.valueOf(j));
            }
            for(int j=1;j<=llanT;j++)
            {
                dibujarLlanta("td"+String.valueOf(i)+String.valueOf(j));
            }
        }

        for(int i=1; i<=ejesD;i++)
        {
            for(int j=1;j<=llanD;j++)
            {
                dibujarLlanta("di"+String.valueOf(i)+String.valueOf(j));
            }
            for(int j=1;j<=llanD;j++)
            {
                dibujarLlanta("dd"+String.valueOf(i)+String.valueOf(j));
            }
        }

    }

    public void dibujarLlanta(String clave)
    {
        //Busca la clave en la lista de claves, obtiene el indice y dibuja en ese ImageView
        for(int k=0; k<iv_clave.length;k++)
        {
            if (clave.equals(iv_clave[k])) {
                ImageView imageView = vista.findViewById(iv_ids[k]);
                imageView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.llanta));
                k=iv_clave.length;
            }
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
