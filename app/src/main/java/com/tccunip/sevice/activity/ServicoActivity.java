package com.tccunip.sevice.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tccunip.sevice.R;
import com.tccunip.sevice.config.ConfiguracaoFirebase;
import com.tccunip.sevice.model.Requisicao;
import com.tccunip.sevice.model.Usuario;

public class ServicoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnAceitarServico;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPrestador;
    private LatLng localCliente;
    private Usuario prestador;
    private Usuario cliente;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorPrestador;
    private Marker marcadorCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);

        inicializarComponentes();

        if (getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("prestador")){
            Bundle extras = getIntent().getExtras();
            prestador = (Usuario)extras.getSerializable("prestador");
            idRequisicao = extras.getString("idRequisicao");
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao(){
        final DatabaseReference requisicoes = firebaseRef.child("requisicoes").child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requisicao = dataSnapshot.getValue(Requisicao.class);

                cliente = requisicao.getCliente();
                localCliente = new LatLng(
                        Double.parseDouble(cliente.getLatitude()),
                        Double.parseDouble(cliente.getLongitude())
                );

                switch (requisicao.getStatus()){
                    case Requisicao.STATUS_AGUARDANDO:
                        requisicaoAguardando();
                        break;
                    case Requisicao.STATUS_A_CAMINHO:
                        requisicaoACaminho();
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void requisicaoAguardando(){
        btnAceitarServico.setText("Aceitar serviço");
    }

    private void requisicaoACaminho(){
        btnAceitarServico.setText("A caminho do local");

        adicionarMarcadorPrestador(localPrestador, prestador.getNome());
        adicionarMarcadorCliente(localCliente, cliente.getNome());
    }

    private void adicionarMarcadorPrestador(LatLng localizacao, String titulo){

        if (marcadorPrestador != null){
            marcadorPrestador.remove();
        }

        marcadorPrestador = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.prestador))
        );
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localizacao, 15)
        );
    }

    private void adicionarMarcadorCliente(LatLng localizacao, String titulo){

        if (marcadorCliente != null){
            marcadorCliente.remove();
        }

        marcadorCliente = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cliente))
        );
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localizacao, 15)
        );
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

    public void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPrestador = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localPrestador)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.prestador))
                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localPrestador, 15)
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
                    LocationManager.GPS_PROVIDER,
                    0,
                    10,
                    locationListener
            );
        }

    }

    public void aceitarServico(View view){
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setPrestador(prestador);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();

    }

    private void inicializarComponentes(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Local do serviço");

        btnAceitarServico = findViewById(R.id.btnAceitarServico);

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}
