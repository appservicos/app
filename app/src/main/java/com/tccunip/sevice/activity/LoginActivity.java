package com.tccunip.sevice.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.tccunip.sevice.R;
import com.tccunip.sevice.config.ConfiguracaoFirebase;
import com.tccunip.sevice.helper.UsuarioFirebase;
import com.tccunip.sevice.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.loginEmailInput);
        campoSenha = findViewById(R.id.loginSenhaInput);
    }

    public void validarLoginUsuario(View view){
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoEmail.isEmpty()){
            if (!textoSenha.isEmpty()){
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);
            }else {
                Toast.makeText(LoginActivity.this,
                        "Preencha a senha !",
                        Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(LoginActivity.this,
                    "Preencha o e-mail !",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void logarUsuario(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);
                }else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não cadastrado !";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não são válidos !";
                    }catch (Exception e){
                        excecao = "Erro ao fazer login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,
                            excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
