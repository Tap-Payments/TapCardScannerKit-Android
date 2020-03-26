package company.tap.cardscanner.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import company.tap.cardscanner.R;

public class TapCardScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_card_scanner);
        //TODO open PayCards activity scanner
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO get scanning result
        //  wrap the result in TapCard
        //  pass it to the caller by setResult()
    }
}
