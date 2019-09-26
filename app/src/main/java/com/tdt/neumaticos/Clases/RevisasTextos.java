package com.tdt.neumaticos.Clases;

import android.support.design.widget.TextInputEditText;
import android.widget.EditText;

public class RevisasTextos {

    public RevisasTextos() {
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

}
