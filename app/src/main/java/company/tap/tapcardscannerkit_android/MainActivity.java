package company.tap.tapcardscannerkit_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import cards.pay.paycardsrecognizer.sdk.Card;
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent;
import cards.pay.paycardsrecognizer.sdk.ui.InlineViewCallback;
import cards.pay.paycardsrecognizer.sdk.ui.InlineViewFragment;
import company.tap.cardscanner.AnalyticsHelper;
import company.tap.cardscanner.BuildConfig;
import company.tap.cardscanner.TapCard;
import company.tap.cardscanner.TapCountDownTimer;
import company.tap.cardscanner.TapTextRecognitionCallBack;
import company.tap.cardscanner.TapTextRecognitionML;

public class MainActivity extends AppCompatActivity implements TapTextRecognitionCallBack, InlineViewCallback {

    private EditText cardNumber, cardHolder, expirationDate;
    private boolean isInlineOpened = false;
    private static final int SCAN_CARD_ID = 101;
    private static final int PICK_IMAGE_ID = 102;
    private TapTextRecognitionML textRecognitionML;
    private LinearLayout cardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardHolder = findViewById(R.id.card_holder);
        cardNumber = findViewById(R.id.card_number);
        expirationDate = findViewById(R.id.expiration_date);
        cardLayout = findViewById(R.id.card_Layout);
        // TODO: 4/21/20 reuse same instance if exist instead of instantiating one each time configuration changes
        textRecognitionML = new TapTextRecognitionML(this);
    }

    /***
     *
     * Open the scanner in fullView
     */
    public void openFullScreenScanner(View view) {
        removeInlineScanner();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("sdk", BuildConfig.VERSION_NAME);
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_FULLSCREEN_CALLED, parameters, true);
        Intent intent = new ScanCardIntent.Builder(this).build();
        startActivityForResult(intent, SCAN_CARD_ID);
    }

    /***
     *
     * Open the scanner in InlineView
     */
    public void openInlineScanner(View view) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("sdk", BuildConfig.VERSION_NAME);
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_INLINE_CALLED, parameters, true);
        setTapCountDownTimer();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.inline_container, new InlineViewFragment())
                .commit();
        isInlineOpened = true;
    }

    /***
     *
     * Pick the Image from the gallery
     */
    public void openImagePicker(View view) {
        removeInlineScanner();
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCAN_CARD_ID:
                if (resultCode == Activity.RESULT_OK) {
                    Card card = data.getParcelableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);
                    if (card != null) {
                        cardNumber.setText(card.getCardNumber());
                        cardHolder.setText(card.getCardHolderName());
                        expirationDate.setText(card.getExpirationDate());
                        cardLayout.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                if(bitmap!=null)
                textRecognitionML.decodeImage(bitmap);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRecognitionSuccess(TapCard card) {
        cardNumber.setText(card.getCardNumber());
        cardHolder.setText(card.getCardHolder());
        expirationDate.setText(card.getExpirationDate());
        cardLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecognitionFailure(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanCardFailed(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanCardFinished(Card card, byte[] cardImage) {
        removeInlineScanner();
        if (card != null) {
            cardNumber.setText(card.getCardNumber());
            cardHolder.setText(card.getCardHolderName());
            expirationDate.setText(card.getExpirationDate());
            cardLayout.setVisibility(View.VISIBLE);
        }
    }

    private void removeInlineScanner() {
        if (isInlineOpened) {
            if (getSupportFragmentManager().findFragmentById(R.id.inline_container) != null)
                getSupportFragmentManager().beginTransaction().
                        remove(getSupportFragmentManager().findFragmentById(R.id.inline_container))
                        .commit();
            isInlineOpened = false;
            cardLayout.setVisibility(View.GONE);
        }
    }

    private void setTapCountDownTimer() {
        final TapCountDownTimer counter = new TapCountDownTimer(this);
        counter.setTimer(10000, 1000);
        counter.start(() -> {
            Toast.makeText(MainActivity.this, "Timed out", Toast.LENGTH_SHORT).show();
            removeInlineScanner();

        });
    }

}
