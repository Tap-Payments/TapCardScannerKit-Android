package company.tap.tapcardscannerkit_android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import company.tap.cardscanner.view.TapCardScannerFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openFullScreenScanner(View view) {
        //TODO open TapCardScannerActivity
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
