package com.tdt.neumaticos.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

public class LeercodigoFragment extends Fragment implements AsyncResponse {

    String tipo,codigo;
    String ubicacion,ubicacion_id;

    TextView tv_mensaje,tv_codigo,tv_infoLector;
    Button button_codigo;

    //Variables para leer codigo
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    private EventHandler eventHandler;
    ToneGenerator toneGenerator;
    ArrayList<String> totalTags,tagsLeidos;
    boolean menuSeleccion=false;

    public LeercodigoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        tipo= activity.getDataFragmento();


        final View view = inflater.inflate(R.layout.fragment_leercodigo, container, false);

        tv_mensaje = view.findViewById(R.id.tv_mensaje);
        tv_codigo = view.findViewById(R.id.tv_codigo);
        button_codigo = view.findViewById(R.id.button_codigo);
        tv_infoLector = view.findViewById(R.id.tv_infoLector);
        totalTags = new ArrayList<>();

        if(tipo.equals("Alta"))
            tv_mensaje.setText("dar de alta");
        else
        if(tipo.equals("Cambia"))
            tv_mensaje.setText("cambiar ubicación");
        else
        if(tipo.equals("Baja"))
            tv_mensaje.setText("dar de baja");
        else
        if(tipo.equals("Mantenimiento"))
            tv_mensaje.setText("dar mantenimiento");


        button_codigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desconectarLector();
                conectarLector();
            }
        });


        conectarLector();


        return view;
    }

    public void verificaCodigo()
    {
        String command="";

        if(tipo.equals("Alta"))
            command = "08|"+codigo+"|\u001a";
        else
        if(tipo.equals("Cambia")|| tipo.equals("Baja")||tipo.equals("Mantenimiento"))
            command = "11|"+codigo+"|\u001a";


        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = LeercodigoFragment.this.getActivity();
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
                if(tipo.equals("Alta"))
                {
                    cambiarFragment();
                }
                else
                if(tipo.equals("Cambia"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
                }
                else
                if(tipo.equals("Baja"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
                }
                else
                if(tipo.equals("Mantenimiento"))
                {
                    String[] resultado = mensaje.split(",");
                    ubicacion_id= resultado[0];
                    ubicacion=resultado[1];
                    cambiarFragment();
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

    public void cambiarFragment()
    {
        desconectarLector();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = null;

        if(tipo.equals("Alta"))
            ft = fm.beginTransaction().replace(R.id.container, AltaFragment.newInstance(codigo) );
        else
        if(tipo.equals("Cambia"))
            ft = fm.beginTransaction().replace(R.id.container, CambiaubiFragment.newInstance(codigo,ubicacion,ubicacion_id) );
        else
        if(tipo.equals("Baja"))
            ft = fm.beginTransaction().replace(R.id.container, BajamatenimientoFragment.newInstance(codigo,ubicacion,ubicacion_id) );
        else
        if(tipo.equals("Mantenimiento"))
            ft = fm.beginTransaction().replace(R.id.container, BajamatenimientoFragment.newInstance(codigo,ubicacion,ubicacion_id) );

        ft.addToBackStack(null);
        if (false || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();
    }

    public void desconectarLector()
    {
        //si hay una coneccion, se cierra
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void conectarLector()
    {

        desconectarLector();

        if (readers == null) {
            readers = new Readers(getContext(), ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        new AsyncTask<Void, Integer, Boolean>() {

            private ProgressDialog progreso;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progreso = new ProgressDialog(getContext());
                progreso.setMessage("Conectando lector...");
                progreso.setCancelable(false);
                progreso.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    if (readers != null) {
                        if (readers.GetAvailableRFIDReaderList() != null) {
                            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                            if (availableRFIDReaderList.size() != 0) {
                                // get first reader from list
                                readerDevice = availableRFIDReaderList.get(0);
                                reader = readerDevice.getRFIDReader();
                                if (!reader.isConnected()) {
                                    // Establish connection to the RFID Reader
                                    reader.connect();
                                    ConfigureReader();
                                    return true;
                                }
                            }
                        }
                    }
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                }
                return false;
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

                if (aBoolean) {
                    //Toast.makeText(getContext(), "Lector conectado", Toast.LENGTH_LONG).show();
                    tv_infoLector.setText("Lector conectado");
                }
                else
                {
                    Toast.makeText(getContext(), "Lector no conectado", Toast.LENGTH_LONG).show();
                    tv_infoLector.setText("Lector no conectado");
                }
            }
        }.execute();
    }

    private void ConfigureReader() {
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data{{
                reader.Events.setTagReadEvent(true);
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public void reproducirSonido()
    {
        Log.d(TAG, "playTone");
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        Log.d(TAG, "ToneGenerator released");
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }
            }, 200);
        } catch (Exception e) {
            Log.d(TAG, "Exception while playing sound:" + e);
        }
    }

    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(1);
            if (myTags != null)
            {
                reproducirSonido();
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    totalTags.add(myTags[index].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                }
            }
        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    if(menuSeleccion==false)
                    {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    reader.Actions.Inventory.perform();
                                } catch (InvalidUsageException e) {
                                    e.printStackTrace();
                                } catch (OperationFailureException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();
                    }
                    else
                    {
                        notificacion("Selecciona un tag");
                    }

                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    if(menuSeleccion==false)
                    {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    reader.Actions.Inventory.stop();
                                } catch (InvalidUsageException e) {
                                    e.printStackTrace();
                                } catch (OperationFailureException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                verTagsLeidos();
                            }
                        }.execute();
                    }

                }
            }
        }
    }

    public void notificacion(final String men)
    {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), men, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void verTagsLeidos()
    {
        tagsLeidos = new ArrayList<>();

        //TagsLeidos contendra unicamente los tags que no están repetidos
        for(int i=0; i<totalTags.size();i++)
        {
            if(!tagsLeidos.contains(totalTags.get(i)))
            {
                tagsLeidos.add(totalTags.get(i));
            }
        }

        totalTags.clear();

        if(tagsLeidos.size()>0)
            menuFlotante();
        else
            Toast.makeText(getContext(), "No se leyeron tags", Toast.LENGTH_SHORT).show();
    }

    public void menuFlotante()
    {
        if(menuSeleccion==false)
        {
            menuSeleccion=true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Tag para "+tipo);
            builder.setCancelable(false);

            final String[] items = tagsLeidos.toArray(new String[tagsLeidos.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int indice) {
                    //llanta_clave.set(llantaSeleccionada - 1, tagsLeidos.get(indice));
                    codigo=tagsLeidos.get(indice);
                    verificaCodigo();
                    menuSeleccion=false;
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int indice) {
                    //cancelar();
                    menuSeleccion=false;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        desconectarLector();
    }
}
