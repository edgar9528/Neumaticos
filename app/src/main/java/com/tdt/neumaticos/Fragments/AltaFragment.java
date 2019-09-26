package com.tdt.neumaticos.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.Clases.RevisasTextos;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class AltaFragment extends Fragment implements AsyncResponse{

    private static final String PARAMETRO="codigo";
    private static String codigo;
    private static final String CERO = "0";
    private static final String BARRA = "/";

    public final Calendar c = Calendar.getInstance();
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    private int peticion=0;

    ArrayList<String> marcas,marcas_id,almacenes,almacenes_id;

    TextInputEditText[] textInputs;
    Spinner spinner_marca,spinner_ubicacion;
    Button button_terminar;



    public AltaFragment() {
        // Required empty public constructor
    }

    public static AltaFragment newInstance (String cod)
    {
        AltaFragment fragment = new AltaFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,cod);
        codigo=cod;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_alta, container, false);


        TextView tv_alta = view.findViewById(R.id.tv_alta);
        textInputs = new TextInputEditText[8];
        textInputs[0] = view.findViewById(R.id.ti_descripcion);
        textInputs[1] = view.findViewById(R.id.ti_fechaFab);
        textInputs[2] = view.findViewById(R.id.ti_fechaCom);
        textInputs[3] = view.findViewById(R.id.ti_kilometraje);
        textInputs[4] = view.findViewById(R.id.ti_presion);
        textInputs[5] = view.findViewById(R.id.ti_medida);
        textInputs[6] = view.findViewById(R.id.ti_nserie);
        textInputs[7] = view.findViewById(R.id.ti_dibujo);
        spinner_marca = view.findViewById(R.id.spinner_marca);
        spinner_ubicacion = view.findViewById(R.id.spinner_ubicacion);
        button_terminar = view.findViewById(R.id.button_terminar1);

        tv_alta.setText("Código: "+codigo);
        obtenerInfoSpinnerMarcas();

        textInputs[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerFecha(1);
            }
        });

        textInputs[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerFecha(2);
            }
        });

        button_terminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RevisasTextos revisasTextos = new RevisasTextos();

                if(revisasTextos.llenos(textInputs))
                {
                    Toast.makeText(getContext(), "Campos llenos", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_LONG).show();
                }

            }
        });


        return view;
    }


    public void obtenerFecha(final int textInput)
    {
        DatePickerDialog recogerFecha = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //Esta variable lo que realiza es aumentar en uno el mes ya que comienza desde 0 = enero
                final int mesActual = month + 1;
                //Formateo el día obtenido: antepone el 0 si son menores de 10
                String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                //Formateo el mes obtenido: antepone el 0 si son menores de 10
                String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
                //Muestro la fecha con el formato deseado
                //etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);

                String fecha = diaFormateado + BARRA + mesFormateado + BARRA + year;

                textInputs[textInput].setText(fecha);
                //Toast.makeText(getContext(), fecha, Toast.LENGTH_SHORT).show();

            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
            /**
             *También puede cargar los valores que usted desee
             */
        },anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();

    }

    public void obtenerInfoSpinnerMarcas()
    {
        String command = "03"+"\u001a";

        ConexionSocket conexionSocket = new ConexionSocket();
        conexionSocket.command = command;
        conexionSocket.context = AltaFragment.this.getActivity();
        conexionSocket.delegate = this;
        conexionSocket.execute();

    }

    public void obtenerInfoSpinnerAlmacen()
    {
        String command = "04"+"\u001a";

        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = AltaFragment.this.getActivity();
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
                String[] resultado = mensaje.split(",");

                if(peticion==0)
                {
                    marcas = new ArrayList<>();
                    marcas_id = new ArrayList<>();

                    for (int i = 0; i < resultado.length; i = i + 2) {
                        marcas_id.add(resultado[i]);
                        marcas.add(resultado[i + 1]);
                    }

                    spinner_marca.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_item, marcas));
                    peticion++;

                    obtenerInfoSpinnerAlmacen();
                }
                else
                {
                    almacenes = new ArrayList<>();
                    almacenes_id = new ArrayList<>();

                    for (int i = 0; i < resultado.length; i = i + 2) {
                        almacenes_id.add(resultado[i]);
                        almacenes.add(resultado[i + 1]);
                    }

                    spinner_ubicacion.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_item, almacenes));
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


}
