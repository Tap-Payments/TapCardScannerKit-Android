package company.tap.cardscanner;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Migrated TapTextRecognitionML using ML Kit Text Recognition API
 */
public class TapTextRecognitionML {
    private TapTextRecognitionCallBack textRecognitionCallBack;
    private static TapScannerCallback _tapScannerCallback;
    private static int frameColor = Color.WHITE;
    TapCard card = new TapCard();
    public static final String NEW_LINE = System.getProperty("line.separator");

    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    StringBuffer text = new StringBuffer();

    public TapTextRecognitionML(TapTextRecognitionCallBack textRecognitionCallBack) {
        this.textRecognitionCallBack = textRecognitionCallBack;
    }

    public void decodeImage(Bitmap imageBitmap) {
        // Convert bitmap to ML Kit's InputImage
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        // Initialize recognizer with Latin script options
        TextRecognizer recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
        );

        recognizer.process(image)
                .addOnSuccessListener(this::processText)
                .addOnFailureListener(e ->
                        textRecognitionCallBack.onRecognitionFailure(e.getMessage()));
    }

    private void processText(@NonNull Text visionText) {
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        TapCard card = new TapCard();

        for (Text.TextBlock block : blocks) {
            String blockText = block.getText();

            System.out.println("Block text length: " + blockText.length());
            System.out.println("Block text: " + blockText);
            System.out.println("Block lines count: " + block.getLines().size());

            if (isHolderName(blockText))
                card.setCardHolder(blockText);

            if (blockText.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$"))
                card.setExpirationDate(blockText);

            if (blockText.replace(" ", "").matches(
                    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$")) {
                card.setCardNumber(blockText);
            }
        }

        if (card != null) {
            textRecognitionCallBack.onRecognitionSuccess(card);
        } else {
            textRecognitionCallBack.onRecognitionFailure("No data found");
        }
    }

    // The rest of your existing helper methods remain unchanged...
    public void processScannedCardDetails(String word) {
        if (isHolderName(word))
            card.setCardHolder(word);

        if (word.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$") ||
                word.matches("^(0[1-9]|1[0-2]|[1-9])-(1[4-9]|[2-9][0-9]|20[1-9][1-9])$")) {
            card.setExpirationDate(word);
        }

        if (word.replace(" ", "").matches(
                "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$")) {
            card.setCardNumber(word);
        } else if (word.contains("|") || word.contains(")") || word.contains("(") || word.length() <= 5 || (word.contains(System.lineSeparator()) && isNumeric(word))) {
            text.append(word);

            String cardNumber = text.toString().replace("\n", "").replace("|", "").replace(")", "").replace("(", "");

            if (cardNumber.matches(
                    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})|(?:124|124|35\\d{124})$")) {
                card.setCardNumber(cardNumber);
            }
        }

        if (card != null) {
            if (card.getCardNumber() != null && card.getCardHolder() != null && card.getExpirationDate() != null) {
                textRecognitionCallBack.onRecognitionSuccess(card);
            }
        } else {
            textRecognitionCallBack.onRecognitionFailure("No data found");
        }
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

    public void addTapScannerCallback(TapScannerCallback tapScannerCallback) {
        _tapScannerCallback = tapScannerCallback;
    }

    public static TapScannerCallback getListener() {
        return _tapScannerCallback;
    }

    public void setFrameColor(int _frameColor) {
        frameColor = _frameColor;
    }

    public static int getFrameColor() {
        return frameColor;
    }

    public static boolean isNumeric(String str) {
        return str.matches("\n?\\d+(\\|\\d+)?");  // match a number with optional '-' and decimal.
    }

    public boolean isNumerice(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
