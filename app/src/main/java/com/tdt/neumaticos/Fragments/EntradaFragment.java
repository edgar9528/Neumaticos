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
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;
import com.tdt.neumaticos.Clases.RevisaTextos;
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
import java.util.Arrays;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class EntradaFragment extends Fragment implements AsyncResponse {

    private static final String PARAMETRO="codigo";
    private static String tipoVentana;
    private static String tipoVehiculo;
    private static String ruta,responsable;
    private static int totalLlantas,totalRefacciones=0;
    private int peticion=0;
    private String user,kilometraje;

    int ejesD,ejesT,llanD,llanT;

    String iv_clave[];
    int iv_ids[], tv_ids[];
    int llantaCoincidencia=-1;
    boolean menuSeleccion=false;

    ArrayList<String> llanta_clave,llanta_numero,refaccion_numero,refaccion_tag,refaccion_mm,noRegistrado_mm,noRegistrado_tag;
    String llanta_tag[];
    String llanta_mm[];
    TableLayout tableLayout;
    TextView tv_lector,tv_nombreRuta;
    Button button_cancelar,button_terminar,button_conectar;
    ImageView imageViewLantas[];
    View vista;
    LayoutInflater layoutInflater;
    TextInputEditText ti_kilometraje;


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

    public static EntradaFragment newInstance (String tip,String tipVehi, String rut,int totLl,String resp)
    {
        EntradaFragment fragment = new EntradaFragment();
        Bundle args = new Bundle();
        args.putString(PARAMETRO,rut);
        tipoVentana=tip;
        tipoVehiculo=tipVehi;
        ruta=rut;
        totalLlantas=totLl;
        responsable=resp;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        user= activity.getUsuarioActivity();
        final View view = inflater.inflate(R.layout.fragment_entrada, container, false);

        vista = view;
        layoutInflater = inflater;

        tv_lector = view.findViewById(R.id.tv_lector);
        tv_nombreRuta = view.findViewById(R.id.tv_nombreRuta2);
        ti_kilometraje = view.findViewById(R.id.ti_kilometraje2);
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);

        button_cancelar = view.findViewById(R.id.button_cancelar5);
        button_terminar = view.findViewById(R.id.button_terminar5);
        button_conectar = view.findViewById(R.id.button_conectar5);

        totalTags= new ArrayList<>();
        llanta_tag = new String[totalLlantas];
        llanta_mm = new String[totalLlantas];
        refaccion_tag = new ArrayList<>();
        refaccion_numero = new ArrayList<>();
        refaccion_mm = new ArrayList<>();
        noRegistrado_mm = new ArrayList<>();
        noRegistrado_tag = new ArrayList<>();
        imageViewLantas = new ImageView[totalLlantas];

        tv_nombreRuta.setText(ruta+" | "+responsable);


        //inicianizar los tags vacios
        for(int i=0; i<totalLlantas;i++)
        {
            llanta_tag[i]="";
            llanta_mm[i]="";
        }

        //pide la información del vehiculo, despues la información de las llantas de la ruta
        String command = "06|"+tipoVehiculo+"\u001a";
        peticionSocket(command);

        conectarLector();

        button_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desconectarLector();
                goFragmentAnterior();
            }
        });

        button_conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conectarLector();
            }
        });

        button_terminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kilometraje = ti_kilometraje.getText().toString();
                RevisaTextos revisaTextos = new RevisaTextos();

                if(revisaTextos.esNumero(kilometraje))
                {
                    if(verificarMM())
                    {
                        String command = crearPeticion();
                        Log.d("salida","comando:"+command);
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Debe agregar mm a todos los neumáticos", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    Toast.makeText(getContext(), "Debe insertar un kilometraje", Toast.LENGTH_SHORT).show();
                }
            }
        });



        return view;
    }

    public String crearPeticion()
    {
        String command;

        if(tipoVentana.equals("Entrada"))
        {
            command = "15"+ruta+"|"+kilometraje+"|"+user;
        }
        else
        {
            command = "14"+ruta+"|"+kilometraje+"|"+user;
        }


        for(int i=0; i<totalLlantas;i++)
        {
            if(!llanta_tag[i].isEmpty())
            {
                command = command +"|"+llanta_numero.get(i)+"|"+llanta_tag[i]+"|"+llanta_mm[i];
            }
        }

        for(int i=0; i<refaccion_tag.size();i++)
        {
            command = command +"|0|"+refaccion_tag.get(i)+"|"+refaccion_mm.get(i);
        }

        for(int i=0; i<noRegistrado_tag.size();i++)
        {
            command = command +"|-1|"+noRegistrado_tag.get(i)+"|"+noRegistrado_mm.get(i);
        }


        return command;
    }

    public boolean verificarMM()
    {
        boolean llenosTotal=true;


        for(int i=0; i<totalLlantas;i++)
        {
            if(!llanta_tag[i].isEmpty())
            {
                if(llanta_mm[i].isEmpty())
                {
                    llenosTotal=false;
                }
            }
        }

        for(int i=0;i< refaccion_tag.size();i++)
        {
            if(refaccion_mm.get(i).isEmpty())
            {
                llenosTotal=false;
            }
        }

        return llenosTotal;
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
                    String command = "09|"+ruta+"\u001a";

                    peticionSocket(command);

                    //hace calculos para dibujar todas las llantas del camion
                    dibujarCamion();
                }
                else
                if(peticion==1)
                {
                    peticion++;
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
                            refaccion_mm.add("");
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
        TableRow tr = (TableRow) layoutInflater.inflate(R.layout.tabla_entrada, null);

        ((TextView) tr.findViewById(R.id.tabla_numero)).setText("#"); //Dato de la columna 1
        ((TextView) tr.findViewById(R.id.tabla_montada)).setText("Registrada"); //Dato de la columna 2
        ((TextView) tr.findViewById(R.id.tabla_mm)).setText("MM"); //Dato de la columna 3
        tableLayout.addView(tr);

        for(int i=0; i<totalLlantas;i++)
        {
            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_entrada, null);

            String montada; //verifica si la llanta tiene un tag o no (si tiene, esta montada)
            if(llanta_tag[i].isEmpty())
                montada="No";
            else
                montada="Si";

            ((TextView) tr.findViewById(R.id.tabla_numero)).setText(llanta_numero.get(i)); //Dato de la columna 1
            ((TextView) tr.findViewById(R.id.tabla_montada)).setText(montada); //Dato de la columna 2
            ((TextView) tr.findViewById(R.id.tabla_mm)).setText(llanta_mm[i]); //Dato de la columna 3

            tableLayout.addView(tr);
        }

        //TABLA REFACCIONES
        for(int i=0; i<totalRefacciones;i++)
        {
            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_entrada, null);

            ((TextView) tr.findViewById(R.id.tabla_numero)).setText("Refacción"+refaccion_numero.get(i)); //Dato de la columna 1
            ((TextView) tr.findViewById(R.id.tabla_montada)).setText("Si"); //Dato de la columna 2
            ((TextView) tr.findViewById(R.id.tabla_mm)).setText(refaccion_mm.get(i)); //Dato de la columna 3
            tableLayout.addView(tr);
        }

        //AGREGA NO REGISTRADOS
        for(int i=0; i<noRegistrado_mm.size();i++)
        {
            tr = (TableRow) layoutInflater.inflate(R.layout.tabla_entrada, null);

            ((TextView) tr.findViewById(R.id.tabla_numero)).setText("No regis"+(i+1)); //Dato de la columna 1
            ((TextView) tr.findViewById(R.id.tabla_montada)).setText("No"); //Dato de la columna 2
            ((TextView) tr.findViewById(R.id.tabla_mm)).setText(noRegistrado_mm.get(i)); //Dato de la columna 3
            tableLayout.addView(tr);


            Log.d("salida",noRegistrado_mm.get(i)+" | "+noRegistrado_tag.get(i));
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

                TextView textView = vista.findViewById(tv_ids[k]);
                textView.setText(String.valueOf(numero));

                k=iv_clave.length;
            }
        }
    }


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

            String mensaje= "Selecione un tag";

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(mensaje);
            builder.setCancelable(false);

            final String[] items = tagsLeidos.toArray(new String[tagsLeidos.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int indice) {

                    comprobarCodigo(tagsLeidos.get(indice));

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

    public void comprobarCodigo(String codigo)
    {
        //obtener todos los tags que estan guardados (montados)
        boolean almacenado = verAlmacenados(codigo);

        if(almacenado)
        {
            int tipo = verNeumaticos(codigo);
            if(tipo==0)
            {
                recibirMM("Neumatico: "+llantaCoincidencia,0,codigo);
            }
            else
            if (tipo==1)
            {
                recibirMM("Refacción: "+llantaCoincidencia,1,codigo);
            }
            else
            if (tipo==2)
            {
                recibirMM("No registrada: "+llantaCoincidencia,2,codigo);
            }
        }
        else
        {
            if(noRegistrado_tag.size()>=16)
            {
                Toast.makeText(getContext(), "Demasiadas llantas no registradas", Toast.LENGTH_SHORT).show();
            }
            else
                mensajeAdvertencia(codigo);

        }

    }

    public void mensajeAdvertencia(final String tag)
    {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Advertencia")
                .setMessage("La llanta no está registrada\n¿Está seguro de llevarla?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recibirMM("Lanta no registrada",3,tag); //opcion 3. registrar por primera vez
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    public void recibirMM(String mensaje, final int opc, final String t)
    {
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog,null);
        final EditText editText = (EditText) view.findViewById(R.id.ti_dialogmm);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Agrega los MM")
                .setMessage(mensaje)
                .setView(view)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lectura = String.valueOf(editText.getText());
                        if(opc==3)
                            noRegistrado_tag.add(t);
                        agregarMM(lectura,opc);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    public void agregarMM(String mm, int opc)
    {
        if(opc==0)//es neumatico
        {
            llanta_mm[llantaCoincidencia-1]=mm;
        }
        else
        if(opc==1)//es refaccion
        {
            refaccion_mm.set(llantaCoincidencia-1,mm);
        }
        else
        if(opc==2)//noregistrado pero ya esta almacenado
        {
            noRegistrado_mm.set(llantaCoincidencia-1,mm);
        }
        else// es no registrado
        {
            noRegistrado_mm.add(mm);
        }

        actualizarTabla();

    }

    public boolean verAlmacenados(String codigo)
    {
        ArrayList<String> tags = new ArrayList<>();

        for(int i=0; i<totalLlantas;i++)
        {
            if(!llanta_tag[i].isEmpty())
                tags.add(llanta_tag[i]);
        }
        for(int i=0; i<totalRefacciones;i++)
        {
            tags.add(refaccion_tag.get(i));
        }
        for (int i=0; i<noRegistrado_tag.size();i++)
        {
            tags.add(noRegistrado_tag.get(i));
        }

        boolean almacenado=false;
        for(int i=0; i<tags.size();i++)
        {
            if(tags.contains(codigo))
            {
                almacenado=true;
                i=tags.size();
            }
        }

        return almacenado;
    }

    public int verNeumaticos(String codigo)
    {
        int tipo = -1;

        //es refaccion
        for(int i=0; i<totalLlantas;i++)
        {
            if(llanta_tag[i].equals(codigo))
            {
                tipo=0;
                llantaCoincidencia=i+1;
                i=totalLlantas;
            }
        }

        //no es neumatico, verificar si es refaccion
        if(tipo==-1)
        {
            for (int i=0; i<refaccion_tag.size();i++)
                if(refaccion_tag.get(i).equals(codigo))
                {
                    tipo=1;
                    llantaCoincidencia=i+1;
                    i=refaccion_tag.size();
                }
        }

        // es no registrado, buscar en el arreglo
        if(tipo==-1)
        {
            for (int i=0; i<noRegistrado_tag.size();i++)
                if(noRegistrado_tag.get(i).equals(codigo))
                {
                    tipo=2;
                    llantaCoincidencia=i+1;
                    i=noRegistrado_tag.size();
                }
        }

        return tipo;
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
