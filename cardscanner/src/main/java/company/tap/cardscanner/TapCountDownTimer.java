package company.tap.cardscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import java.net.URISyntaxException;


/**
 * Created by AhlaamK on 4/2/20.
 * <p>
 * Copyright (c) 2020    Tap Payments.
 * All rights reserved.
 **/
public class TapCountDownTimer extends CountDownTimer {
   private  Activity context;
    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public TapCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
      //  this.context = context;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
       // Toast.makeText(context, "Timed out", Toast.LENGTH_SHORT).show();
        //    context.setResult(Activity.RESULT_CANCELED);
    }

}
