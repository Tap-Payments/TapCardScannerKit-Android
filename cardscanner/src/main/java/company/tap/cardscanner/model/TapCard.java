package company.tap.cardscanner.model;

import java.io.Serializable;

/**
 * Created by Mario Gamal on 3/26/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapCard implements Serializable {

    private String cardNumber;
    private String cardHoldername;
    private String cardExpirydate;

    public TapCard(String cardNumber, String cardHoldername, String cardExpirydate) {
        this.cardNumber = cardNumber;
        this.cardHoldername = cardHoldername;
        this.cardExpirydate = cardExpirydate;
    }
    /**
     *
     * @return cardNumber
     */
    public String getCardNumber() {
        return cardNumber;
    }
    /**
     *
     * @return cardHoldername
     */
    public String getCardHolder() {
        return cardHoldername;
    }
    /**
     *
     * @return cardHoldername
     */
    public String getCardExpiryDate() {
        return cardExpirydate;
    }

}
