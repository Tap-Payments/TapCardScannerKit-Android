package company.tap.cardscanner;

import android.app.Application;
import android.util.Log;



public class TapCardScannerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

       /* new FlurryAgent.Builder()
                .withDataSaleOptOut(false) //CCPA - the default value is false
                .withCaptureUncaughtExceptions(true)
                .withIncludeBackgroundSessionsInMetrics(true)
                .withLogLevel(Log.VERBOSE)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .build(this, BuildConfig.FLURRY_API_KEY);*/
    }
}