package company.tap.cardscanner;

import android.app.Activity;
import android.content.Context;

/**
 * Created by AhlaamK on 4/26/20.
 * <p>
 * Copyright (c) 2020    Tap Payments.
 * All rights reserved.
 **/
public class TapCountDownTimer extends Thread {
    private long milliSeconds, timeInterval;
    private OnCounterFinishedListener mCounterFinishedListener;
    private final Thread t;
    private final Activity activity;


    public TapCountDownTimer(Context context) {
        t = new Thread(this);
        activity = (Activity) context;
    }

    @Override
    public void run() {
        try {
            sleep(milliSeconds);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCounterFinishedListener.onCounterFinished();
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTimer(long milliSeconds, long timeInterval) {
        this.milliSeconds = milliSeconds;
        this.timeInterval = timeInterval;

    }

    public void start(OnCounterFinishedListener listener) {
        mCounterFinishedListener = listener;
        t.start();
    }

    public interface OnCounterFinishedListener {
        void onCounterFinished();
    }
}

