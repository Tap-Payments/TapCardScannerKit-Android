package company.tap.cardscanner;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Mario Gamal on 4/1/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapTextRecognitionML {
    /***
     * TapTextRecognitionML uses FirebaseML kit
     * Text recognition can automate reading of credit cards, you can also extract text from pictures of documents,
     * which you can use to increase accessibility of documents.
     *
     */
    private final TapTextRecognitionCallBack textRecognitionCallBack;
    private static TapScannerCallback _tapScannerCallback;
    private static int frameColor = Color .WHITE;
    TapCard card = new TapCard();
    public static final String NEW_LINE = System.getProperty("line.separator");

    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    StringBuffer text =new StringBuffer();
    public TapTextRecognitionML(TapTextRecognitionCallBack textRecognitionCallBack) {
        this.textRecognitionCallBack = textRecognitionCallBack;

    }


    public void decodeImage(Bitmap imageBitmap) {
        // Capture image & SDK version
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("bitmap", String.valueOf(imageBitmap));
     //   parameters.put("sdk", BuildConfig.VERSION_NAME);
     //   parameters.put("appId", BuildConfig.LIBRARY_PACKAGE_NAME);
      //  AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_DECODE_IMAGE, parameters, true);
        /*
         public class FirebaseVisionImage extends Object
         Represents an image object that can be used for both on-device and cloud API detectors.
         */

        FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        /*
         FirebaseVisionTextRecognizer  is a Text recognizer for performing optical character
         recognition(OCR) on an input image.
         */
        FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(firebaseImage)
                .addOnSuccessListener(this::processText)
                .addOnFailureListener(e -> textRecognitionCallBack.onRecognitionFailure(e.getMessage()));

    }


    /*
     public class FirebaseVisionText extends Object
     A hierarchical representation of texts.A FirebaseVisionText contains a list of FirebaseVisionText.TextBlock,
     and a FirebaseVisionText.TextBlock contains a list of FirebaseVisionText.Line which is composed of a list of
     FirebaseVisionText.Element.
     */

    private void processText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        TapCard card = new TapCard();
        for (FirebaseVisionText.TextBlock word : blocks
        ) {
            System.out.println("word is leght>>"+word.getText().length());
            System.out.println("word is"+word.getText());
            System.out.println("word is ss"+ word.getLines().size());

            if (isHolderName(word.getText()))
                card.setCardHolder(word.getText());

            if (word.getText().matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$"))
                card.setExpirationDate(word.getText());

         //   if (word.getText().contains(" ")) {
                if (word.getText().replace(" ", "")
                        .matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$"))
                    card.setCardNumber(word.getText());

         //   }
               /*if (isNumeric(word.getText()) && word.getText().length()>=4){
                   String rere = "";
                    rere = rere.concat(word.getText());
                   card.setCardNumber(rere);
               }

*/

/*
       String regex = "^(?:4[0-9]\\w+\n{12}(?:[0-9]\\w+\n{3}))$";
            if (word.getText().replace(")","").replace("|","").matches(regex) )
                card.setCardNumber(word.getText());*/
          /*  if (word.getText().contains(")")||word.getText().contains("|")){
                card.setCardNumber(word.getText().replace(")","").replace("|",""));
            }*/
          //  card.setCardNumber(word.getText().replace(")","").replace("|",""));

        }
        if (card != null){
            textRecognitionCallBack.onRecognitionSuccess(card);
           // TapTextRecognitionML.getListener().onReadSuccess(card);
          // _tapScannerCallback.onReadSuccess(card);
        }

        else
            textRecognitionCallBack.onRecognitionFailure("No data founded");

    }


    public void processScannedCardDetails(String word){

     //   System.out.println("processScannedCardDetails>>>>"+word);
      //      System.out.println("check words has newline>>>>"+word.contains(NEW_LINE));

        if (isHolderName(word))
            card.setCardHolder(word);

        if (word.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$")){
            card.setExpirationDate(word);
        }else if (word.matches("^(0[1-9]|1[0-2]|[1-9])-(1[4-9]|[2-9][0-9]|20[1-9][1-9])$")){
            card.setExpirationDate(word);
        }


        if (word.replace(" ", "")
                .matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$")){
            card.setCardNumber(word);
        }else if(word.contains("|") || word.contains(")")|| word.contains("(")|| word.length()<=5|| word.contains(System.lineSeparator()) && isNumeric(word)) {
           // text = new StringBuffer();

            text.append(word);

           // System.out.println("text is called"+text.toString().replace("\n","").matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$"));
            System.out.println("text is called"+text);

            String cardNumber = text.toString().replace("\n", "").replace("|", "").replace(")","").replace("(","");
            System.out.println("cardNumber is called"+cardNumber);

            if (cardNumber.matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$")) {
                System.out.println("text cardNumber is called"+text.toString().replace("\n", " "));
                card.setCardNumber(text.toString().replace("\n", " ").replace("|", ""));
            }
        }

        if (card != null){
            if(card.getCardNumber()!=null && card.getCardHolder()!=null &&  card.getExpirationDate()!=null) {
                textRecognitionCallBack.onRecognitionSuccess(card);
            }
        }

        else
            textRecognitionCallBack.onRecognitionFailure("No data founded");
    }

    private boolean isHolderName(String text) {
        if (text == null) return false;
        else {
            return isUpperCase(text)
                    && text.length() > 9
                    && text.split("\\s+").length > 1;
        }
    }

    private boolean isUpperCase(String text) {
        if (text != null) {
            char[] characters = text.replace(" ", "").toCharArray();
            for (Character character : characters) {
                if (!Character.isUpperCase(character) && !character.equals('\'') && !character.equals('.'))
                    return false;
            }
        }
        return true;
    }

    public void addTapScannerCallback(TapScannerCallback tapScannerCallback){
        _tapScannerCallback = tapScannerCallback;
    }

    public static TapScannerCallback getListener(){
        return _tapScannerCallback;
    }

    public void setFrameColor(int _frameColor){
        frameColor = _frameColor;
    }

    public static int getFrameColor(){
        return frameColor;
    }
    public static boolean isNumeric(String str) {
        return str.matches("\n?\\d+(\\|\\d+)?");  //match a number with optional '-' and decimal.
    }

    public boolean isNumerice(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }


}
