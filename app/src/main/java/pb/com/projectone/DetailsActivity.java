package pb.com.projectone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.HashMap;

public class DetailsActivity extends AppCompatActivity {

    HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        TextView mDetailsOfAddress = findViewById(R.id.address_details);

        Intent intent = getIntent();
        map = (HashMap<String, String>) intent.getSerializableExtra(Constants.SEND_ADDRESS_DETAILS);
        mDetailsOfAddress.setText("City :" + map.get(Constants.CITY) +
                "\nState :" + map.get(Constants.STATE) +
                "\nCountry :" + map.get(Constants.COUNTRY) +
                "\nPostal Code :" + map.get(Constants.POSTAL_CODE) +
                "\nLatitude :" + map.get(Constants.LATITUDE) +
                "\nLongitude :" + map.get(Constants.LONGITUDE));


    }
}
