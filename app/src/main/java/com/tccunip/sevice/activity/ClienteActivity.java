package com.tccunip.sevice.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tccunip.sevice.R;
import com.tccunip.sevice.config.ConfiguracaoFirebase;
import com.tccunip.sevice.helper.UsuarioFirebase;
import com.tccunip.sevice.model.Destino;
import com.tccunip.sevice.model.Requisicao;
import com.tccunip.sevice.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClienteActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localCliente;
    private boolean prestadorChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;

    private EditText localDestino;
    private LinearLayout linearLayoutDestino;
    private Button btnChamarPrestador;
    private CheckBox checkLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        inicializarComponentes();

        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("cliente/id")
                .equalTo(usuarioLogado.getId());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    lista.add(ds.getValue(Requisicao.class));
                }

                Collections.reverse(lista);
                if (lista != null && lista.size()>0) {
                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO :
                            linearLayoutDestino.setVisibility(View.GONE);
                            btnChamarPrestador.setText("Cancelar");
                            prestadorChamado = true;
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();
    }

    public void opcaoLocal(View view){
        if (checkLocal.isChecked()){
            this.localDestino.setText("");
            this.localDestino.setVisibility(View.GONE);
            EditText meuLocal = findViewById(R.id.meuLocal);
            meuLocal.setVisibility(View.VISIBLE);
        }else {
            this.localDestino.setVisibility(View.VISIBLE);
            EditText meuLocal = findViewById(R.id.meuLocal);
            meuLocal.setVisibility(View.GONE);
        }
    }

    public void chamarPrestador(View view){
        if (!prestadorChamado) {
            if (checkLocal.isChecked()){

                final Destino destino = new Destino();
                destino.setLatitude(String.valueOf(localCliente.latitude));
                destino.setLongitude(String.valueOf(localCliente.longitude));

                salvarRequisicao(destino);
            }else {
                String enderecoDestino = localDestino.getText().toString();

                if(!enderecoDestino.equals("") || enderecoDestino != null){
                    Address addressDestino = recuperarEndereco(enderecoDestino);

                    if (addressDestino != null){

                        final Destino destino = new Destino();
                        destino.setCidade(addressDestino.getAdminArea());
                        destino.setCep(addressDestino.getPostalCode());
                        destino.setBairro(addressDestino.getSubLocality());
                        destino.setRua(addressDestino.getThoroughfare());
                        destino.setNumero(addressDestino.getFeatureName());
                        destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                        destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                        StringBuilder mensagemConfirmacao = new StringBuilder();
                        mensagemConfirmacao.append("Cidade: " + destino.getCidade());
                        mensagemConfirmacao.append("\nRua: " + destino.getRua());
                        mensagemConfirmacao.append("\nBairro: " + destino.getBairro());
                        mensagemConfirmacao.append("\nNúmero: " + destino.getNumero());
                        mensagemConfirmacao.append("\nCEP: " + destino.getCep());

                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Confirme o endereço:")
                                .setMessage(mensagemConfirmacao)
                                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        salvarRequisicao(destino);
                                        prestadorChamado = true;
                                    }
                                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }else {
                    Toast.makeText(this,
                            "Informe um endereço ou utilize sua localização atual !",
                            Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
            prestadorChamado = false;
        }
    }

    private void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioCliente = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioCliente.setLatitude(String.valueOf(localCliente.latitude));
        usuarioCliente.setLongitude(String.valueOf(localCliente.longitude));

        requisicao.setCliente(usuarioCliente);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        btnChamarPrestador.setText("Cancelar");


    }

    private Address recuperarEndereco(String endereco){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if (listaEnderecos != null && listaEnderecos.size() > 0){
                Address address = listaEnderecos.get(0);
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localCliente = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localCliente)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cliente))
                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localCliente, 15)
                );
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    10,
                    locationListener
            );
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Procurar um prestador");
        setSupportActionBar(toolbar);

        localDestino = findViewById(R.id.localDestino);
        linearLayoutDestino = findViewById(R.id.linearLayotDestino);
        btnChamarPrestador = findViewById(R.id.btnChamarPrestador);
        checkLocal = findViewById(R.id.checkLocal);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
