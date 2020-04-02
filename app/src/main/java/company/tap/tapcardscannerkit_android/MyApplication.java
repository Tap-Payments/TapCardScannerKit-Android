package company.tap.tapcardscannerkit_android;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryPerformance;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withDataSaleOptOut(false) //CCPA - the default value is false
                .withCaptureUncaughtExceptions(true)
                .withIncludeBackgroundSessionsInMetrics(true)
                .withLogLevel(Log.VERBOSE)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .build(this, "87SBKCH7W7MD6S7P4DNQ");
    }
}