package company.tap.cardscanner.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Mario Gamal on 3/26/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapCard implements  Serializable {

    private String cardNumber;
    private String cardHoldername;
    private String cardExpiryDate;

    public TapCard(String cardNumber, String cardHolder, String cardExpiryDate) {
        this.cardNumber = cardNumber;
        this.cardHoldername = cardHolder;
        this.cardExpiryDate = cardExpiryDate;
    }


    protected TapCard(Parcel in) {
        cardNumber = in.readString();
        cardHoldername = in.readString();
        cardExpiryDate = in.readString();
    }



    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardHolder() {
        return cardHoldername;
    }

    public String getCardExpiryDate() {
        return cardExpiryDate;
    }

}
