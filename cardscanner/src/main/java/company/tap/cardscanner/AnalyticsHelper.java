package company.tap.cardscanner;

//import com.flurry.android.FlurryAgent;

import java.util.Map;

/**
 * Created by AhlaamK on 4/7/20.
 * <p>
 * Copyright (c) 2020    Tap Payments.
 * All rights reserved.
 **/
public class AnalyticsHelper {
    public static final String EVENT_DECODE_IMAGE = "decode_image";
    public static final String EVENT_INLINE_CALLED = "inline_called";
    public static final String EVENT_FULLSCREEN_CALLED = "fullscreen_called";
    public static final String EVENT_CAN_SCAN_CALLED = "can_scan_called";

    /**
     * Logs an event for analytics.
     *
     * @param eventName   name of the event
     * @param eventParams event parameters (can be null)
     * @param timed       <code>true</code> if the event should be timed, false otherwise
     */
    public static void logEvent(String eventName, Map<String, String> eventParams, boolean timed) {
      //  FlurryAgent.logEvent(eventName, eventParams, timed);
    }

    /**
     * Logs an error.
     *
     * @param errorId          error ID
     * @param errorDescription error description
     * @param throwable        a {@link Throwable} that describes the error
     */
    public static void logError(String errorId, String errorDescription, Throwable throwable) {
       // FlurryAgent.onError(errorId, errorDescription, throwable);
    }

}
