package company.tap.tapcardscannerkit_android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import company.tap.cardscanner.view.TapCardScannerActivity;
import company.tap.cardscanner.view.TapCardScannerFragment;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CODE_SCAN_CARD = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openFullScreenScanner(View view) {
        //TODO open TapCardScannerActivity
        Intent intent = new Intent(this,TapCardScannerActivity.class);
        startActivityForResult(intent,REQUEST_CODE_SCAN_CARD);
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
        //TODO get parcelable TapCard or error message
    }
}
