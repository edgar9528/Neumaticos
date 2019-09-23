package com.tdt.neumaticos.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.Model.AmigoCardView;
import com.tdt.neumaticos.PeliculasVistasActivity;
import com.tdt.neumaticos.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AmigosAdapterRecyclerView extends RecyclerView.Adapter<AmigosAdapterRecyclerView.AmigosViewHolder> {

    private ArrayList<AmigoCardView> amigoCardViews;
    private int resource;
    private Activity activity;

    public static  String SERVIDOR="";
    RequestQueue requestQueue;

    public AmigosAdapterRecyclerView(ArrayList<AmigoCardView> amigoCardViews, int resource, Activity activity) {
        this.amigoCardViews = amigoCardViews;
        this.resource = resource;
        this.activity = activity;
    }

    @NonNull
    @Override
    public AmigosAdapterRecyclerView.AmigosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(resource,viewGroup,false);

        return new AmigosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmigosAdapterRecyclerView.AmigosViewHolder amigosViewHolder, int i) {
        AmigoCardView amigoCardView = amigoCardViews.get(i);

        amigosViewHolder.correoCard.setText(amigoCardView.getCorreo() );
        amigosViewHolder.nombreCard.setText(amigoCardView.getNombre() );

        final String seguidor= amigoCardView.getId_usuarioActual();
        final String seguido = amigoCardView.getId_usuario();
        final String bandera = amigoCardView.getIdentificador();


        amigosViewHolder.correoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bandera.equals("buscar"))
                {
                    seguirUsuario(seguidor,seguido);
                }
                else
                {
                    Intent intent = new Intent(activity,PeliculasVistasActivity.class);
                    intent.putExtra("id", seguido);
                    activity.startActivity(intent);
                }
            }
        });

        amigosViewHolder.nombreCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bandera.equals("buscar"))
                {
                    seguirUsuario(seguidor,seguido);
                }
                else
                {
                    Intent intent = new Intent(activity,PeliculasVistasActivity.class);
                    intent.putExtra("id", seguido);
                    activity.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return amigoCardViews.size();
    }

    public class AmigosViewHolder extends RecyclerView.ViewHolder {

        private TextView  correoCard;
        private TextView  nombreCard;

        public AmigosViewHolder(@NonNull View itemView) {
            super(itemView);

            correoCard= itemView.findViewById(R.id.CorreoCard);
            nombreCard= itemView.findViewById(R.id.NombreCard);
        }
    }

    public void seguirUsuario(final String seguidor, final String seguido)
    {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(activity,R.style.CustomAlertDialog);
        dialogo1.setTitle("Amigos");
        String mensaje="¿Desea seguir este usuario?\n";

        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                obtenerServidor();
                final String URL= SERVIDOR +"insertarSeguidores.php";
                ejecutarServicio(URL,seguidor,seguido);
            }
        });
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                //cancelar();
            }
        });
        dialogo1.show();
    }

    private void ejecutarServicio(String URL, final String seguidor, final String seguido)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.isEmpty())
                {
                    Toast.makeText(activity.getApplicationContext(), "Usuario seguido", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activity,MainActivity.class);
                    activity.startActivity(intent);
                }
                else
                    Toast.makeText(activity.getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity.getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("ID_USUARIO", seguidor);
                parametros.put("ID_USUARIO_SEGUIDO",seguido);
                return parametros;
            }
        };
        requestQueue = Volley.newRequestQueue(this.activity.getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void obtenerServidor()
    {
        SharedPreferences sharedPref = activity.getSharedPreferences("DireccionIP",Context.MODE_PRIVATE);
        SERVIDOR = sharedPref.getString("ip","null");
    }
}
