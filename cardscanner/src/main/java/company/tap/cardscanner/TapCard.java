package company.tap.cardscanner;

import java.io.Serializable;

/**
 * Created by Mario Gamal on 4/1/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapCard implements Serializable {

    private String cardNumber;
    private String cardHolder;
    private String expirationDate;

 /*   protected TapCard(Parcel in) {
        cardNumber = in.readString();
        cardHolder = in.readString();
        expirationDate = in.readString();
    }

    public static final Creator<TapCard> CREATOR = new Creator<TapCard>() {
        @Override
        public TapCard createFromParcel(Parcel in) {
            return new TapCard(in);
        }

        @Override
        public TapCard[] newArray(int size) {
            return new TapCard[size];
        }
    };*/

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

  /*  @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.cardNumber);
        parcel.writeString(this.cardNumber);
        parcel.writeString(this.expirationDate);
    }*/
}
