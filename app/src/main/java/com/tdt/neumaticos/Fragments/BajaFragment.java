package com.tdt.neumaticos.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
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
import com.tdt.neumaticos.LoginActivity;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BajaFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String codigo;
    private static String ubicacion;
    private static String ubicacion_id;

    private ArrayList<String> bajaMantenimiento;
    private ArrayList<String> bajaMantenimiento_id;
    private boolean seleccionado=false;
    private String bajaMante;
    private String bajaMante_id;
    private String tipo;
    private int peticion=0;

    TextView tv_titulo;
    Button button_terminar,button_cancelar;

    public BajaFragment() {
        // Required empty public constructor
    }

    public static BajaFragment newInstance (String cod,String ubi,String ubi_id)
    {
        BajaFragment fragment = new BajaFragment();
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

        MainActivity activity = (MainActivity) getActivity();
        tipo= activity.getDataFragmento();
        final View view = inflater.inflate(R.layout.fragment_baja, container, false);

        tv_titulo = view.findViewById(R.id.tv_titulo);
        button_cancelar = view.findViewById(R.id.button_cancelar3);
        button_terminar = view.findViewById(R.id.button_terminar3);


        if(tipo.equals("Baja"))
        {
            tv_titulo.setText("Motivo para dar de baja");
            peticionSocket();
        }
        else
        {
            tv_titulo.setText("Motivo para dar mantenimiento");
            peticionSocket();
        }

        button_terminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seleccionado)
                {
                    String command;
                    String mensaje="";
                    String titulo="";


                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = new Date();
                    String fecha = dateFormat.format(date);

                    SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm:ss");
                    String hora = horaFormat.format(new Date());

                    if(tipo.equals("Baja"))
                    {
                        command = "18" + "\u001a";

                        titulo="Se dará de baja";

                        mensaje="Neumátio: "+codigo+"\n"
                                +"Ubicación: "+ubicacion_id+"-"+ubicacion+"\n"
                                +"Motivo: \n"+bajaMante+"\n"
                                +"El día: "+fecha+"\n"
                                +"A las: "+hora+"\n\n"
                                +"¿Desea continuar?";
                    }
                    else
                    {
                        command = "18" + "\u001a";
                    }


                    if(confirmacion(mensaje,titulo))
                    {
                        ConexionSocket conexionSocket = new ConexionSocket();
                        conexionSocket.command = command;
                        conexionSocket.context = BajaFragment.this.getActivity();
                        conexionSocket.delegate = BajaFragment.this;
                        conexionSocket.execute();
                    }

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

    public boolean confirmacion(String mensaje,String titulo)
    {
        final boolean[] aceptar = {false};
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(getContext());
        dialogo1.setTitle(titulo);
        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptar[0] =true;
            }
        });
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptar[0] =false;
            }
        });
        dialogo1.show();

        return aceptar[0];
    }

    public void peticionSocket()
    {
        String command;

        if(tipo.equals("Baja"))
        {
            command = "18"+"\u001a";
        }
        else
        {
            command = "03"+"\u001a";
        }

        ConexionSocket conexionSocket = new ConexionSocket();
        conexionSocket.command = command;
        conexionSocket.context = BajaFragment.this.getActivity();
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

                    bajaMantenimiento = new ArrayList<>();
                    bajaMantenimiento_id = new ArrayList<>();

                    for (int i = 0; i < resultado.length; i = i + 2) {
                        bajaMantenimiento_id.add(resultado[i]);
                        bajaMantenimiento.add(resultado[i + 1]);
                    }
                    llenarSpinners(getView());
                    peticion++;
                }
                else
                {
                    if(tipo.equals("Baja"))
                    {

                    }
                    else
                    {

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


    public void llenarSpinners(View view)
    {

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout);
        RadioGroup radioGroup = new RadioGroup(getContext());

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.addView(radioGroup, p);

        for(int i=0; i<bajaMantenimiento.size();i++)
        {
            RadioButton radioButtonView = new RadioButton(getContext());
            radioButtonView.setText(bajaMantenimiento.get(i));
            radioButtonView.setTextColor( getResources().getColor(R.color.colorPrimaryDark) );
            radioGroup.addView(radioButtonView, p);
            radioButtonView.setOnClickListener(rbListener);
        }

    }

    private View.OnClickListener rbListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            seleccionado=true;
            bajaMante = ((RadioButton) view).getText().toString();
            for(int i=0; i<bajaMantenimiento.size(); i++)
            {
                if(bajaMantenimiento.get(i).equals(bajaMante))
                {
                    bajaMante_id = bajaMantenimiento_id.get(i) ;
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


}
