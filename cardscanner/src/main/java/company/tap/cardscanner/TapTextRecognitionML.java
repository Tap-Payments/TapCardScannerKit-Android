package company.tap.cardscanner;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.List;
import java.util.regex.Pattern;

public class TapTextRecognitionML {

    private TapTextRecognitionCallBack textRecognitionCallBack;
    private static TapScannerCallback _tapScannerCallback;
    private static int frameColor = 0xFFFFFFFF; // White

    private TapCard card = new TapCard();
    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final StringBuilder textBuffer = new StringBuilder();

    public static final String NEW_LINE = System.getProperty("line.separator");

    public TapTextRecognitionML(TapTextRecognitionCallBack textRecognitionCallBack) {
        this.textRecognitionCallBack = textRecognitionCallBack;
    }

    public void decodeImage(Bitmap imageBitmap) {
        try {
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(this::processText)
                    .addOnFailureListener(e -> textRecognitionCallBack.onRecognitionFailure(e.getMessage()));

        } catch (Exception e) {
            textRecognitionCallBack.onRecognitionFailure("Failed to process image: " + e.getMessage());
        }
    }

    private void processText(Text visionText) {
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        TapCard detectedCard = new TapCard();

        for (Text.TextBlock block : blocks) {
            String blockText = block.getText();

            if (isHolderName(blockText))
                detectedCard.setCardHolder(blockText);

            if (blockText.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$"))
                detectedCard.setExpirationDate(blockText);

            // Card number pattern (Visa, MasterCard, Amex, etc)
            String cardNumberRegex = "^(?:4[0-9]{12}(?:[0-9]{3})?" +
                    "|[25][1-7][0-9]{14}" +
                    "|6(?:011|5[0-9]{2})[0-9]{12}" +
                    "|3[47][0-9]{13}" +
                    "|3(?:0[0-5]|[68][0-9])[0-9]{11}" +
                    "|(?:2131|1800|35\\d{3})\\d{11})$";

            if (blockText.replace(" ", "").matches(cardNumberRegex))
                detectedCard.setCardNumber(blockText);
        }

        if (detectedCard != null &&
                detectedCard.getCardHolder() != null &&
                detectedCard.getCardNumber() != null &&
                detectedCard.getExpirationDate() != null) {

            textRecognitionCallBack.onRecognitionSuccess(detectedCard);
        } else {
            textRecognitionCallBack.onRecognitionFailure("No valid card data found");
        }
    }

    public void processScannedCardDetails(String word) {
        if (isHolderName(word))
            card.setCardHolder(word);

        if (word.matches("^(0[1-9]|1[0-2]|[1-9])/(1[4-9]|[2-9][0-9]|20[1-9][1-9])$")
                || word.matches("^(0[1-9]|1[0-2]|[1-9])-(1[4-9]|[2-9][0-9]|20[1-9][1-9])$")) {
            card.setExpirationDate(word);
        }

        String cardNumberRegex = "^(?:4[0-9]{12}(?:[0-9]{3})?" +
                "|[25][1-7][0-9]{14}" +
                "|6(?:011|5[0-9][0-9])[0-9]{12}" +
                "|3[47][0-9]{13}" +
                "|3(?:0[0-5]|[68][0-9])[0-9]{11}" +
                "|(?:2131|1800|35\\d{3})\\d{11})$";

        if (word.replace(" ", "").matches(cardNumberRegex)) {
            card.setCardNumber(word);
        } else if (word.contains("|") || word.contains(")") || word.contains("(") || word.length() <= 5
                || (word.contains(System.lineSeparator()) && isNumeric(word))) {

            textBuffer.append(word);

            String cardNumber = textBuffer.toString().replace("\n", "")
                    .replace("|", "")
                    .replace(")", "")
                    .replace("(", "");

            if (cardNumber.matches(cardNumberRegex)) {
                card.setCardNumber(cardNumber);
            }
        }

        if (card.getCardNumber() != null && card.getCardHolder() != null && card.getExpirationDate() != null) {
            textRecognitionCallBack.onRecognitionSuccess(card);
        } else {
            textRecognitionCallBack.onRecognitionFailure("No valid card data found");
        }
    }

    private boolean isHolderName(String text) {
        if (text == null) return false;
        return isUpperCase(text) && text.length() > 9 && text.trim().split("\\s+").length > 1;
    }

    private boolean isUpperCase(String text) {
        if (text == null) return false;
        char[] characters = text.replace(" ", "").toCharArray();
        for (char c : characters) {
            if (!Character.isUpperCase(c) && c != '\'' && c != '.') return false;
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
        if (str == null) return false;
        return str.matches("\\d+");  // simple numeric check
    }

    public boolean isNumerice(String strNum) {
        if (strNum == null) return false;
        return pattern.matcher(strNum).matches();
    }
}
