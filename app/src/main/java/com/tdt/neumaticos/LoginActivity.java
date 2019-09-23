package com.tdt.neumaticos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class LoginActivity extends AppCompatActivity {

    Button button_sesion,button_conectar;
    TextInputEditText ti_usuario,ti_contrasena;
    String usuario,pass;

    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿Desea salir de la app?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                //cancelar();
            }
        });
        dialogo1.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //VERIFICAR SI ES USUARIO YA SE HA LOGUEADO ANTES
        verificarUsuario();

        button_sesion = (Button) findViewById(R.id.Blogin);
        ti_usuario = (TextInputEditText) findViewById(R.id.TIusername);
        ti_contrasena = (TextInputEditText) findViewById(R.id.TIpassword);

        button_conectar = findViewById(R.id.button_conectar);

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ti_usuario.getText().toString().isEmpty() && !ti_contrasena.getText().toString().isEmpty())
                {
                    usuario=ti_usuario.getText().toString();
                    pass=ti_contrasena.getText().toString();

                    conectar();

                    guardarUsuario();

                }
                else
                    Toast.makeText(LoginActivity.this,"Debe llenar todos los campos",Toast.LENGTH_SHORT).show();
            }
        });

        button_conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

    public void conectar()
    {
        SERVER_IP = "192.168.0.188";
        SERVER_PORT = 2048;

        new Thread(new Runnable() {
            public void run() {
                InetAddress serverAddr;
                try {
                    String command="10|edgar|edgar"+"\u001a";
                    String command2="10|"+usuario+"|"+pass+"\u001a";

                    Log.d("salida",command);
                    Log.d("salida",command2);

                    //String command= String.format("10|%s|%snull", usuario, pass);

                    //byte[] commandByte = command.getBytes();

                    //Establecer conexion con servidor
                    serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVER_PORT);


                    //enviar el parametro
                    OutputStream out = socket.getOutputStream();
                    PrintWriter output = new PrintWriter(out);
                    output.println(command);
                    output.flush();


                    //recibir información

                    if(socket != null && socket.isConnected()){

                        int byteCount=1024;
                        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                        byte[] buffer = new byte[byteCount];
                        int con=0;
                        while(input.read(buffer, 0, byteCount) != -1  )
                        {
                            String decoded = respuesta(buffer);
                            Log.d("salida",decoded);
                            con++;

                            if(con==2)
                                break;
                        }

                        /*BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        while((line = input.readLine()) != null){
                            Log.d("salida",line);
                        }*/

                    }

                    //socket.close();

                    Log.d("salida","cerro conexion");


                } catch (Exception e) {

                }
            }
        }).start();
    }

    public String respuesta(byte[] buffer)
    {
        StringBuilder sb = new StringBuilder(buffer.length);
        for (int i = 0; i < buffer.length && buffer[i]!=0 ; ++ i)
        {
            //Log.d("salida", String.valueOf(buffer[i] ) +" "+(char) buffer[i]  );
            if (buffer[i] < 0)
                throw new IllegalArgumentException();
            sb.append((char) buffer[i]);
        }
        return sb.toString();
    }

    public void guardarUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("usuario",usuario);
        editor.putString("pass",pass);
        editor.apply();
    }

    public void verificarUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        usuario = sharedPref.getString("usuario","null");
        pass = sharedPref.getString("pass","null");

        if(!usuario.equals("null"))
        {

        }
    }
}
