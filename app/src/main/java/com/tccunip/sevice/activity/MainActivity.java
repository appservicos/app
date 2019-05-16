package com.tccunip.sevice.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.tccunip.sevice.R;
import com.tccunip.sevice.config.ConfiguracaoFirebase;
import com.tccunip.sevice.helper.Permissoes;
import com.tccunip.sevice.helper.UsuarioFirebase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Esconde o cabeçalho padrão
        getSupportActionBar().hide();

        Permissoes.validarPermissoes(permissoes, this, 1);

        //Para testes, força o usuario a deslogar
        /*autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.signOut();*/
    }

    //Métodos para direcionar a aplicação entre as telas do App
    public void abrirTelaCadastro(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }
    public void abrirTelaLogin(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }

    //Método para que o usuário permaneca logado mesmo fechando o App
    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionaUsuarioLogado(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermicao();
            }
        }
    }

    private void alertaValidacaoPermicao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o Sevice é necessário aceitar as permissões !");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
