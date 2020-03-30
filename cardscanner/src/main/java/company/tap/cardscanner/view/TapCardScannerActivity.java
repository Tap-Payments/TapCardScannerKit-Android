package company.tap.cardscanner.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

import cards.pay.paycardsrecognizer.sdk.Card;
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent;
import company.tap.cardscanner.R;
import company.tap.cardscanner.model.TapCard;

public class TapCardScannerActivity extends AppCompatActivity {

    static final int REQUEST_CODE_SCAN_CARD = 1;
    private static final String TAG = "TapCardScannerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_card_scanner);
        //TODO open PayCards activity scanner
        Intent intent = new ScanCardIntent.Builder(this).build();
        startActivityForResult(intent, REQUEST_CODE_SCAN_CARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO get scanning result
        //  wrap the result in TapCard
        //  pass it to the caller by setResult()

        if (requestCode == REQUEST_CODE_SCAN_CARD) {
            if (resultCode == Activity.RESULT_OK) {
                Card card = null;
                if (data != null) {
                    card = data.getParcelableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);
                }
                String cardData = "Card number: " + card.getCardNumberRedacted() + "\n"
                        + "Card holder: " + card.getCardHolderName() + "\n"
                        + "Card expiration date: " + card.getExpirationDate();
                Log.i(TAG, "Card info: " + cardData);
                TapCard tapCard = new TapCard(card.getCardNumber(),card.getCardHolderName(),card.getExpirationDate());

                finishActivityWithResultCodeOK(tapCard);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "Scan canceled");
            } else {
                Log.i(TAG, "Scan failed");
            }
        }
    }

    private void finishActivityWithResultCodeOK(TapCard tapCard) {
        setResult(RESULT_OK,new Intent().putExtra("tapcard", tapCard));
        finish();
    }
}
