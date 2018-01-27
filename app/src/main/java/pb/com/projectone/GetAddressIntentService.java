package pb.com.projectone;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GetAddressIntentService extends IntentService {


    protected ResultReceiver mReceiver;

    public GetAddressIntentService() {
        super("GetAddessIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String error = "";
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
        bundle.putString(Constants.SEND_RESULT, msg);
        mReceiver.send(resultCode, bundle);
    }

}
