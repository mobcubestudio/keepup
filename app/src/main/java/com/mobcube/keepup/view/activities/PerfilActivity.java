package com.mobcube.keepup.view.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobcube.keepup.R;
import com.mobcube.keepup.helpers.Constantes;
import com.mobcube.keepup.model.MeusVeiculos;
import com.mobcube.keepup.view.adapters.MeusVeiculosAdapter;

import java.util.ArrayList;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNome, tvEmail;
    Button btnSair, btnAddVeiculo;
    FirebaseAuth mAuth;
    FirebaseUser user;

    //LISTA
    RecyclerView recyclerView;
    ArrayList<MeusVeiculos> meusVeiculosArrayList;
    MeusVeiculosAdapter meusVeiculosAdapter;
    FirebaseFirestore db;


    ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null){
            Log.d("A","ISO==> FIREBASE AUTH: NAO LOGADO");
            //FirebaseCrashlytics.getInstance().log("USUARIO FIREBASE => NULL");
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            //intent.putExtra(AppCons.KEY_REDIRECT_LOGIN,"favoritos");
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando Veículos...");
        progressDialog.show();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvNome = findViewById(R.id.tv_nome);
        tvEmail = findViewById(R.id.tv_email);



        user = mAuth.getCurrentUser();



        //GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(user!=null){
            tvNome.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        }


        //BOTOES
        btnSair = findViewById(R.id.btn_sair);
        btnAddVeiculo = findViewById(R.id.btn_add_veiculo);

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        btnAddVeiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AddVeiculoActivity.class);
                startActivity(intent);
            }
        });


        if(user!=null) {
            Log.d("ISO","ISO==> LOGADO!!!!");
            carregaLista();
        }

    }

    private void carregaLista() {
        recyclerView = findViewById(R.id.rv_meus_veiculos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        meusVeiculosArrayList = new ArrayList<MeusVeiculos>();
        meusVeiculosAdapter = new MeusVeiculosAdapter(PerfilActivity.this,meusVeiculosArrayList);

        meusVeiculosAdapter.setOnItemClickListener(data -> {
            Intent intent = new Intent(getApplicationContext(),VeiculoActivity.class);
            intent.putExtra(Constantes.KEY_VEICULO,data.getId_veiculo());
            intent.putExtra(Constantes.KM_ATUAL,data.getKm_atual());
            startActivity(intent);
        });


        recyclerView.setAdapter(meusVeiculosAdapter);



        carregaDB();

    }

    private void carregaDB() {
        db.collection("veiculos")
                .whereEqualTo("id_user",user.getUid())
                .orderBy("ano", Query.Direction.ASCENDING)
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
                                    MeusVeiculos mv = new MeusVeiculos(
                                            dc.getDocument().getId(),
                                            dc.getDocument().getString("marca"),
                                            dc.getDocument().getString("modelo"),
                                            dc.getDocument().getString("apelido"),
                                            dc.getDocument().getString("tipo"),
                                            dc.getDocument().getString("id_user"),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("ano"))),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("km_inicial"))),
                                            Integer.parseInt(String.valueOf(dc.getDocument().get("km_atual")))
                                    );

                                    meusVeiculosArrayList.add(mv);

                                    meusVeiculosAdapter.notifyDataSetChanged();
                                    if (progressDialog.isShowing()) progressDialog.dismiss();
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}