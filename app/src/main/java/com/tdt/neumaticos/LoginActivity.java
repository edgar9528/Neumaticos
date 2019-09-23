package com.tdt.neumaticos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    Button button_sesion;
    TextInputEditText ti_usuario,ti_contrasena;
    String usuario,pass,permisos;

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

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ti_usuario.getText().toString().isEmpty() && !ti_contrasena.getText().toString().isEmpty())
                {
                    usuario=ti_usuario.getText().toString();
                    pass=ti_contrasena.getText().toString();

                    String command="10|"+usuario+"|"+pass+"\u001a";

                    //Envia la peticion al socket, recibe respuesta en
                    //public void processFinish(String output)
                    ConexionSocket conexionSocket = new ConexionSocket();
                    conexionSocket.command=command;
                    conexionSocket.context=LoginActivity.this;
                    conexionSocket.delegate = LoginActivity.this;
                    conexionSocket.execute();

                }
                else
                    Toast.makeText(LoginActivity.this,"Debe llenar todos los campos",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //recibe respuesta de la peticion al socket
    @Override
    public void processFinish(String output){

        try
        {
            String clave = output.substring(0,2);
            String mensaje = output.substring(2,output.length());
            mensaje=mensaje.trim(); // elimina espacios en blanco al principio y final

            if(clave.equals("BC"))
            {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

                permisos=mensaje;

                guardarUsuario();
            }
            else
            {
                Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
            }

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error: "+e.toString(), Toast.LENGTH_LONG).show();
        }


    }

    public void guardarUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("usuario",usuario);
        editor.putString("pass",pass);
        editor.putString("permisos",permisos);
        editor.apply();
    }

    public void verificarUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        usuario = sharedPref.getString("usuario","null");

        if(!usuario.equals("null"))
        {

        }
    }
}
