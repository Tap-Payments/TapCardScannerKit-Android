package company.tap.cardscanner;

/**
 * Created by Mario Gamal on 4/1/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public interface TapTextRecognitionCallBack {
    /***
     * Provides an interface to handle success and failure.
     */
    void onRecognitionSuccess(TapCard card);
    void onRecognitionFailure(String error);
}
