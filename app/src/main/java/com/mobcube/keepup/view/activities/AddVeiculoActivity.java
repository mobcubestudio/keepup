package com.mobcube.keepup.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobcube.keepup.R;

import java.util.HashMap;
import java.util.Map;

public class AddVeiculoActivity extends AppCompatActivity {

    Spinner spTipo;
    EditText etMarca, etModelo, etAno,etKmInicial, etApelido;
    Button btnSalvar, btnVoltar;

    FirebaseFirestore db;

    FirebaseAuth mAuth;
    FirebaseUser user;



    //ERRO
    AlertDialog.Builder dialogErro;
    TextView erro_texto, erro_entendi;
    private AlertDialog dialog;

    //CAMERA
    private ImageView fotoReceita;
    private static final int CAMERA_PER_CODE = 101;
    StorageReference storageReference;
    private Uri fotoUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_veiculo);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();

        //SPINNER
        spTipo = findViewById(R.id.sp_tipo);
        Spinner tipo = (Spinner) findViewById(R.id.sp_tipo);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddVeiculoActivity.this,
                R.layout.color_spinner_layout,getResources().getStringArray(R.array.tipo_veiculo)){

            @Override
            public boolean isEnabled(int position) {
                if(position==0){
                    return false;
                } else {
                    return true;
                }

            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view =  super.getView(position, convertView, parent);

                if(position>0){
                    ((CheckedTextView) view).setTextColor(Color.BLACK);
                }

                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position==0){
                    tv.setTextColor(Color.GRAY);
                }
                return view;

            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipo.setAdapter(adapter);


        etMarca = findViewById(R.id.et_marca);
        etModelo = findViewById(R.id.et_modelo);
        etAno = findViewById(R.id.et_ano);
        etKmInicial = findViewById(R.id.et_km_inicial);
        etApelido = findViewById(R.id.et_apelido);

        btnSalvar = findViewById(R.id.btn_salvar);

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                salvar();

            }
        });


        btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),PerfilActivity.class);
                startActivity(intent);
            }
        });


        fotoReceita = findViewById(R.id.iv_foto);
        fotoReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(AddVeiculoActivity.this)
                        .crop(1f,1f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(80, 80)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

    }

    private void salvar() {
        if(etApelido.getText().toString().toString().equals("")
                || spTipo.getSelectedItem().equals("Selecione o tipo")
                || etMarca.getText().toString().equals("")
                || etModelo.getText().toString().equals("")
                || etAno.getText().toString().equals("")
                || etKmInicial.getText().toString().equals("")){

            String erro = "";

            //if(fotoUri==null) erro = erro + "- Escolha uma foto para sua receita. \n\n";
            if(spTipo.getSelectedItem().equals("Selecione o tipo")) erro = erro + "- Escolha qual o tipo do veículo. \n\n";
            if(etApelido.getText().toString().equals("")) erro = erro + "- Você precisa dar um apelido para seu veículo. \n\n";
            if(etMarca.getText().toString().equals("")) erro = erro + "- Preencha o campo Marca. \n\n";
            if(etModelo.getText().toString().equals("")) erro = erro + "- Especifique o modelo do veículo. \n\n";
            if(etAno.getText().toString().equals("")) erro = erro + "- Preencha o campo Ano. \n\n";
            if(etKmInicial.getText().toString().equals("")) erro = erro + "- Informe a kilometragem atual de seu veículo. \n\n";


            dialogErro = new AlertDialog.Builder(this);
            View popup_erro = getLayoutInflater().inflate(R.layout.popup_erro,null);
            erro_texto = popup_erro.findViewById(R.id.erro_texto);
            erro_entendi = popup_erro.findViewById(R.id.erro_entendi);



            erro_texto.setText(erro);
            dialogErro.setView(popup_erro);

            dialog = dialogErro.create();

            dialog.show();


            erro_entendi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

        } else {
            //ADICIONA VEICULO AO FIRESTORE
            Map<String, Object> veiculo = new HashMap<>();
            veiculo.put("marca", etMarca.getText().toString());
            veiculo.put("apelido", etApelido.getText().toString());
            veiculo.put("tipo", spTipo.getSelectedItem().toString());
            veiculo.put("modelo", etModelo.getText().toString());
            veiculo.put("ano", Integer.parseInt(etAno.getText().toString()));
            veiculo.put("km_inicial",Integer.parseInt(etKmInicial.getText().toString()));
            veiculo.put("km_atual",Integer.parseInt(etKmInicial.getText().toString()));
            veiculo.put("data_hora", FieldValue.serverTimestamp());
            veiculo.put("ultima_atualizacao", FieldValue.serverTimestamp());
            veiculo.put("id_user",user.getUid());




            // Add a new document with a generated ID
            db.collection("veiculos")
                    .add(veiculo)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            if(fotoUri!=null) {
                                //ENVIA A FOTO DO VEICULO
                                uploadFotoFirebase(fotoUri, documentReference.getId());
                            }
                            Log.d("Firestore", "ISO==> DocumentSnapshot added with ID: " + documentReference.getId());
                            Intent intent = new Intent(getApplicationContext(),PerfilActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore", "ISO==> Error adding document", e);
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),PerfilActivity.class);
        startActivity(intent);
    }


    //FOTO
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fotoUri =  data.getData();
        fotoReceita.setVisibility(View.VISIBLE);
        fotoReceita.setImageURI(fotoUri);

    }

    private void uploadFotoFirebase(Uri uri, String nomeArquivo) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Enviando...");
        progressDialog.show();

        storageReference = FirebaseStorage.getInstance().getReference("veiculos/"+nomeArquivo);
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        //Toast.makeText(getApplicationContext(), "Enviada com sucesso.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Erro ao enviar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}