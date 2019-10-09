package com.tdt.neumaticos.Clases;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import com.tdt.neumaticos.LoginActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

            //byte[] utf8ByteOutput = command.getBytes(StandardCharsets.UTF_16);
            //byte[] ascii = command.getBytes(StandardCharsets.US_ASCII);

            //byte[] comm=command.getBytes( StandardCharsets.US_ASCII );

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

                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                byte[] contents = new byte[4096];

                int bytesRead = 0;
                String strFileContents="";
                while((bytesRead = in.read(contents)) != -1)
                {
                    strFileContents += new String(contents, 0, bytesRead);
                    if(strFileContents.charAt(strFileContents.length()-1)== '\u001a')
                    {
                        break;
                    }
                }

                respuestaSocket = strFileContents.substring(13,strFileContents.length()-1);
                Log.d("salida","_"+respuestaSocket+"_");
            }

            //enviar el parametro
            //metodo cerrar conexion del socket
            command="00|"+"\u001a";
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
