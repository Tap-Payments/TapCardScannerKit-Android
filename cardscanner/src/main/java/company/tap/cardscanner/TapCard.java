package company.tap.cardscanner;

/**
 * Created by Mario Gamal on 4/1/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapCard {

    private String cardNumber;
    private String cardHolder;
    private String expirationDate;
    /**
     * Gets cardNumber.
     *
     * @return the card Number.
     */
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Gets cardHolder.
     *
     * @return the card Holder.
     */
    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    /**
     * Gets expiration Date.
     *
     * @return the expiration Date.
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
