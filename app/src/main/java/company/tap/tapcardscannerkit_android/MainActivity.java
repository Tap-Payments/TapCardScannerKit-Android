package company.tap.tapcardscannerkit_android;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import cards.pay.paycardsrecognizer.sdk.Card;
import cards.pay.paycardsrecognizer.sdk.FrameManager;
import cards.pay.paycardsrecognizer.sdk.ScanCardIntent;
import cards.pay.paycardsrecognizer.sdk.ui.InlineViewCallback;
import cards.pay.paycardsrecognizer.sdk.ui.InlineViewFragment;
import company.tap.cardscanner.AnalyticsHelper;
import company.tap.cardscanner.BuildConfig;
import company.tap.cardscanner.CameraActivity;
import company.tap.cardscanner.CameraFragment;
import company.tap.cardscanner.TapCard;
import company.tap.cardscanner.TapCountDownTimer;
import company.tap.cardscanner.TapScannerCallback;
import company.tap.cardscanner.TapTextRecognitionCallBack;
import company.tap.cardscanner.TapTextRecognitionML;

public class MainActivity extends AppCompatActivity implements TapTextRecognitionCallBack, InlineViewCallback ,TapScannerCallback , Serializable {

    private EditText cardNumber, cardHolder, expirationDate;
    private boolean isInlineOpened = false;
    private boolean isInlineCameraOpened = false;
    private static final int SCAN_CARD_ID = 101;
    private static final int PICK_IMAGE_ID = 102;
    private TapTextRecognitionML textRecognitionML;
    private TapScannerCallback tapScannerCallback;
    private LinearLayout cardLayout;
    private Button btnFullscreen, btnInline,btnImagedecoder ,btncustomCamerar;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        checkAndroidVersion();


        // TODO: 4/21/20 reuse same instance if exist instead of instantiating one each time configuration changes
        textRecognitionML = new TapTextRecognitionML(this);
        textRecognitionML.addTapScannerCallback(this);
        textRecognitionML.setFrameColor(Color.BLUE);

    }


    private void initializeViews() {
        cardHolder = findViewById(R.id.card_holder);
        cardNumber = findViewById(R.id.card_number);
        expirationDate = findViewById(R.id.expiration_date);
        cardLayout = findViewById(R.id.card_Layout);
        btnFullscreen = findViewById(R.id.btn_fullscanner);
        btnInline = findViewById(R.id.btn_inlinescanner);
        btnImagedecoder = findViewById(R.id.btn_imagescanner);
        btncustomCamerar = findViewById(R.id.btn_imageCustom);

    }

    /***
     *
     * Open the scanner in fullView
     */
    public void openFullScreenScanner(View view) {
        removeInlineScanner();
        Map<String, String> parameters = new HashMap<String, String>();
       // parameters.put("sdk", BuildConfig.VERSION_NAME);
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_FULLSCREEN_CALLED, parameters, true);
        Intent intent = new ScanCardIntent.Builder(this).build();
        startActivityForResult(intent, SCAN_CARD_ID);
        setTapCountDownTimer();
    }

    /***
     *
     * Open the scanner in InlineView
     */
    public void openInlineScanner(View view) {
        Map<String, String> parameters = new HashMap<String, String>();
       // parameters.put("sdk", BuildConfig.VERSION_NAME);
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_INLINE_CALLED, parameters, true);
        setTapCountDownTimer();
        FrameManager.getInstance().setFrameColor(Color.YELLOW);
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


       Intent chooseImageIntent = new Intent(this, CameraActivity.class);
        startActivity(chooseImageIntent);
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
                        btnImagedecoder.setVisibility(View.GONE);
                        btnFullscreen.setVisibility(View.GONE);
                        btnInline.setVisibility(View.GONE);
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
        btnImagedecoder.setVisibility(View.GONE);
        btnFullscreen.setVisibility(View.GONE);
        btnInline.setVisibility(View.GONE);
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
            btnImagedecoder.setVisibility(View.GONE);
            btnFullscreen.setVisibility(View.GONE);
            btnInline.setVisibility(View.GONE);
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
            btnImagedecoder.setVisibility(View.VISIBLE);
            btnFullscreen.setVisibility(View.VISIBLE);
            btnInline.setVisibility(View.VISIBLE);
            btncustomCamerar.setVisibility(View.VISIBLE);

        }
    }

    private void setTapCountDownTimer() {
        final TapCountDownTimer counter = new TapCountDownTimer(this);
        counter.setTimer(15000, 1000);
        counter.start(() -> {
            Toast.makeText(MainActivity.this, "Timed out", Toast.LENGTH_SHORT).show();
            removeInlineScanner();
            finishActivity(SCAN_CARD_ID);
        });
    }

    @Override
    public void onBackPressed() {
        cardLayout.setVisibility(View.GONE);
        btnImagedecoder.setVisibility(View.VISIBLE);
        btnFullscreen.setVisibility(View.VISIBLE);
        btncustomCamerar.setVisibility(View.VISIBLE);
        btnInline.setVisibility(View.VISIBLE);
    }


    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }


    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int wtite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("in fragment on request", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    @Override
    public void onReadSuccess(TapCard card) {
        removeInlineCameraScanner();
        cardNumber.setText(card.getCardNumber());
        cardHolder.setText(card.getCardHolder());
        expirationDate.setText(card.getExpirationDate());
        cardLayout.setVisibility(View.VISIBLE);
        btnImagedecoder.setVisibility(View.GONE);
        btncustomCamerar.setVisibility(View.GONE);

        btnFullscreen.setVisibility(View.GONE);

        btnInline.setVisibility(View.GONE);
    }

    @Override
    public void onReadFailure(String error) {

    }

    public void openInhouseCameraFragment(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.inline_container, new CameraFragment())
                .commit();
        isInlineCameraOpened = true;
    }

    private void removeInlineCameraScanner() {
        if (isInlineCameraOpened) {
            if (getSupportFragmentManager().findFragmentById(R.id.inline_container) != null)
                getSupportFragmentManager().beginTransaction().
                        remove(getSupportFragmentManager().findFragmentById(R.id.inline_container))
                        .commit();
            isInlineCameraOpened = false;
            cardLayout.setVisibility(View.GONE);
            btnImagedecoder.setVisibility(View.VISIBLE);
            btnFullscreen.setVisibility(View.VISIBLE);
            btnInline.setVisibility(View.VISIBLE);
            btncustomCamerar.setVisibility(View.VISIBLE);

        }
    }
}

