package com.mobcube.keepup.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobcube.keepup.R;
import com.mobcube.keepup.helpers.Constantes;
import com.mobcube.keepup.model.MeusVeiculos;
import com.mobcube.keepup.model.Reparos;
import com.mobcube.keepup.view.adapters.ReparosAdapter;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VeiculoActivity extends AppCompatActivity {


    String idVeiculo;
    FirebaseFirestore db;

    TextView tvApelido, tvModelo, tvMarca, tvUltimaKmInformada;

    //POP UPS
    AlertDialog.Builder alertBuilder, alertKmAtual;
    AlertDialog dialog;
    Button btnNovoReparo, btnAtualizarKm, btnFinalizados;

    //LISTA DE REPAROS
    RecyclerView rvReparos;
    ArrayList<Reparos> reparosArrayList;
    ReparosAdapter reparosAdapter;
    ProgressDialog progressDialog;

    Integer kmAtual;
    ArrayList<MeusVeiculos> meusVeiculosArrayList;

    ImageView ivFoto;

    AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veiculo);

        db = FirebaseFirestore.getInstance();

        idVeiculo = getIntent().getStringExtra(Constantes.KEY_VEICULO);
        kmAtual = getIntent().getIntExtra(Constantes.KM_ATUAL,1);
        tvApelido = findViewById(R.id.tv_apelido_veiculo);
        tvModelo = findViewById(R.id.tv_modelo_veiculo);
        ivFoto = findViewById(R.id.iv_foto_veiculo);

        tvUltimaKmInformada = findViewById(R.id.tv_ultima_km_informada);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        DocumentReference reference = db.collection("veiculos").document(idVeiculo);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    tvApelido.setText(doc.getString("apelido"));
                    tvModelo.setText(doc.getString("modelo"));
                    tvUltimaKmInformada.setText(doc.get("km_atual").toString());

                    if(doc.get("tipo").equals("Moto")){
                        ivFoto.setImageResource(R.drawable.moto_default);
                    }
                    if(doc.get("tipo").equals("Carro")){
                        ivFoto.setImageResource(R.drawable.carro_default);
                    }
                    if(doc.get("tipo").equals("Camionete")){
                        ivFoto.setImageResource(R.drawable.camionete_default);
                    }
                    if(doc.get("tipo").equals("Caminhão")){
                        ivFoto.setImageResource(R.drawable.caminhao_default);
                    }

                    StorageReference fotoRef = FirebaseStorage.getInstance().getReference();
                    fotoRef.child("veiculos/"+doc.getId())
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(ivFoto);
                                }
                            });

                }
            }
        });




        //POP UPS
        btnNovoReparo = findViewById(R.id.btn_novo_reparo);
        btnNovoReparo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpNovoReparo();
            }
        });


        btnAtualizarKm = findViewById(R.id.btn_atualizar_km);
        btnAtualizarKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpAtualizarKm();
            }
        });



        //LISTA DE REPAROS
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando Veículos...");
        progressDialog.show();
        carregaListaReparos();
    }

    private void carregaListaReparos() {
        rvReparos = findViewById(R.id.rv_lista_reparos);
        rvReparos.setLayoutManager(new LinearLayoutManager(this));


        Log.d("ISO","ISO==> VEICULO KM ATUAL: "+kmAtual);

        reparosArrayList = new ArrayList<Reparos>();
        reparosAdapter = new ReparosAdapter(VeiculoActivity.this,reparosArrayList,kmAtual);
        rvReparos.setAdapter(reparosAdapter);
        carregaDB();
    }

    private void carregaDB() {
        db.collection("reparo")
                .whereEqualTo("id_veiculo",idVeiculo)
                .whereEqualTo("status","ativo")
                .orderBy("percorrido_falta", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!=null){
                            if(progressDialog.isShowing()) progressDialog.dismiss();
                            Log.d("Firestore","ISO==> Firestore: "+error.getMessage());
                            return;
                        }

                        if(value.size()==0){
                            if(progressDialog.isShowing()) progressDialog.dismiss();
                        }


                        if(value.size()>0) {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    Reparos rep = new Reparos(
                                            dc.getDocument().getId(),
                                            dc.getDocument().getString("tipo"),
                                            dc.getDocument().getString("id_veiculo"),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("km_reparo"))),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("duracao"))),
                                            dc.getDocument().getDate("data_hora"),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("percorrido"))),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("percorrido_falta")))
                                    );
                                    reparosArrayList.add(rep);

                                    reparosAdapter.notifyDataSetChanged();
                                    //reparosAdapter.notifyItemRemoved(po);
                                    if (progressDialog.isShowing()) progressDialog.dismiss();
                                }
                            }
                        }
                    }
                });
    }

    private void popUpAtualizarKm() {
        alertKmAtual = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_atualizar_km,null);
        EditText cpKmAtual = (EditText) popup.findViewById(R.id.cp_km_atual);

        Button btnCancelar = (Button) popup.findViewById(R.id.btn_cancelar);
        Button btnSalvar = (Button) popup.findViewById(R.id.btn_salvar);

        alertKmAtual.setView(popup);
        dialog = alertKmAtual.create();
        dialog.show();

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(cpKmAtual.getText().toString().equals("")){
                    AlertDialog dialogErro = new AlertDialog.Builder(VeiculoActivity.this)
                    .setTitle("ALERTA")
                            .setMessage("Você precisa preencher o campo.")
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
                    Map<String,Object> kmAtual = new HashMap<>();
                    kmAtual.put("km_atual",Integer.parseInt(cpKmAtual.getText().toString()));
                    kmAtual.put("ultima_atualizacao",FieldValue.serverTimestamp());
                    db.collection("veiculos").document(idVeiculo)
                            .update(kmAtual)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "A Quilometragem foi atualizada com sucesso.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();

                                    //LISTA OS REPAROS DO VEICULO PARA ALTERAR O CAMPO PERCORRIDO
                                    db.collection("reparo")
                                            .whereEqualTo("id_veiculo",idVeiculo)
                                            .whereEqualTo("status","ativo")
                                            .orderBy("data_hora", Query.Direction.ASCENDING)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                                    if(error!=null){
                                                        if(progressDialog.isShowing()) progressDialog.dismiss();
                                                        Log.d("Firestore","ISO==> Firestore: "+error.getMessage());
                                                        return;
                                                    }

                                                    if(value.size()==0){
                                                        Log.d("ISO","ISO==> NENHUM REPARO");
                                                    }



                                                    if(value.size()>0) {

                                                        for (DocumentChange dc : value.getDocumentChanges()) {
                                                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                                                Map<String,Object> kmPercorrido = new HashMap<>();

                                                                //ALTERA O KM PERCORRIDO DO REPARO
                                                                int perc = (Integer.parseInt(cpKmAtual.getText().toString()) - Integer.parseInt(dc.getDocument().get("km_reparo").toString()));
                                                                int percFalta = (Integer.parseInt(dc.getDocument().get("duracao").toString())-perc);
                                                                kmPercorrido.put("percorrido",perc);
                                                                kmPercorrido.put("percorrido_falta",percFalta);
                                                                db.collection("reparo")
                                                                        .document(dc.getDocument().getId())
                                                                        .update(kmPercorrido);


//                                                                Reparos rep = new Reparos(
//                                                                        dc.getDocument().getString("tipo"),
//                                                                        dc.getDocument().getString("id_veiculo"),
//                                                                        Integer.parseInt(String.valueOf(dc.getDocument().get("km_reparo"))),
//                                                                        Integer.parseInt(String.valueOf(dc.getDocument().get("duracao"))),
//                                                                        dc.getDocument().getDate("data_hora")
//                                                                );
//
//                                                                reparosArrayList.add(rep);
//
//                                                                reparosAdapter.notifyDataSetChanged();
                                                                //Log.d("ISO","ISO==> REPARO: "+dc.getDocument().getString("tipo"));
                                                                //if (progressDialog.isShowing()) progressDialog.dismiss();
                                                            }
                                                        }
                                                    }
                                                }
                                            });

                                    Intent intent = new Intent(VeiculoActivity.this,VeiculoActivity.class);
                                    intent.putExtra(Constantes.KEY_VEICULO,idVeiculo);
                                    intent.putExtra(Constantes.KM_ATUAL,Integer.parseInt(cpKmAtual.getText().toString()));
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Erro ao atualizar a Kilometragem.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }



            }
        });
    }

    private void popUpNovoReparo() {
        alertBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_novo_reparo,null);
        EditText cpTipoReparo = (EditText) popup.findViewById(R.id.cp_tipo_reparo);
        EditText cpDuracaoReparo = (EditText) popup.findViewById(R.id.cp_duracao_reparo);
        EditText cpKmAtualReparo = (EditText) popup.findViewById(R.id.cp_km_atual_reparo);

        Button btnCancelar = (Button) popup.findViewById(R.id.btn_cancelar);
        Button btnSalvar = (Button) popup.findViewById(R.id.btn_salvar);

        alertBuilder.setView(popup);
        dialog = alertBuilder.create();
        dialog.show();

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(cpTipoReparo.getText().toString().equals("") || cpDuracaoReparo.getText().toString().equals("") || cpKmAtualReparo.getText().toString().equals("")){
                    AlertDialog dialogErro = new AlertDialog.Builder(VeiculoActivity.this)
                            .setTitle("ALERTA")
                            .setMessage("Todos os campos são obrigatórios.")
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
                    //ADICIONA REPARO AO FIRESTORE
                    Map<String, Object> reparo = new HashMap<>();
                    reparo.put("tipo", cpTipoReparo.getText().toString());
                    reparo.put("km_reparo",Integer.parseInt(cpKmAtualReparo.getText().toString()));
                    reparo.put("duracao",Integer.parseInt(cpDuracaoReparo.getText().toString()));
                    reparo.put("percorrido",(kmAtual-Integer.parseInt(cpKmAtualReparo.getText().toString())));
                    reparo.put("percorrido_falta",(Integer.parseInt(cpDuracaoReparo.getText().toString())-(kmAtual-Integer.parseInt(cpKmAtualReparo.getText().toString()))));
                    reparo.put("id_veiculo",idVeiculo);
                    reparo.put("status","ativo");
                    reparo.put("data_hora", FieldValue.serverTimestamp());

                    db.collection("reparo")
                            .add(reparo)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getApplicationContext(), "Reparo registrado com sucesso.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    //reparosAdapter.notifyDataSetChanged();
                                    Log.d("ISO","ISO==> REPARO ADD OK... Redireciona...");
                                    Intent intent = new Intent(VeiculoActivity.this,VeiculoActivity.class);
                                    intent.putExtra(Constantes.KEY_VEICULO,idVeiculo);
                                    intent.putExtra(Constantes.KM_ATUAL,kmAtual);
                                    startActivity(intent);

                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Erro ao registrar o reparo.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),PerfilActivity.class);
        startActivity(intent);
    }


}