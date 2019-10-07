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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
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
import java.util.Arrays;

public class MontajeFragment extends Fragment implements AsyncResponse {

    //variables del fragment
    private static final String PARAMETRO="codigo";
    private static String tipo,tipoVehiculo,ruta;
    private static int totalLlantas;

    private int peticion=0;

    int ejesD,ejesT,llanD,llanT;

    String iv_clave[];
    int iv_ids[], tv_ids[];
    int llantaSeleccionada=-1;
    boolean menuSeleccion=false;

    ArrayList<String> llanta_clave,llanta_numero;
    String llanta_tag[];
    TableLayout tableLayout;
    TextView tv_seleccionado,tv_lector;
    Button button_cancelar,button_terminar,button_conectar;
    ImageView imageViewLantas[];
    View vista;
    LayoutInflater layoutInflater;

    //Variables para leer codigo
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    private EventHandler eventHandler;
    ToneGenerator toneGenerator;
    ArrayList<String> totalTags,tagsLeidos;

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
        tv_lector = view.findViewById(R.id.tv_lector);
        button_cancelar = view.findViewById(R.id.button_cancelar4);
        button_terminar = view.findViewById(R.id.button_terminar4);
        button_conectar = view.findViewById(R.id.button_conectar);
        totalTags= new ArrayList<>();
        llanta_tag = new String[totalLlantas];
        imageViewLantas = new ImageView[totalLlantas];

        //pide la información del vehiculo, despues la información de las llantas de la ruta
        String command = "06|"+tipoVehiculo+"\u001a";
        peticionSocket(command);

        conectarLector();

        button_terminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "boton finalizar", Toast.LENGTH_SHORT).show();
            }
        });

        button_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goFragmentAnterior();
            }
        });

        button_conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conectarLector();
            }
        });

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
                        actualizarTabla();
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

    public void actualizarTabla()
    {
        tableLayout.removeAllViews();

        TableRow tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

        ((TextView) tr.findViewById(R.id.lTitle)).setText("#"); //Dato de la columna 1
        ((TextView) tr.findViewById(R.id.lDetail)).setText("TAG"); //Dato de la columna 2
        tableLayout.addView(tr);

        for(int i=0; i<totalLlantas;i++)
        {
            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

            ((TextView) tr.findViewById(R.id.lTitle)).setText(llanta_numero.get(i)); //Dato de la columna 1
            ((TextView) tr.findViewById(R.id.lDetail)).setText(llanta_tag[i]); //Dato de la columna 2
            tableLayout.addView(tr);
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
                imageViewLantas[numero-1] = vista.findViewById(iv_ids[k]);
                imageViewLantas[numero-1].setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.llanta));
                imageViewLantas[numero-1].setTag(numero);
                imageViewLantas[numero-1].setOnClickListener(ivListener);

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

            //regresamos la anterior a llanta negra
            if(llantaSeleccionada>0)
                imageViewLantas[llantaSeleccionada-1].setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.llanta));

            llantaSeleccionada= Integer.parseInt(view.getTag().toString());

            //se pinta de rojo la nueva seleccionada
            imageViewLantas[llantaSeleccionada-1].setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.llantaroja));


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

    //LEER CODIGO TC20

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

        //si hay una coneccion, se cierra
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
                    tv_lector.setText("Lector conectado");
                }
                else
                {
                    Toast.makeText(getContext(), "Lector no conectado", Toast.LENGTH_LONG).show();
                    tv_lector.setText("Lector no conectado");
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

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
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

                    if(llantaSeleccionada>-1)
                    {
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
                            return;
                        }
                    }
                    else
                    {
                        notificacion("Selecciona un neumático");
                        return;
                    }
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {

                    if(llantaSeleccionada>-1 && menuSeleccion==false)
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
                    else
                        return;
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
            builder.setTitle("Tag para neumático: " + llantaSeleccionada);
            builder.setCancelable(false);

            final String[] items = tagsLeidos.toArray(new String[tagsLeidos.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int indice) {
                    //llanta_clave.set(llantaSeleccionada - 1, tagsLeidos.get(indice));
                    llanta_tag[llantaSeleccionada-1]=tagsLeidos.get(indice);
                    actualizarTabla();
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
