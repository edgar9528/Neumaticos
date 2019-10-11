package com.tdt.neumaticos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tdt.neumaticos.Clases.CifrarDescifrar;
import com.tdt.neumaticos.Clases.RegistroTerminal;
import com.tdt.neumaticos.Clases.ValorRetorno;
import com.tdt.neumaticos.Clases.WSValorRetorno;
import com.tdt.neumaticos.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class LicenciaActivity extends AppCompatActivity {

    TextView tv_noserie,tv_modelo,tv_marca,tv_dispositivo,tv_estado;
    EditText et_licencia;
    Button button_act,button_reg;
    String licencia;

    String archivoRegistro,archivoLicencia;
    private WSValorRetorno valorRetorno;
    RegistroTerminal registroTerminal;
    private Gson gson = new Gson();
    private String respJSONConsultaEmpresa, respJSONActivaLicencia;

    private String NAMESPACE = "http://tempuri.org/";
    private String URL = "http://192.168.0.1/WebTDTLicencias/WSComunicacion.asmx";
    private String SOAP_ACTION = "http://tempuri.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licencia);

        this.setTitle("Configuración");


        try {
            archivoRegistro = "Registro.properties";
            archivoLicencia = "Licencia.properties";

            registroTerminal = new RegistroTerminal();
            tv_noserie = findViewById(R.id.tv_noSerie);
            tv_modelo = findViewById(R.id.tv_modelo);
            tv_marca = findViewById(R.id.tv_marca);
            tv_dispositivo = findViewById(R.id.tv_dispositivo);
            et_licencia = findViewById(R.id.et_licencia);
            button_act = findViewById(R.id.button_activar);
            tv_estado = findViewById(R.id.tv_estado);
            button_reg = findViewById(R.id.button_regresar);

            SharedPreferences sharedPref = getSharedPreferences("Licencia", Context.MODE_PRIVATE);
            String lic = sharedPref.getString("licencia", "");

            et_licencia.setText(lic);

            button_act.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!et_licencia.getText().toString().isEmpty())
                    {
                        licencia = et_licencia.getText().toString();

                        if(licencia.length()==29)
                        {
                            if(licencia.contains("01002620001"))
                            {
                                ActivaLicencia activaLicencia = new ActivaLicencia();
                                activaLicencia.execute();
                            }
                            else
                                Toast.makeText(getApplication(), "Formato incorrecto", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getApplication(), "Numero de carácteres incorrecto", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplication(), "Ingresa una licencia", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            button_reg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            actualizaDatosTerminal();
            validaLicencia();

        }catch (Exception e)
        {
            Toast.makeText(getApplication(), "Error: "+e.toString(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LicenciaActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        }





    }


    private void validaLicencia() {
        Properties propiedades = new Properties();
        FileInputStream fis = null;
        String strSerieL, strModeloL, strMarcaL;
        String strSerieR, strModeloR, strMarcaR;

        try {
            fis = openFileInput(archivoLicencia);
            propiedades.load(fis);

            strSerieL= CifrarDescifrar.descifrar(propiedades.getProperty("SERIE"), "TdTm901016");
            strModeloL= CifrarDescifrar.descifrar(propiedades.getProperty("MODELO"), "TdTm901016");
            strMarcaL= CifrarDescifrar.descifrar(propiedades.getProperty("MARCA"), "TdTm901016");

            fis = openFileInput(archivoRegistro);
            propiedades.load(fis);
            strSerieR = propiedades.getProperty("SERIE");
            strModeloR = propiedades.getProperty("MODELO");
            strMarcaR = propiedades.getProperty("MARCA");

            if( strSerieL.equals(strSerieR) && strMarcaL.equals(strMarcaR) && strModeloL.equals(strModeloR) )
            {
                tv_estado.setText("Licencia activada");
            }
            else
            {
                tv_estado.setText("Licencia erronea");
            }

        } catch (Exception ex) {
            //Toast.makeText(this, "[EX LA] " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            tv_estado.setText("Producto sin licencia");

        } finally {
            try {
                fis.close();
            } catch (Exception ex) {
            }
        }
    }

    private class ActivaLicencia extends AsyncTask<String,Integer,Boolean> {

        private ProgressDialog progreso;

        @Override protected void onPreExecute() {
            progreso = new ProgressDialog(LicenciaActivity.this);
            progreso.setMessage("Verificando...");
            progreso.setCancelable(false);
            progreso.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = true;

            try {

                SoapObject request = new SoapObject(NAMESPACE, "HabilitaLicencia");
                // Property which holds input parameters
                PropertyInfo paramPI = new PropertyInfo();

                paramPI = new PropertyInfo();
                paramPI.setName("codigoLicencia"); // Set Name
                paramPI.setValue(licencia); // Set Value
                paramPI.setType(String.class); // Set dataType
                request.addProperty(paramPI);// Add the property to request object

                paramPI = new PropertyInfo();
                paramPI.setName("numeroSerie"); // Set Name
                paramPI.setValue(registroTerminal.getSerie()); // Set Value
                paramPI.setType(String.class); // Set dataType
                request.addProperty(paramPI);// Add the property to request object

                paramPI = new PropertyInfo();
                paramPI.setName("modelo"); // Set Name
                paramPI.setValue(registroTerminal.getModelo()); // Set Value
                paramPI.setType(String.class); // Set dataType
                request.addProperty(paramPI);// Add the property to request object

                paramPI = new PropertyInfo();
                paramPI.setName("marca"); // Set Name
                paramPI.setValue(registroTerminal.getMarca()); // Set Value
                paramPI.setType(String.class); // Set dataType
                request.addProperty(paramPI);// Add the property to request object

                paramPI = new PropertyInfo();
                paramPI.setName("dispositivo"); // Set Name
                paramPI.setValue(registroTerminal.getDispositivo()); // Set Value
                paramPI.setType(String.class); // Set dataType
                request.addProperty(paramPI);// Add the property to request object

                // Create envelope
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                        SoapEnvelope.VER11);
                envelope.dotNet = true;
                // Set output SOAP object
                envelope.setOutputSoapObject(request);
                // Create HTTP call object
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    // Invole web service
                    androidHttpTransport.call("http://tempuri.org/HabilitaLicencia", envelope);
                    // Get the response
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    // Assign it to static variable
                    respJSONActivaLicencia = response.toString();
                    valorRetorno = new WSValorRetorno(true, "");
                    Log.i("invokeActivaLicenciaWS", "[respJSONActivaLicencia] " + respJSONActivaLicencia);

                } catch (Exception ex) {
                    Log.e("invokeActivaLicenciaWS", ex.getMessage());
                    valorRetorno = new WSValorRetorno(false, ex.getMessage());

                    result = false;
                }

            }catch (Exception e)
            {
                Toast.makeText(getApplication(), "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progreso.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            progreso.dismiss();

            if(aBoolean)
            {
                try {
                    ValorRetorno valRetorno;

                    if (valorRetorno.isValor() ) {
                        // Licencia Activada
                        valRetorno = gson.fromJson(respJSONActivaLicencia,ValorRetorno.class);
                        if(valRetorno.retorno == 1){
                            generaArchivoLicencia();
                            guardarLicencia();
                            Toast.makeText(getApplicationContext(), "Producto activado", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(LicenciaActivity.this, InicioActivity.class);
                            startActivity(intent);
                            finish();

                        }else{
                            Toast.makeText(getApplicationContext(), valRetorno.mensaje, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Licencia NO Activada
                        Toast.makeText(getApplicationContext(), valorRetorno.getMensaje(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "[EX] " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                //et_comunicacion.setText("Error en la comunicación");
                Toast.makeText(getApplication(), "Error en la comunicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generaArchivoLicencia() {
        Properties propiedades;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        propiedades = new Properties();
        try {
            propiedades.setProperty("SERIE", CifrarDescifrar.cifrar(registroTerminal.getSerie(), "TdTm901016"));
            propiedades.setProperty("MODELO", CifrarDescifrar.cifrar(registroTerminal.getModelo(), "TdTm901016"));
            propiedades.setProperty("MARCA", CifrarDescifrar.cifrar(registroTerminal.getMarca(), "TdTm901016"));
            propiedades.setProperty("FABRICANTE", CifrarDescifrar.cifrar(registroTerminal.getFabricante(), "TdTm901016"));
            propiedades.setProperty("DISPOSITIVO", CifrarDescifrar.cifrar(registroTerminal.getDispositivo(), "TdTm901016"));
            propiedades.setProperty("PRODUCTO", CifrarDescifrar.cifrar(registroTerminal.getProducto(), "TdTm901016"));

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "[EX - generaArchivoLicencia]" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Borra El archivo de Propiedades Actual
        try {
            this.deleteFile(archivoLicencia);
        } catch (Exception ex) {
        }

        try {
            fos = this.openFileOutput(archivoLicencia, Context.MODE_APPEND);
            propiedades.store(fos, "");

            //Toast.makeText(this, "Datos de Licencia Generados con Exito", Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            Toast.makeText(this, "[EX GA] " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                fis.close();
            } catch (Exception ex) {
            }
        }
    }

    public void guardarLicencia()
    {
        SharedPreferences sharedPref = getSharedPreferences("Licencia", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("licencia",et_licencia.getText().toString());
        editor.apply();
    }

    private void actualizaDatosTerminal() {

        String strNumeroSerie="",strModelo="",strMarca="",strDispositivo="";

        try
        {
            strNumeroSerie = Build.SERIAL;
            strModelo = Build.MODEL;
            strMarca = Build.BRAND;
            strDispositivo = Build.DEVICE;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "[EX] " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        tv_noserie.setText(strNumeroSerie);
        tv_modelo.setText(strModelo);
        tv_marca.setText(strMarca);
        tv_dispositivo.setText(strDispositivo);

        registroTerminal.setSerie(Build.SERIAL);
        registroTerminal.setModelo(Build.MODEL);
        registroTerminal.setMarca(Build.BRAND);
        registroTerminal.setFabricante(Build.MANUFACTURER);
        registroTerminal.setDispositivo(Build.DEVICE);
        registroTerminal.setProducto(Build.PRODUCT);

    }





}
