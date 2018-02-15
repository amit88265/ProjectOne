package pb.com.projectone;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class GetAddressIntentService extends IntentService {


    protected ResultReceiver mReceiver;
    HashMap<String, String> map = new HashMap<>();


    public GetAddressIntentService() {
        super("GetAddessIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLng latLng = intent.getParcelableExtra(Constants.RESULT_DATA_KEY);
        mReceiver = intent.getParcelableExtra(Constants.RESULT_RECEIVER);

        List<Address> mAddress = new ArrayList<>();
        try {
            mAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mAddress == null || mAddress.size() == 0) {
            sendResult(Constants.FAILURE_RESULT, "No address found");

        } else {
            android.location.Address address = mAddress.get(0);

            map.put(Constants.CITY, address.getLocality());
            map.put(Constants.STATE, address.getAdminArea());
            map.put(Constants.COUNTRY, address.getCountryName());
            map.put(Constants.LATITUDE, String.valueOf(address.getLatitude()));
            map.put(Constants.LONGITUDE, String.valueOf(address.getLongitude()));
            map.put(Constants.POSTAL_CODE, address.getPostalCode());
            map.put(Constants.FEATURE, address.getFeatureName());
            map.put(Constants.GET_THROUGH_FARE, address.getThoroughfare());
            map.put(Constants.SUB_LOCALITY, address.getSubLocality());

            /*
             *        Log.d("vivz", "AdminArea" + address.getAdminArea() + " \nextra " + address.getExtras() + " \nfeature name" + address.getFeatureName()
             *        + "\nlocality " +
             *         address.getLocality() + " \npremises" + address.getPremises() + "\nsub admin area " + address.getSubAdminArea()
             *         + " \nsub locality" + address.getSubLocality() + " \nget through fare" + address.getThoroughfare());
            */


            String[] adr = address.getAddressLine(0).split(",");

            String str = "";
            for (String s : adr) {
                str = str + s + "\n";
            }
            sendResult(Constants.SUCCESS_RESULT, str);
        }

    }

    private void sendResult(int resultCode, String msg) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ADDESS_DETAILS, map);
        bundle.putString(Constants.SEND_RESULT, msg);
        mReceiver.send(resultCode, bundle);
    }

}
