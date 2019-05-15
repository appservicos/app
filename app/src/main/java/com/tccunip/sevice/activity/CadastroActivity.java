package com.tccunip.sevice.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.tccunip.sevice.R;
import com.tccunip.sevice.config.ConfiguracaoFirebase;
import com.tccunip.sevice.helper.UsuarioFirebase;
import com.tccunip.sevice.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    //Variáveis
    private TextInputEditText campoNome, campoEmail, campoSenha;
    private Switch swithClientePestador;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Inicializa as variáveis com os componentes na activity_cadastro.xml
        campoNome = findViewById(R.id.cadastroNomeInput);
        campoEmail = findViewById(R.id.cadastroEmailInput);
        campoSenha = findViewById(R.id.cadastroSenhaInput);
        swithClientePestador = findViewById(R.id.swithClientePrestador);
    }

    //Verifica se o usuário preencheu todos os campos antes de continuar
    public void validarCadastro(View view){

        //Passagem para variável String para usar o médoto isEmpty()
        String nomeDigitado = campoNome.getText().toString();
        String emailDigitado = campoEmail.getText().toString();
        String senhaDigitada = campoSenha.getText().toString();

        //Verifica se o usuário preencheu todos os campos antes de continuar
        if (!nomeDigitado.isEmpty()){
            if (!emailDigitado.isEmpty()){
                if (!senhaDigitada.isEmpty()){

                    //Atribui os dados ao objeto
                    Usuario usuario = new Usuario();
                    usuario.setNome(nomeDigitado);
                    usuario.setEmail(emailDigitado);
                    usuario.setSenha(senhaDigitada);
                    usuario.setTipo(verificaSwitchClientePrestador()); //Metodo para definir o valor da switch

                    cadastrarUsuario(usuario);

                }else {
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o senha !",
                            Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(CadastroActivity.this,
                        "Preencha o E-mail !",
                        Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(CadastroActivity.this,
                    "Preencha o nome !",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //Método para definir o valor da switch
    public String verificaSwitchClientePrestador(){
        return swithClientePestador.isChecked() ? "Prestador" : "Cliente"; //"Operador ternário"
    }

    //Método para deixar a seleção do switch em negrito
    public void selecaoSwitchClientePrestador (View view){
        if(swithClientePestador.isChecked()){
            TextView prestador = findViewById(R.id.textoPrestador);
            prestador.setTypeface(Typeface.DEFAULT_BOLD);
            TextView cliente = findViewById(R.id.textoCliente);
            cliente.setTypeface(Typeface.DEFAULT);

        }else{
            TextView cliente = findViewById(R.id.textoCliente);
            cliente.setTypeface(Typeface.DEFAULT_BOLD);
            TextView prestador = findViewById(R.id.textoPrestador);
            prestador.setTypeface(Typeface.DEFAULT);
        }

    }

    //Método para cadastrar o usuário
    public void cadastrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        if(usuario.getTipo() == "Cliente"){
                            startActivity(new Intent(CadastroActivity.this, ClienteActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this,
                                    "Cliente cadastrado com sucesso !",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this,
                                    "Prestador cadastrado com sucesso !",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte !";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um e-mail válido !";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Conta já cadastrada !";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar o usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,
                            excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
