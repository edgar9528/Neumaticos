package com.tdt.neumaticos.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.R;

import java.util.ArrayList;

public class CambiaubiFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String codigo;
    private static String ubicacion;
    private static String ubicacion_id;
    private boolean esAlmacen=false;
    private String usuario;


    public CambiaubiFragment() {
        // Required empty public constructor
    }

    TextView tv_tag,tv_ubicacion;
    Button button_aceptar,button_cancelar;
    Spinner spinner_almacenes;


    int peticion=0;

    ArrayList<String> almacenes,almacenes_id;

    public static CambiaubiFragment newInstance (String cod,String ubi,String ubi_id)
    {
        CambiaubiFragment fragment = new CambiaubiFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,cod);
        codigo=cod;
        ubicacion=ubi;
        ubicacion_id=ubi_id;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cambiaubi, container, false);

        tv_tag = view.findViewById(R.id.tv_tag1);
        tv_ubicacion = view.findViewById(R.id.tv_ubiActual);
        button_aceptar = view.findViewById(R.id.button_terminar2);
        button_cancelar = view.findViewById(R.id.button_cancelar2);
        spinner_almacenes = view.findViewById(R.id.spinner_ubicacion1);

        tv_tag.setText(codigo);

        obtenerUsuario();
        obtenerInfoSpinnerAlmacen();

        button_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ubicacion_nueva_id = almacenes_id.get((int)spinner_almacenes.getSelectedItemId() );

                if(!ubicacion_nueva_id.equals(ubicacion_id))
                {
                    String command;

                    if(esAlmacen)
                        command= "13|"+codigo+"|"+ubicacion_nueva_id+"|"+ "A"+"|" + ubicacion_id +"|"+usuario+"\u001a";
                    else
                        command= "13|"+codigo+"|"+ubicacion_nueva_id+"|"+ "R"+"|" + ubicacion_id +"|"+usuario+"\u001a";

                    ConexionSocket conexionSocket2 = new ConexionSocket();
                    conexionSocket2.command = command;
                    conexionSocket2.context = CambiaubiFragment.this.getActivity();
                    conexionSocket2.delegate = CambiaubiFragment.this;
                    conexionSocket2.execute();

                }
                else
                {
                    Toast.makeText(getContext(), "La ubicación es igual a la anterior", Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goFragmentAnterior();
            }
        });

        return view;
    }

    public void obtenerInfoSpinnerAlmacen()
    {
        String command = "04|"+"\u001a";

        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = CambiaubiFragment.this.getActivity();
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

                    String[] resultado = mensaje.split("\u0009");

                    almacenes = new ArrayList<>();
                    almacenes_id = new ArrayList<>();

                    esAlmacen=false;

                    for (int i = 0; i < resultado.length; i = i + 2) {
                        almacenes_id.add(resultado[i]);
                        almacenes.add(resultado[i + 1]);
                        if(resultado[i+1].equals(ubicacion))
                            esAlmacen=true;
                    }

                    if(esAlmacen)
                        tv_ubicacion.setText("Llanta en almacen: "+ubicacion);
                    else
                        tv_ubicacion.setText("Llanta en ruta: "+ubicacion);

                    spinner_almacenes.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_item, almacenes));

                    peticion++;
                }
                else
                {
                    Toast.makeText(getContext(), "Ubicación actualizada", Toast.LENGTH_LONG).show();
                    goFragmentAnterior();
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
            fragment = new LeercodigoFragment();

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

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        usuario = sharedPref.getString("usuario","null");
    }

}
