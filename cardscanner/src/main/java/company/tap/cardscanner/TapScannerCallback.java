package company.tap.cardscanner;

import java.io.Serializable;

public interface TapScannerCallback extends Serializable {
    /***
     * Provides an interface to handle success and failure.
     */
    void onReadSuccess(TapCard card);

    void onReadFailure(String error);
}
