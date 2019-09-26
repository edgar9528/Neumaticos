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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.tdt.neumaticos.Clases.AsyncResponse;
import com.tdt.neumaticos.Clases.ConexionSocket;

public class LoginActivity extends AppCompatActivity implements AsyncResponse{

    Button button_sesion;
    TextInputEditText ti_usuario,ti_contrasena;
    String usuario,pass,permisos;
    boolean servidor_conf=false;

    ImageView iv_logo;

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
        verificarServidor();

        button_sesion = (Button) findViewById(R.id.Blogin);
        ti_usuario = (TextInputEditText) findViewById(R.id.TIusername);
        ti_contrasena = (TextInputEditText) findViewById(R.id.TIpassword);
        iv_logo = findViewById(R.id.logo);

        ti_usuario.setText("edgar");
        ti_contrasena.setText("edgar");

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(servidor_conf) {
                    if (!ti_usuario.getText().toString().isEmpty() && !ti_contrasena.getText().toString().isEmpty()) {
                        usuario = ti_usuario.getText().toString();
                        pass = ti_contrasena.getText().toString();

                        String command = "10|" + usuario + "|" + pass + "\u001a";

                        /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();*/



                        //Envia la peticion al socket, recibe respuesta (delegate) en
                        //public void processFinish(String output)
                        ConexionSocket conexionSocket = new ConexionSocket();
                        conexionSocket.command = command;
                        conexionSocket.context = LoginActivity.this;
                        conexionSocket.delegate = LoginActivity.this;
                        conexionSocket.execute();


                    } else
                        Toast.makeText(LoginActivity.this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(LoginActivity.this, "Debe configurar servidor y puerto", Toast.LENGTH_SHORT).show();
            }
        });


        //Doble click en imagen
        iv_logo.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(LoginActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    Intent intent = new Intent(LoginActivity.this, ConfiguracionActivity.class);
                    startActivity(intent);

                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //un click
                gestureDetector.onTouchEvent(event);
                return true;
            }});

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

        }
        catch (Exception e)
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
    public void verificarServidor()
    {
        SharedPreferences sharedPref = getSharedPreferences("ServidorPreferences",Context.MODE_PRIVATE);
        String servidor = sharedPref.getString("servidor","null");

        if(servidor.equals("null"))
            servidor_conf=false;
        else
            servidor_conf=true;

    }

}
