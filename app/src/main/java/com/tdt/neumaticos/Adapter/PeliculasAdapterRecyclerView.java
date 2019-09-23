package com.tdt.neumaticos.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tdt.neumaticos.DetallePeliculaActivity;
import com.tdt.neumaticos.Model.PeliculaCardView;
import com.tdt.neumaticos.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PeliculasAdapterRecyclerView extends RecyclerView.Adapter<PeliculasAdapterRecyclerView.PeliculasViewHolder> {

    private ArrayList<PeliculaCardView> peliculaCardViews;
    private int resource;
    private Activity activity;


    public PeliculasAdapterRecyclerView(ArrayList<PeliculaCardView> peliculaCardViews, int resource, Activity activity) {
        this.peliculaCardViews = peliculaCardViews;
        this.resource = resource;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PeliculasViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(resource,viewGroup,false);

        return new PeliculasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PeliculasViewHolder peliculasViewHolder, int i) {
        PeliculaCardView peliculaCardView = peliculaCardViews.get(i);

        Picasso.with(activity.getApplicationContext()).load(peliculaCardView.getImagen()).into(peliculasViewHolder.imagenCard);
        peliculasViewHolder.tituloCard.setText(peliculaCardView.getTitulo());
        peliculasViewHolder.autorCard.setText(peliculaCardView.getAutor());
        peliculasViewHolder.generoCard.setText(peliculaCardView.getGenero());

        final String id=peliculaCardView.getId_pelicula();
        final String id_usuarioEnviar=peliculaCardView.getId_usuarioEnviar();

        peliculasViewHolder.tituloCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,DetallePeliculaActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("idUsuarioRecibido", id_usuarioEnviar);
                activity.startActivity(intent);
            }
        });

        peliculasViewHolder.tituloCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,DetallePeliculaActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("idUsuarioRecibido", id_usuarioEnviar);
                activity.startActivity(intent);
            }
        });

        peliculasViewHolder.tituloCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,DetallePeliculaActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("idUsuarioRecibido", id_usuarioEnviar);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return peliculaCardViews.size();
    }

    public class PeliculasViewHolder extends RecyclerView.ViewHolder{

        private ImageView imagenCard;
        private TextView  tituloCard;
        private TextView  autorCard;
        private TextView  generoCard;

        public PeliculasViewHolder(@NonNull View itemView) {
            super(itemView);

            imagenCard= itemView.findViewById(R.id.ImagenCard);
            tituloCard= itemView.findViewById(R.id.TituloCard);
            autorCard= itemView.findViewById(R.id.AutorCard);
            generoCard= itemView.findViewById(R.id.GeneroCard);

        }
    }


}
