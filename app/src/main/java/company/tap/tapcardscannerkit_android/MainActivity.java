package company.tap.tapcardscannerkit_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import company.tap.cardscanner.model.TapCard;
import company.tap.cardscanner.view.TapCardScannerActivity;
import company.tap.cardscanner.view.TapCardScannerFragment;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CODE_TAP_SCAN_CARD = 1;
    private static final String TAG = "MainActivity";
    TextView scannedresultTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannedresultTextview = findViewById(R.id.scannedresult_Textview);
    }

    public void openFullScreenScanner(View view) {
        Intent intent = new Intent(this, TapCardScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TAP_SCAN_CARD);
    }

    public void openOnlineScanner(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.inline_container, new TapCardScannerFragment())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAP_SCAN_CARD) {
            if (resultCode == Activity.RESULT_OK) {
                TapCard card = null;
                if (data != null) {
                    card = (TapCard) data.getSerializableExtra("tapcard");
                }
                String cardData = "Card number: " + card.getCardNumber() + "\n"
                        + "Card holder: " + card.getCardHolder() + "\n"
                        + "Card expiration date: " + card.getCardExpiryDate();
                Log.i(TAG, "Card info in main: " + cardData);
                scannedresultTextview.setText(cardData);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "Scan canceled");
                if(data!=null){
                    String resultcancel = data.getStringExtra("error");
                    Toast.makeText(this, resultcancel, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i(TAG, "Scan failed");
            }
        }
    }
}
