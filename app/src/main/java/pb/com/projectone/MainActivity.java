package pb.com.projectone;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 11;
    private GoogleMap mGoogleMap;
    private LatLng markPosition = null;
    String str = "";
    PlaceAutocompleteFragment autocompleteFragment;
    HashMap<String, String> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isOnline()){
            InternetConnectionDialog();
        }

        //Getting map fragment from xml to java
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Getting place auto complete fragment fragment to java
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        ImageView mCustomMarker = findViewById(R.id.custom_marker);
        mCustomMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTheAddressDialog();
            }
        });


    }

public boolean isOnline(){
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //Setting the  initial camera position
        markPosition = new LatLng(28.4968596, 77.0782323);
        startIntentService(markPosition);
        CameraPosition position = CameraPosition.builder()
                .target(markPosition)
                .zoom(15)
                .bearing(30)
                .tilt(45).build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));


        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                startIntentService(mGoogleMap.getCameraPosition().target);
            }
        });

        //Setting the listener to the fragment auto complete fragment
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                markPosition = place.getLatLng();
                //Building the camera position
                CameraPosition position = CameraPosition.builder()
                        .target(place.getLatLng())
                        .zoom(15)
                        .bearing(30)
                        .tilt(45).build();

                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000, null);

                startIntentService(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTheAddressDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(str)
                .setTitle("YOUR ADDRESS")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                        intent.putExtra(Constants.SEND_ADDRESS_DETAILS, hashMap);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //do nothing
                    }
                }).show();
    }

    private void InternetConnectionDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Please check your internet connection and try again")
                .setTitle("NO INTERNET CONNECTION")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
               .show();
    }

    //function to start service
    public void startIntentService(LatLng latLng) {
        Intent intent = new Intent(MainActivity.this, GetAddressIntentService.class);
        intent.putExtra(Constants.RESULT_DATA_KEY, latLng);
        //initiating the object to get the result
        AddressReceiver mResultReceiver = new AddressReceiver(new Handler());
        intent.putExtra(Constants.RESULT_RECEIVER, mResultReceiver);
        startService(intent);
    }


    //class to get the result from service
    class AddressReceiver extends ResultReceiver {

        public AddressReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode ==0) {
                str = resultData.getString(Constants.SEND_RESULT);
            }
            if (resultCode == 1) {
                str = resultData.getString(Constants.SEND_RESULT);
                hashMap = (HashMap<String, String>) resultData.getSerializable(Constants.ADDESS_DETAILS);

            }
        }
    }
}
