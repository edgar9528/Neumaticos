package com.tdt.neumaticos.Clases;

import android.support.design.widget.TextInputEditText;
import android.widget.EditText;

import java.util.ArrayList;

public class RevisaTextos {

    public RevisaTextos() {
    }

    public boolean llenos(TextInputEditText[] editTexts)
    {
        boolean lleno=true;
        for(int i=0; i<editTexts.length;i++)
        {
            if(editTexts[i].getText().toString().isEmpty())
            {
                lleno=false;
                i=editTexts.length+1;
            }
        }
        return lleno;
    }

    public ArrayList<String> obtenerStrings(TextInputEditText[] editTexts)
    {
        ArrayList<String> arrayList = new ArrayList<>();

        for(int i=0; i<editTexts.length;i++)
        {
            arrayList.add( editTexts[i].getText().toString() );
        }
        return arrayList;
    }

    public boolean esNumero(String cadena)
    {
        boolean resultado;

        try {
            Float.parseFloat(cadena);
            resultado = true;
        } catch
        (Exception e)
        {
            resultado = false;
        }

        return resultado;
    }

}
