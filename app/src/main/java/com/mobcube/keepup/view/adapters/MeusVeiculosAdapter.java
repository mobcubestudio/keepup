package com.mobcube.keepup.view.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobcube.keepup.R;
import com.mobcube.keepup.helpers.Constantes;
import com.mobcube.keepup.model.MeusVeiculos;
import com.mobcube.keepup.view.activities.VeiculoActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MeusVeiculosAdapter  extends RecyclerView.Adapter<MeusVeiculosAdapter.MyViewHolder> {

    Context context;
    ArrayList<MeusVeiculos> meusVeiculosArrayList;


    private MeusVeiculosAdapter.ClickListener<MeusVeiculos> clickListener;

    public MeusVeiculosAdapter(Context context, ArrayList<MeusVeiculos> meusVeiculosArrayList) {
        this.context = context;
        this.meusVeiculosArrayList = meusVeiculosArrayList;
    }

    public interface ClickListener<T> {
        void onItemClick(T data);
    }

    public void setOnItemClickListener(MeusVeiculosAdapter.ClickListener<MeusVeiculos> clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MeusVeiculosAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.meus_veiculos_item,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeusVeiculosAdapter.MyViewHolder holder, int position) {
        MeusVeiculos meusVeiculos = meusVeiculosArrayList.get(position);
        holder.tvApelido.setText(meusVeiculos.getApelido());
        holder.tvModelo.setText(meusVeiculos.getModelo());
        holder.tvKmAtual.setText(meusVeiculos.getKm_atual()+" km");



        if(meusVeiculos.getTipo().equals("Moto")){
            holder.ivFoto.setImageResource(R.drawable.moto_default);
        }
        if(meusVeiculos.getTipo().equals("Carro")){
            holder.ivFoto.setImageResource(R.drawable.carro_default);
        }
        if(meusVeiculos.getTipo().equals("Camionete")){
            holder.ivFoto.setImageResource(R.drawable.camionete_default);
        }
        if(meusVeiculos.getTipo().equals("Caminhão")){
            holder.ivFoto.setImageResource(R.drawable.caminhao_default);
        }

        StorageReference fotoRef = FirebaseStorage.getInstance().getReference();
        fotoRef.child("veiculos/"+meusVeiculos.getId_veiculo())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(holder.ivFoto);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERRO","ISO==> ERRO FOTO--"+e.getMessage());
                    }
                });


        holder.linha.setOnClickListener(v -> clickListener.onItemClick(meusVeiculos));


    }

    @Override
    public int getItemCount() {
        return meusVeiculosArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView linha;
        TextView tvApelido, tvMarca, tvModelo, tvAno, tvKmAtual;
        ImageView ivFoto;
        //StorageReference fotoRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvApelido = itemView.findViewById(R.id.tv_apelido_meu_veiculo);
            tvModelo = itemView.findViewById(R.id.tv_modelo_meu_veiculo);
            tvKmAtual = itemView.findViewById(R.id.tv_km_atual_meu_veiculo);
            ivFoto = itemView.findViewById(R.id.iv_foto_meu_veiculo);
            linha = itemView.findViewById(R.id.linha_meu_veiculo_item);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
