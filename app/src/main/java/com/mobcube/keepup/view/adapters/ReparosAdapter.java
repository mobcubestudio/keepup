package com.mobcube.keepup.view.adapters;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobcube.keepup.R;
import com.mobcube.keepup.model.Reparos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ReparosAdapter extends RecyclerView.Adapter<ReparosAdapter.MyViewholder> {

    Context context;
    ArrayList<Reparos> reparosArrayList;
    Integer kmAtual;



    public ReparosAdapter(Context context, ArrayList<Reparos> reparosArrayList, Integer kmAtual) {
        this.context = context;
        this.reparosArrayList = reparosArrayList;
        this.kmAtual = kmAtual;
    }

    public static class MyViewholder extends RecyclerView.ViewHolder{

        TextView tvtipo, tvKmReparo, tvDataReparo, percorrido, kmFinal, tvFalta, tvDuracao;
        View barra_progressao;
        public MyViewholder(@NonNull View itemView) {
            super(itemView);
            tvtipo = itemView.findViewById(R.id.tv_tipo_reparo);
            tvKmReparo = itemView.findViewById(R.id.tv_km_reparo);
            tvDataReparo = itemView.findViewById(R.id.tv_data_reparo);
            tvDuracao = itemView.findViewById(R.id.tv_km_duracao);
            barra_progressao = itemView.findViewById(R.id.vw_percentual);
            tvFalta = itemView.findViewById(R.id.tv_falta_reparo);

            percorrido = itemView.findViewById(R.id.tv_km_percorrido);
            kmFinal = itemView.findViewById(R.id.tv_km_final);



        }
    }

    @NonNull
    @Override
    public MyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reparos_item,parent,false);
        return new MyViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewholder holder, int position) {
        Reparos reparos = reparosArrayList.get(position);
        holder.tvtipo.setText(reparos.getTipo());
        //Log.d("D", "ISO==> DURACAO: "+reparos.getDuracao());
        holder.tvKmReparo.setText("Reparo: "+reparos.getKm_reparo().toString()+" km");
        holder.tvDuracao.setText("Duração: "+reparos.getDuracao().toString()+" km");
        SimpleDateFormat data = new SimpleDateFormat("dd/MM/yyyy");
        //Date dataRep = reparos.getData_hora();
        //Log.d("ISO", "ISO==> Data: " + dataF.format(dataRep));

        if(reparos.getData_hora()!=null) {

            //Log.d("ISO", "ISO==> Data: " + data.format(reparos.getData_hora()));
            //Date data = reparos.getData_hora().for;
            holder.tvDataReparo.setText("Data: " + data.format(reparos.getData_hora()));
        }
        holder.kmFinal.setText(reparos.getDuracao().toString());

        Drawable drawable = holder.barra_progressao.getBackground();



        Log.d("ISO","ISO==> KM TROCA: "+reparos.getKm_reparo());
        Log.d("ISO","ISO==> KM ATUAL: "+kmAtual);

        int diferenca = reparos.getPercorrido();
        holder.percorrido.setText(reparos.getPercorrido().toString());
        Log.d("ISO","ISO==> DIFF: "+diferenca);
        int percentual = ((diferenca*10000) / reparos.getDuracao());
        Log.d("ISO","ISO==> PERCENTUAL: "+percentual);

        if(drawable instanceof ClipDrawable){
            ((ClipDrawable)drawable).setLevel(percentual);
            Log.d("ISO","ISO==> MARGIN: "+((ClipDrawable) drawable).getLevel());
        }

        holder.tvFalta.setText("Troca em: "+reparos.getPercorridoFalta()+" km");




        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        p.setMarginStart(percentual/12);
        holder.percorrido.setLayoutParams(p);

    }

    @Override
    public int getItemCount() {
        return reparosArrayList.size();
    }


}
