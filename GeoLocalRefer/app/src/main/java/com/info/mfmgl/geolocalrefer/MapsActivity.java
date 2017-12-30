package com.info.mfmgl.geolocalrefer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.security.Provider;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener{

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 1;

    private DecimalFormat df;
    private AlertDialog.Builder myBuild;
    private AlertDialog dialog;

    private String longitud, latitud, direccion;

    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //onCreate() configura el archivo de diseño como la vista de contenido
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);//definimos layout con el mapa
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // declaramos el mapa, buscamos el fragmento con el id
        //Obtén un controlador para el fragmento de mapa llamando a FragmentManager.findFragmentById()
        mapFragment.getMapAsync(this); // para registrar el callback del mapa

        initializerDialog();
        initializerService();
        initializerUbication();
    }

    private void initializerDialog() {
        df = new DecimalFormat("#.##########");
        myBuild = new AlertDialog.Builder(MapsActivity.this);
        myBuild.setTitle("Detalles del Marcador");
        myBuild.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void initializerService(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(c,true);
        locationManager.requestLocationUpdates(provider, 10000, 1, this);
    }

    void initializerUbication(){
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(provider);
        if(location == null){
            latitud = df.format(-8.10961274f);
            longitud = df.format(-79.02835584f);
        }else{
            latitud = df.format(location.getLatitude());
            longitud = df.format(location.getLongitude());
        }

        initializerDirection(location);
    }

    public void initializerDirection(Location location) {
        if(location != null){
            if (location.getLatitude()!=0.0 && location.getLongitude() != 0.0){
                try{
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!list.isEmpty()){
                        Address direction = list.get(0);
                        direccion = direction.getAddressLine(0);
                    }
                }catch (IOException ie){
                    direccion = "desconocido";
                }
            }
        }
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
        // Controles UI
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng latLng = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
        //latitud = df.format(-8.10961274f);
        //longitud = df.format(-79.02835584f);
        MarkerOptions markerOptions = new MarkerOptions()
                .draggable(true)
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

        Marker marker = mMap.addMarker(markerOptions);
        /*mMap.addMarker(new MarkerOptions()
                .draggable(true)
                // .anchor(0.0f, 1.0f)
                .position(latLng)
        );*/

        // mMap.moveCamera(CameraUpdateFactory.newLatLng(trujillo));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // mMap.setMyLocationEnabled(true);
        }
        //Listener
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            // ¿Permisos asignados?
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    return;
                }
            } else {
                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // TODO Auto-generated method stub
        // Here your code
        System.out.println("Dragging Start");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // TODO Auto-generated method stub
        // Here your code
        System.out.println("Dragging");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //DecimalFormat df = new DecimalFormat("#.####");
        System.out.println("Dragging End");
        latitud = df.format(marker.getPosition().latitude);
        longitud = df.format(marker.getPosition().longitude);
        try{
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            if (!list.isEmpty()){
                Address direction = list.get(0);
                direccion = direction.getAddressLine(0);
            }
        }catch (IOException ie){
            direccion = "desconocido";
        }
        System.out.println(latitud);
        System.out.println(longitud);
        System.out.println(direccion);
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("Haciendo click");
        myBuild.setMessage("Latitud: " + latitud + " \nLongitud: " + longitud + " \nDirección: " + direccion);
        System.out.println("Llega");
        System.out.println(latitud);
        System.out.println(longitud);
        System.out.println(direccion);
        dialog = myBuild.create();
        dialog.show();
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
