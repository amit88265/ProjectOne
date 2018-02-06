package pb.com.projectone;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private LatLng markPosition = null;
    private Button DetailedAddress;
    String str = "";
    PlaceAutocompleteFragment autocompleteFragment;
    HashMap<String, String> hashMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting map fragment from xml to java
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Getting place auto complete fragment fragment to java
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        DetailedAddress = findViewById(R.id.send_details);


        DetailedAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra(Constants.SEND_ADDRESS_DETAILS, hashMap);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //Setting the  initial camera position
        markPosition = new LatLng(28.4968596, 77.0782323);
        CameraPosition position = CameraPosition.builder()
                .target(markPosition)
                .zoom(15)
                .bearing(30)
                .tilt(45).build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        //Setting the map on click listener
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mGoogleMap.clear();
                markPosition = latLng;
                mGoogleMap.addMarker(new MarkerOptions().position(markPosition).draggable(true));
                //starting the service to reverse geocode the location
                startIntentService(markPosition);
            }
        });

        //to display the complete address
        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView info = v.findViewById(R.id.infoWindow);
                info.setText(str);
                return v;
            }
        });


        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //need to hide as you drag marker to new position
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                //no use as of now
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                markPosition = marker.getPosition();
                startIntentService(markPosition);
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

                mGoogleMap.addMarker(new MarkerOptions().position(markPosition).draggable(true));
                //starting service to reverse geocode the searched place
                startIntentService(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
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
            if (resultCode == 0) {
                Toast.makeText(MainActivity.this, "no address found", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == 1) {
                str = resultData.getString(Constants.SEND_RESULT);
                hashMap = (HashMap<String, String>) resultData.getSerializable(Constants.ADDESS_DETAILS);

            }
        }
    }
}
