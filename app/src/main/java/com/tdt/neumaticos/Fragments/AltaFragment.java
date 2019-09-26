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
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

import java.util.Calendar;

public class AltaFragment extends Fragment {

    private static final String PARAMETRO="codigo";
    private static String codigo;
    private static final String CERO = "0";
    private static final String BARRA = "/";

    public final Calendar c = Calendar.getInstance();
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    Activity activity;

    String str_marcas,str_almacenes;

    TextInputEditText[] textInputs;
    Spinner spinner_marca,spinner_ubicacion;

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
        activity = (MainActivity) getActivity();
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

        obtenerInfoSpinners();

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


        tv_alta.setText("Código: "+codigo);

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

    public void obtenerInfoSpinners()
    {


    }

}
