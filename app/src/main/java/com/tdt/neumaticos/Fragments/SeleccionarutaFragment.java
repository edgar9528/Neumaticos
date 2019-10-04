package com.tdt.neumaticos.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SeleccionarutaFragment extends Fragment implements AsyncResponse {

    private static String tipo;

    private ArrayList<String> rutas;
    private ArrayList<String> rutas_id;
    private boolean seleccionado=false;
    private String ruta;
    private String ruta_id;

    private int peticion=0;

    private String tipoVehiculo;
    private int totalLlantas;

    Button button_terminar,button_cancelar;

    public SeleccionarutaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        tipo= activity.getDataFragmento();

        final View view = inflater.inflate(R.layout.fragment_seleccionaruta, container, false);

        button_cancelar = view.findViewById(R.id.button_cancelar4);
        button_terminar = view.findViewById(R.id.button_terminar4);

        Log.d("salida",tipo);

        peticionSocket();

        button_terminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seleccionado)
                {
                    String command;

                    command = "02|"+ruta_id+"\u001a";

                    ConexionSocket conexionSocket = new ConexionSocket();
                    conexionSocket.command = command;
                    conexionSocket.context = SeleccionarutaFragment.this.getActivity();
                    conexionSocket.delegate = SeleccionarutaFragment.this;
                    conexionSocket.execute();
                }
                else
                {
                    Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
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


    public void peticionSocket()
    {
        String command;

        command = "01"+"\u001a";

        ConexionSocket conexionSocket = new ConexionSocket();
        conexionSocket.command = command;
        conexionSocket.context = SeleccionarutaFragment.this.getActivity();
        conexionSocket.delegate = this;
        conexionSocket.execute();
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

                    rutas = new ArrayList<>();
                    rutas_id = new ArrayList<>();

                    for (int i = 0; i < resultado.length; i = i + 2) {
                        rutas_id.add(resultado[i]);
                        rutas.add(resultado[i + 1]);
                    }
                    llenarSpinners(getView());
                    peticion++;
                }
                else
                {
                    String[] datos = {"Ruta: ","Responsable: ","Marca: ","Año: ",
                                    "No. Serie: ","Placas: ","Tipo vehiculo: ","Total llantas: "};
                    String[] resultado = mensaje.split(",");

                    String mostrar="";

                    for(int i=0; i<datos.length;i++)
                    {
                        mostrar=mostrar+datos[i]+resultado[i]+"\n";
                    }

                    totalLlantas= Integer.parseInt(resultado[7]) ;
                    tipoVehiculo=resultado[8];


                    mensajeConfirmacion(mostrar);
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

    private void mensajeConfirmacion(String mensaje)
    {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(getContext());
        dialogo1.setTitle("Información ruta");
        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                cambiarFragment();
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

            }
        });
        dialogo1.show();
    }

    public void cambiarFragment()
    {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = null;

        if(tipo.equals("Montaje"))
            ft = fm.beginTransaction().replace(R.id.container, MontajeFragment.newInstance(tipo,tipoVehiculo,ruta_id,totalLlantas) );
        else
        if(tipo.equals("Entrada"))
            ft = fm.beginTransaction().replace(R.id.container, EntradaFragment.newInstance(tipo,tipoVehiculo,ruta_id) );
        else
        if(tipo.equals("Salida"))
            ft = fm.beginTransaction().replace(R.id.container, EntradaFragment.newInstance(tipo,tipoVehiculo,ruta_id) );

        ft.addToBackStack(null);
        if (false || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();
    }

    public void llenarSpinners(View view)
    {

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout);
        RadioGroup radioGroup = new RadioGroup(getContext());

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.addView(radioGroup, p);

        for(int i=0; i<rutas.size();i++)
        {
            RadioButton radioButtonView = new RadioButton(getContext());
            radioButtonView.setText( rutas_id.get(i)+" | "+ rutas.get(i));
            radioButtonView.setTextColor( getResources().getColor(R.color.colorPrimaryDark) );
            radioGroup.addView(radioButtonView, p);
            radioButtonView.setOnClickListener(rbListener);
        }

    }

    private View.OnClickListener rbListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            seleccionado=true;

            //OBTENER EL ID SELECCIONADO
            String op=((RadioButton) view).getText().toString();
            String cod="";
            for(int i=0; i<op.length();i++)
            {
                if(op.charAt(i)!=' ')
                    cod=cod+ op.charAt(i);
                else
                    i=op.length();
            }

            ruta_id=cod;

            for(int i=0; i<rutas.size(); i++)
            {
                if(rutas_id.get(i).equals(ruta_id))
                {
                    ruta = rutas.get(i) ;
                    break;
                }
            }
        }
    };

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
