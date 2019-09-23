package com.tdt.neumaticos.Clases;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tdt.neumaticos.LoginActivity;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ConexionSocket extends AsyncTask<String,Integer,String>
{
    private ProgressDialog progreso;

    public Context context;
    public String command;
    public AsyncResponse delegate = null;

    private String SERVER_IP;
    private int SERVER_PORT;
    private Socket socket;
    private String respuestaSocket;

    @Override protected void onPreExecute() {
        progreso = new ProgressDialog(context);
        progreso.setMessage("Verificando información...");
        progreso.setCancelable(false);
        progreso.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "false";

        InfoServidor infoServidor = new InfoServidor(context);

        SERVER_IP = infoServidor.getServidor();
        SERVER_PORT = infoServidor.getPuerto();

        InetAddress serverAddr;

        try {

            //Establecer conexion con servidor
            serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVER_PORT);

            //enviar el parametro
            OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out);
            output.println(command);
            output.flush();

            //recibir información
            if(socket != null && socket.isConnected())
            {
                int byteCount=1024;
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                byte[] buffer = new byte[byteCount];
                int con=0;

                while(input.read(buffer, 0, byteCount) != -1  )
                {
                    String decoded = ConvertirRespuesta(buffer);
                    Log.d("salida",decoded);
                    respuestaSocket=decoded;
                    con++;
                    for (int i = 0; i < buffer.length && buffer[i]!=0 ; ++ i)
                        buffer[i] =32;
                    if(con==2)
                        break;
                }
            }

            //enviar el parametro
            //metodo cerrar conexion del socket
            command="00"+"\u001a";
            output.println(command);
            output.flush();

            socket.close();
            result=respuestaSocket;

        } catch (Exception e) {
            result= e.toString();
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progreso.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String resultado) {
        super.onPostExecute(resultado);
        progreso.dismiss();

        delegate.processFinish(resultado);
        //Toast.makeText(context.getApplicationContext(), resultado, Toast.LENGTH_LONG).show();
    }

    public String ConvertirRespuesta(byte[] buffer)
    {
        StringBuilder sb = new StringBuilder(buffer.length);

        for (int i = 0; i < buffer.length && buffer[i]!=0 ; ++ i)
        {
            //Log.d("salida", String.valueOf(buffer[i] ) +" "+(char) buffer[i]  );
            if (buffer[i] < 0)
            {
                if(buffer[i] ==-15)
                    sb.append("ñ");
                else
                    sb.append("_");
            }
            else
                sb.append((char) buffer[i]);
        }
        return sb.toString();

    }


}
