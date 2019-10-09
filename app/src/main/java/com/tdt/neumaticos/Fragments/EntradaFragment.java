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
import android.widget.CheckBox;
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

public class EntradaFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String tipo;
    private static String tipoVehiculo;
    private static String ruta;
    private static int totalLlantas,totalRefacciones=0;
    private int peticion=0;

    int ejesD,ejesT,llanD,llanT;

    String iv_clave[];
    int iv_ids[], tv_ids[];
    int llantaSeleccionada=-1;
    boolean menuSeleccion=false;

    ArrayList<String> llanta_clave,llanta_numero,refaccion_numero,refaccion_tag;
    String llanta_tag[];
    TableLayout tableLayout,tableLayout2;
    TextView tv_seleccionado,tv_lector;
    Button button_cancelar,button_terminar,button_conectar;
    ImageView imageViewLantas[];
    View vista;
    LayoutInflater layoutInflater;
    CheckBox cb_refacciones;


    //Variables para leer codigo
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    private EventHandler eventHandler;
    ToneGenerator toneGenerator;
    ArrayList<String> totalTags,tagsLeidos;

    public EntradaFragment() {
        // Required empty public constructor
    }

    public static EntradaFragment newInstance (String tip,String tipVehi, String rut,int totLl)
    {
        EntradaFragment fragment = new EntradaFragment();
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
        final View view = inflater.inflate(R.layout.fragment_entrada, container, false);


        vista = view;
        layoutInflater = inflater;

        tv_seleccionado = view.findViewById(R.id.tv_seleccionado);
        tv_lector = view.findViewById(R.id.tv_lector);
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        tableLayout2 = (TableLayout) view.findViewById(R.id.tableLayout2);
        totalTags= new ArrayList<>();
        llanta_tag = new String[totalLlantas];
        refaccion_tag = new ArrayList<>();
        refaccion_numero = new ArrayList<>();
        imageViewLantas = new ImageView[totalLlantas];

        //inicianizar los tags vacios
        for(int i=0; i<totalLlantas;i++)
            llanta_tag[i]="";



        //pide la información del vehiculo, despues la información de las llantas de la ruta
        String command = "06|"+tipoVehiculo+"\u001a";
        peticionSocket(command);




        Log.d("salida","creado fragment entrada");

        return view;
    }


    private void peticionSocket(String command)
    {
        ConexionSocket conexionSocket2 = new ConexionSocket();
        conexionSocket2.command = command;
        conexionSocket2.context = EntradaFragment.this.getActivity();
        conexionSocket2.delegate = this;
        conexionSocket2.execute();
    }


    //RECIBE TODAS LAS PETICIONES AL SOCKET
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
                    //recibe la información de las llantas de acuerdo al tipo de vehiculo
                    String[] resultado = mensaje.split("\u0009");

                    ejesD = Integer.parseInt(resultado[0]);
                    ejesT = Integer.parseInt(resultado[1]);
                    llanD = Integer.parseInt(resultado[2]);
                    llanT = Integer.parseInt(resultado[3]);

                    peticion++;

                    //Ejecuta la siguiente peticion(Solicita llantas ya almacenadas)
                    String command = "12|"+ruta+"\u001a";

                    peticionSocket(command);

                    //hace calculos para dibujar todas las llantas del camion
                    dibujarCamion();
                }
                else
                if(peticion==1)
                {
                    peticion++;
                    if(mensaje.isEmpty())
                    {
                        //quiere decir que no tiene llantas/refacciones registradas
                        //las llantas se inicializan en "" (vacio)
                        totalRefacciones=0;
                        //Muestra las llantas/refacciones y sus tags
                        actualizarTabla();
                    }
                    else
                    {
                        totalRefacciones=0;
                        //Resultado contiene los neumaticos ya asignados
                        String[] resultado = mensaje.split("\u0009");

                        for(int i=0,k=1; i<resultado.length;i=i+2,k=k+2)
                        {
                            //si el numero del neumatico es 0, entonces es refaccion y se agrega
                            if(resultado[k].equals("0"))
                            {
                                totalRefacciones++;
                                refaccion_tag.add(resultado[i]);
                                refaccion_numero.add(String.valueOf(totalRefacciones));
                            }
                            else
                            {
                                //si no, entonces se agrega a las llantas dependiendo el numero de llanta
                                for(int j=0; j<llanta_numero.size();j++)
                                {
                                    if(resultado[k].equals(llanta_numero.get(j)))
                                    {
                                        llanta_tag[j]=resultado[i];
                                    }
                                }
                            }
                        }
                        //Muestra las llantas/refacciones y sus tags
                        actualizarTabla();
                    }
                }
                else
                {
                    if(mensaje.isEmpty())
                    {
                        //Realizo el montaje con exito
                        Toast.makeText(getContext(), "Montaje agregado", Toast.LENGTH_LONG).show();
                        desconectarLector();
                        goFragmentAnterior();
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


    public void actualizarTabla()
    {
        //TABLA NEUMATICOS
        tableLayout.removeAllViews();
        TableRow tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

        ((TextView) tr.findViewById(R.id.lTitle)).setText("#"); //Dato de la columna 1
        ((TextView) tr.findViewById(R.id.lDetail)).setText("TAG NUMATICO"); //Dato de la columna 2
        tableLayout.addView(tr);

        for(int i=0; i<totalLlantas;i++)
        {
            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

            ((TextView) tr.findViewById(R.id.lTitle)).setText(llanta_numero.get(i)); //Dato de la columna 1
            ((TextView) tr.findViewById(R.id.lDetail)).setText(llanta_tag[i]); //Dato de la columna 2
            tableLayout.addView(tr);
        }


        //TABLA REFACCIONES

        tableLayout2.removeAllViews();
        TableRow tr2 = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

        ((TextView) tr2.findViewById(R.id.lTitle)).setText("#"); //Dato de la columna 1
        ((TextView) tr2.findViewById(R.id.lDetail)).setText("TAG REFACCIÓN"); //Dato de la columna 2
        tableLayout2.addView(tr2);

        for(int i=0; i<totalRefacciones;i++)
        {
            tr2 = (TableRow) layoutInflater.inflate(R.layout.tabla_detalles, null);

            ((TextView) tr2.findViewById(R.id.lTitle)).setText(refaccion_numero.get(i)); //Dato de la columna 1
            ((TextView) tr2.findViewById(R.id.lDetail)).setText(refaccion_tag.get(i)); //Dato de la columna 2
            tableLayout2.addView(tr2);
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

    public void mensajeError(String men)
    {
        android.support.v7.app.AlertDialog.Builder dialogo1 = new android.support.v7.app.AlertDialog.Builder(getContext());
        dialogo1.setTitle("Error");
        dialogo1.setMessage(men);
        dialogo1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

            }
        });
        dialogo1.show();
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

            String mensaje;
            if(cb_refacciones.isChecked())
                mensaje="Tag para refacción";
            else
                mensaje= "Tag para neumático: " + llantaSeleccionada;


            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(mensaje);
            builder.setCancelable(false);

            final String[] items = tagsLeidos.toArray(new String[tagsLeidos.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int indice) {
                    if(cb_refacciones.isChecked())
                    {
                        refaccion_tag.add(tagsLeidos.get(indice));
                        refaccion_numero.add( String.valueOf( refaccion_tag.size()) );
                        totalRefacciones++;
                        for(int i=0;i<refaccion_tag.size();i++)
                        {
                            Log.d("salida","tg: "+refaccion_tag.get(i));
                        }
                    }
                    else
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
