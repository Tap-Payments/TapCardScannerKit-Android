package company.tap.cardscanner;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TapTextRecognitionCallBack textRecognitionCallBack;

    public TapTextRecognitionML(TapTextRecognitionCallBack textRecognitionCallBack) {
        this.textRecognitionCallBack = textRecognitionCallBack;
    }

    public void decodeImage(Bitmap imageBitmap) {
        // Capture image & SDK version
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("bitmap", String.valueOf(imageBitmap));
        parameters.put("sdk", BuildConfig.VERSION_NAME);
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_DECODE_IMAGE, parameters, true);
        /*
         FirebaseVisionImage represents an image object that can be used for on-device detectors.
         */
        // TODO: 4/21/20 rename image >> firebaseImage
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        /*
         FirebaseVisionTextRecognizer  is a Text recognizer for performing optical character
         recognition(OCR) on an input image.
         */
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        // TODO: 4/21/20 no need to save result in a variable

        detector.processImage(image)
                // TODO: 4/21/20 use lambda
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processText(firebaseVisionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textRecognitionCallBack.onRecognitionFailure(e.getMessage());
                    }
                });


    }

    // TODO: 4/21/20 use proper method documentatioins
    /*
     Processes the FirebaseVisionText to identify cardNumber,cardHolder,cardexpirationDate
     */

    private void processText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        TapCard card = new TapCard();
        // TODO: 4/21/20 we can replace for with foreach
        for (int i = 0; i < blocks.size(); i++) {
            // TODO: 4/21/20 secure code by checking if not null  blocks.get(i) >> may result null
            String word = blocks.get(i).getText();

            if (isHolderName(word))
                card.setCardHolder(word);

            if (word.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$"))
                card.setExpirationDate(word);

            if (word.replace(" ", "")
                    .matches("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$"))
                card.setCardNumber(word);
        }
        if (card.getCardNumber() != null)
            textRecognitionCallBack.onRecognitionSuccess(card);
        else
            textRecognitionCallBack.onRecognitionFailure("No data founded");
    }

    private boolean isHolderName(String text) {
        // TODO: 4/21/20 secure your code by checking if text!=null
        return isUpperCase(text)
                && text.length() > 9
                && text.split("\\s+").length > 1;
    }

    private boolean isUpperCase(String text) {
        // TODO: 4/21/20 secure your code by checking if text!=null
        char[] characters = text.replace(" ", "").toCharArray();
        for (Character character : characters) {
            if (!Character.isUpperCase(character) && !character.equals('\'') && !character.equals('.'))
                return false;
        }
        return true;
    }

}
