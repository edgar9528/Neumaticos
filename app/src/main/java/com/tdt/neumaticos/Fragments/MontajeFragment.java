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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.R;

import java.util.ArrayList;
import java.util.Arrays;

public class MontajeFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String tipo,tipoVehiculo,ruta;
    private static int totalLlantas;

    private int peticion=0;

    int ejesD,ejesT,llanD,llanT;

    String iv_clave[];
    int iv_ids[], tv_ids[];

    ArrayList<String> llanta_clave,llanta_numero,llanta_codigo;
    TableLayout tableLayout;
    TextView tv_seleccionado;

    View vista;
    LayoutInflater layoutInflater;

    public MontajeFragment() {
        // Required empty public constructor
    }

    public static MontajeFragment newInstance (String tip,String tipVehi, String rut,int totLl)
    {
        MontajeFragment fragment = new MontajeFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,rut);
        tipo=tip;
        tipoVehiculo=tipVehi;
        ruta=rut;
        totalLlantas=totLl;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_montaje, container, false);
        vista = view;
        layoutInflater = inflater;

        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        tv_seleccionado = view.findViewById(R.id.tv_seleccionado);

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
                    if(mensaje.isEmpty())
                    {
                        TableRow tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

                        ((TextView) tr.findViewById(R.id.lTitle)).setText("#"); //Dato de la columna 1
                        ((TextView) tr.findViewById(R.id.lDetail)).setText("TAG"); //Dato de la columna 2
                        tableLayout.addView(tr);

                        for(int i=0; i<totalLlantas;i++)
                        {
                            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

                            ((TextView) tr.findViewById(R.id.lTitle)).setText(llanta_numero.get(i)); //Dato de la columna 1
                            ((TextView) tr.findViewById(R.id.lDetail)).setText("0000000000"); //Dato de la columna 2
                            tableLayout.addView(tr);
                            Log.d("salida","entro aqui");
                        }

                    }
                    else
                    {
                        String[] resultado = mensaje.split(",");
                        Log.d("salida","tiene llantas");


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
        ArrayList<String> clavesOrdenadas;
        ArrayList<String> clavesGeneradas;

        //todos las las claves de las posiciones de llantas (IDS de ImageView)
        iv_clave = new String[] {"ti11","ti12","ti21","ti22","di11","di12","di21","di22",
                                 "td11","td12","td21","td22","dd11","dd12","dd21","dd22"};

        //Todos los id's de los image view
        iv_ids = new int[] {R.id.ti11,R.id.ti12,R.id.ti21,R.id.ti22,R.id.di11,R.id.di12,R.id.di21,R.id.di22,
                R.id.td11,R.id.td12,R.id.td21,R.id.td22,R.id.dd11,R.id.dd12,R.id.dd21,R.id.dd22};

        tv_ids = new int[] {R.id.t_ti11,R.id.t_ti12,R.id.t_ti21,R.id.t_ti22,R.id.t_di11,R.id.t_di12,R.id.t_di21,R.id.t_di22,
                R.id.t_td11,R.id.t_td12,R.id.t_td21,R.id.t_td22,R.id.t_dd11,R.id.t_dd12,R.id.t_dd21,R.id.t_dd22};


        //Para obtener los neumaticos enumerados
        String[] clavOrd= {"di22","di21","dd21","dd22","di12","di11","dd11","dd12",
                           "ti22","ti21","td21","td22","ti12","ti11","td11","td12"};
        clavesGeneradas = new ArrayList<>();
        clavesOrdenadas = new ArrayList<>(Arrays.asList(clavOrd));
        llanta_clave = new ArrayList<>();
        llanta_numero = new ArrayList<>();

        String clave;
        for(int i=1; i<=ejesT;i++)
        {
            for(int j=1;j<=llanT;j++)
            {
                clave = "ti"+String.valueOf(i)+String.valueOf(j);
                clavesGeneradas.add(clave);
            }
            for(int j=1;j<=llanT;j++)
            {
                clave= "td"+String.valueOf(i)+String.valueOf(j);
                clavesGeneradas.add(clave);
            }
        }

        for(int i=1; i<=ejesD;i++)
        {
            for(int j=1;j<=llanD;j++)
            {
                clave= "di"+String.valueOf(i)+String.valueOf(j);
                clavesGeneradas.add(clave);
            }
            for(int j=1;j<=llanD;j++)
            {
                clave="dd"+String.valueOf(i)+String.valueOf(j);
                clavesGeneradas.add(clave);
            }
        }

        //Obtener numeros de neumaticos y dibujarlos
        int con=1;
        for(int i=0; i<clavesOrdenadas.size();i++)
        {
            for(int j=0; j<clavesGeneradas.size();j++)
            {
                if(clavesOrdenadas.get(i).equals(clavesGeneradas.get(j)))
                {
                    llanta_clave.add(clavesGeneradas.get(j));
                    llanta_numero.add( String.valueOf(con));

                    dibujarLlanta(clavesGeneradas.get(j),con);
                    con++;

                    j=clavesGeneradas.size(); //terminar ciclo
                }
            }
        }
    }

    public void dibujarLlanta(String clave,int numero)
    {
        //Busca la clave en la lista de claves, obtiene el indice y dibuja en ese ImageView
        for(int k=0; k<iv_clave.length;k++)
        {
            if (clave.equals(iv_clave[k])) {
                ImageView imageView = vista.findViewById(iv_ids[k]);
                imageView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.llanta));
                imageView.setTag(numero);
                imageView.setOnClickListener(ivListener);

                TextView textView = vista.findViewById(tv_ids[k]);
                textView.setText(String.valueOf(numero));

                k=iv_clave.length;
            }
        }
    }

    //Evento cada que se da click a una llanta
    private View.OnClickListener ivListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("salida","llanta:"+view.getTag());
            tv_seleccionado.setText("Neumático: "+view.getTag()+" seleccionado");
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
