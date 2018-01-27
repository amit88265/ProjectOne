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
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private LatLng markPosition = null;
    String str = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting map fragment from xml to java
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Getting place auto complete fragment fragment to java
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        //Setting the listener to the fragment auto complete fragment
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                //Building the camera position
                CameraPosition position = CameraPosition.builder()
                        .target(place.getLatLng())
                        .zoom(15)
                        .bearing(30)
                        .tilt(45).build();

                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000, null);

                //Adding the marker
                mGoogleMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                //starting service to reverse geocode the searched place
                startIntentService(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //Setting the  initial camera position
        LatLng delhi = new LatLng(28.4968596, 77.0782323);
        CameraPosition position = CameraPosition.builder()
                .target(delhi)
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

                mGoogleMap.addMarker(new MarkerOptions().position(markPosition));
                str = resultData.getString(Constants.SEND_RESULT);
            }
        }
    }
}
