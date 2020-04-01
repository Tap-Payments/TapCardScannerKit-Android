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

import java.util.List;

/**
 * Created by Mario Gamal on 4/1/20
 * Copyright © 2020 Tap Payments. All rights reserved.
 */
public class TapTextRecognitionML {

    private TapTextRecognitionCallBack textRecognitionCallBack;

    public TapTextRecognitionML(TapTextRecognitionCallBack textRecognitionCallBack) {
        this.textRecognitionCallBack = textRecognitionCallBack;
    }

    public void decodeImage(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result = detector.processImage(image)
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

    private void processText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        TapCard card = new TapCard();
        for (int i = 0; i < blocks.size(); i++) {
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
        return isUpperCase(text)
                && text.length() > 9
                && text.split("\\s+").length >1;
    }

    private boolean isUpperCase(String text) {
        char [] characters = text.replace(" ", "").toCharArray();
        for (Character character: characters) {
            if (!Character.isUpperCase(character) && !character.equals('\'') && !character.equals('.'))
                return false;
        }
        return true;
    }

}
