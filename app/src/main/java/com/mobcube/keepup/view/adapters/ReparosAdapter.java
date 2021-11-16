package com.mobcube.keepup.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobcube.keepup.R;
import com.mobcube.keepup.helpers.Constantes;
import com.mobcube.keepup.model.Reparos;
import com.mobcube.keepup.view.activities.VeiculoActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
        ImageView btnFinaliza, btnDelete;
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

            btnFinaliza = itemView.findViewById(R.id.iv_finaliza_reparo);
            btnDelete = itemView.findViewById(R.id.iv_delete_reparo);


        }
    }

    @NonNull
    @Override
    public MyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reparos_item,parent,false);
        return new MyViewholder(view);
    }

    @SuppressLint("RecyclerView")
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

        //BOTOES DE ACAO
        //FINALIZA REPARO
        holder.btnFinaliza.setOnClickListener(view -> {
            AlertDialog alertFinaliza = new AlertDialog.Builder(context)
                    .setTitle("ALERTA")
                    .setMessage("Tem certeza que deseja finalizar esse reparo?")
                    .setPositiveButton("SIM", null)
                    .setNegativeButton("NÃO",null)
                    .show();
            Button btnSim = alertFinaliza.getButton(DialogInterface.BUTTON_POSITIVE);
            btnSim.setOnClickListener(view1 -> {
                //Toast.makeText(context, "SIM PARA: "+reparos.getId_reparo(), Toast.LENGTH_SHORT).show();
                popUpFinaliza(reparos.getId_reparo(), reparos.getKm_reparo(), reparos.getDuracao(), reparos.getId_veiculo());
                alertFinaliza.dismiss();
            });

            Button btnNao = alertFinaliza.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNao.setOnClickListener(view2 -> {
                //Toast.makeText(context, "NÃO PARA: "+reparos.getId_reparo(), Toast.LENGTH_SHORT).show();
                alertFinaliza.dismiss();
            });


        });
        //DELETE
        holder.btnDelete.setOnClickListener(view -> {
            AlertDialog alertDelete = new AlertDialog.Builder(context)
                    .setTitle("ALERTA")
                    .setMessage("Tem certeza que deseja deletar o reparo?\nEssa ação não pode ser desfeita.")
                    .setPositiveButton("SIM",null)
                    .setNegativeButton("NÃO",null)
                    .show();
            Button btnSim = alertDelete.getButton(DialogInterface.BUTTON_POSITIVE);
            btnSim.setOnClickListener(view1 -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("reparo").document(reparos.getId_reparo())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            int x = position;
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Reparo excluído com sucesso.", Toast.LENGTH_SHORT).show();
                                reparosArrayList.remove(x);
                                notifyItemRemoved(x);
                                notifyDataSetChanged();

//                                int x = position;
//                                notifyItemRemoved(x);
                                //notifyItemRemoved(x);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Erro ao tentar excluir o reparo.\nTente novamente.", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDelete.dismiss();
            });

            Button btnNao = alertDelete.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNao.setOnClickListener(view2 -> {
                alertDelete.dismiss();
            });
            //Toast.makeText(context, "Reparo: "+reparos.getId_reparo(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return reparosArrayList.size();
    }


    private void popUpFinaliza(String id_reparo, int km_inicial, int duracao, String id_veiculo) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        AlertDialog dialog;

        final View popup = LayoutInflater.from(context).inflate(R.layout.popup_finaliza_reparo,null);
        TextView tvTipo = (TextView) popup.findViewById(R.id.tv_tipo_reparo_finalizar);
        TextView tvKm = (TextView) popup.findViewById(R.id.tv_km_inicial_reparo_finalizar);
        TextView tvData = (TextView) popup.findViewById(R.id.tv_data_reparo_finalizar);
        EditText cpKmAtualReparo = (EditText) popup.findViewById(R.id.cp_km_atual_finaliza_reparo);

        Button btnCancelar = (Button) popup.findViewById(R.id.btn_cancelar);
        Button btnSalvar = (Button) popup.findViewById(R.id.btn_salvar);

        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db2.collection("reparo").document(id_reparo);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    SimpleDateFormat data2 = new SimpleDateFormat("dd/MM/yyyy");

                    tvTipo.setText(doc.getString("tipo"));
                    tvKm.setText(String.valueOf(doc.get("km_reparo")));
                    tvData.setText(data2.format(doc.getDate("data_hora")));
                }
            }
        });

        alertBuilder.setView(popup);
        dialog = alertBuilder.create();
        dialog.show();

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(cpKmAtualReparo.getText().toString().equals("")){
                    AlertDialog dialogErro = new AlertDialog.Builder(context)
                            .setTitle("ALERTA")
                            .setMessage("Preencha o campo \"Quilometragem Atual\".")
                            .setPositiveButton("ENTENDI",null)
                            .show();

                    Button okBtn = dialogErro.getButton(AlertDialog.BUTTON_POSITIVE);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogErro.dismiss();
                        }
                    });
                } else {
                    //ALTERA O REPARO NO FIREBASE
                    int kmFinal = Integer.parseInt(cpKmAtualReparo.getText().toString());
                    int kmInicial = km_inicial;
                    int percorrido = (kmFinal-kmInicial);
                    int percorridoFalta = (duracao-percorrido);
                    Map<String, Object> reparo = new HashMap<>();


                    reparo.put("km_reparo_final",kmFinal);
                    reparo.put("status","finalizado");
                    reparo.put("percorrido",percorrido);
                    reparo.put("percorrido_falta",percorridoFalta);


                    //reparo.put("percorrido_falta",(Integer.parseInt(cpDuracaoReparo.getText().toString())-(kmAtual-Integer.parseInt(cpKmAtualReparo.getText().toString()))));

                    reparo.put("data_hora_final", FieldValue.serverTimestamp());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("reparo").document(id_reparo)
                            .update(reparo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Reparo Finalizado com sucesso.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    criaCopia(id_reparo, kmFinal, id_veiculo);
                                }
                            });
                }


            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }

    private void criaCopia(String id_reparo, int km_reparo, String id_veiculo) {
        AlertDialog alertCopia = new AlertDialog.Builder(context)
                .setTitle("ALERTA")
                .setMessage("\nDeseja criar uma cópia desse reparo?\n\nEssa ação irá copiar os dados do reparo ques está sendo finalizado.\n\nA quilometragem INICIAL dessa cópia será a quilometragem FINAL do reparo finalizado.")
                .setPositiveButton("SIM",null)
                .setNegativeButton("NÃO",null)
                .show();
        Button btnSim = alertCopia.getButton(DialogInterface.BUTTON_POSITIVE);
        btnSim.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference dr = db.collection("reparo").document(id_reparo);
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot res = task.getResult();

                        int percorrido = (kmAtual - km_reparo);
                        int duracao = Integer.parseInt(res.get("duracao").toString());
                        int percorridoFalta = (duracao-percorrido);

                        Map<String, Object> novoReparo = new HashMap<>();
                        novoReparo.put("data_hora",FieldValue.serverTimestamp());
                        novoReparo.put("duracao",res.get("duracao"));
                        novoReparo.put("id_veiculo",res.getString("id_veiculo"));
                        novoReparo.put("km_reparo",km_reparo);
                        novoReparo.put("percorrido",percorrido);
                        novoReparo.put("percorrido_falta",percorridoFalta);
                        novoReparo.put("status","ativo");
                        novoReparo.put("tipo",res.getString("tipo"));

                        db.collection("reparo").add(novoReparo)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(context, "Cópia criada com sucesso.", Toast.LENGTH_SHORT).show();
                                        alertCopia.dismiss();
                                        Intent intent = new Intent(context,VeiculoActivity.class);
                                        intent.putExtra(Constantes.KEY_VEICULO,id_veiculo);
                                        intent.putExtra(Constantes.KM_ATUAL,kmAtual);
                                        context.startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Falha ao criar a cópia.", Toast.LENGTH_SHORT).show();
                                        alertCopia.dismiss();
                                        Intent intent = new Intent(context,VeiculoActivity.class);
                                        intent.putExtra(Constantes.KEY_VEICULO,id_veiculo);
                                        intent.putExtra(Constantes.KM_ATUAL,kmAtual);
                                        context.startActivity(intent);
                                    }
                                });


                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            alertCopia.dismiss();
                            Toast.makeText(context, "Falha ao criar a cópia.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context,VeiculoActivity.class);
                            intent.putExtra(Constantes.KEY_VEICULO,id_veiculo);
                            intent.putExtra(Constantes.KM_ATUAL,kmAtual);
                            context.startActivity(intent);
                        }
                    });

        });

        Button btnNao = alertCopia.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNao.setOnClickListener(view -> {
            alertCopia.dismiss();
            Intent intent = new Intent(context,VeiculoActivity.class);
            intent.putExtra(Constantes.KEY_VEICULO,id_veiculo);
            intent.putExtra(Constantes.KM_ATUAL,kmAtual);
            context.startActivity(intent);
        });

    }

}
