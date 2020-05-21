# TapCardScannerKit-Android
A SDK that provides an interface to scan different types of payment cards with various ways and contexts.

[![Platform](https://img.shields.io/badge/platform-Android-inactive.svg?style=flat)](https://github.com/Tap-Payments/TapCardScannerKit-Android.git)
[![SDK Version](https://img.shields.io/badge/minSdkVersion-19-blue.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/targetSdkVersion-29-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)

## Requirements

To use the SDK the following requirements must be met:

1. **Android Studio 3.6** or newer
2. **Android SDK Tools 29.0.0 ** or newer
3. **Android Platform Version: API 29: Android 10.0 (Q)
4. **Android targetSdkVersion: 29

# Installation
---
<a name="include_library_to_code_locally"></a>
### Include TapCardScannerKit library as a dependency module in your project
---
1. Clone TapCardScannerKit library from Tap repository
   ```
       git@github.com:Tap-Payments/TapCardScannerKit-Android.git
    ```
2. Add TapCardScannerKit library to your project settings.gradle file as following
    ```java
        include ':cardscanner', ':YourAppName'
    ```
3. Setup your project to include cardscanner as a dependency Module.
   1. File -> Project Structure -> Modules -> << your project name >>
   2. Dependencies -> click on **+** icon in the screen bottom -> add Module Dependency
   3. select cardscanner library

<a name="installation_with_jitpack"></a>
### Installation with JitPack
---
[JitPack](https://jitpack.io/) is a novel package repository for JVM and Android projects. It builds Git projects on demand and provides you with ready-to-use artifacts (jar, aar).

To integrate TapCardScannerKit into your project add it in your **root** `build.gradle` at the end of repositories:
```java
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```java
	dependencies {

          }
```
## Features
**TapCardScannerKit-Android** provides extensive ways for scanning payment cards whether:

1. Embossed cards:

   i. Cards that has digits and letters raised and well crafted.

   ii. Examples: Visa , Mastercard , Amex

    ![All of Your Embossing Questions, Answered.](https://www.cardsource.com/hs-fs/hubfs/Images%20for%20sharing%20(bigger%20files)/shutterstock_613618706%20(1).jpg?width=400&name=shutterstock_613618706%20(1).jpg)

   iii. For more info check : [Embossed Card Explaination](https://www.creditcards.com/credit-card-news/glossary/termembossed.php)

2. Unembossed cards:

   i. Cards where the numbers on the card are not raised up like a normal card. It is perfectly flat, with everything just           printed on the card.

   ii. Examples:Some types of Visa , KNET in Kuwait, MADA in KSA

   ![Prepaid Cards | Mastercard](https://www.mastercard.ca/en-ca/consumers/find-card-products/prepaid-cards/_jcr_content/contentpar/herolight/image.adaptive.479.high.png/1516312754375.png)

   iii. For more info check: [Unembossed Card Explaination](https://www.commercebank.com/sharedcontent/pdfs/merchant-online/MerchantOnline_winter07.pdf).

### Asynchronous Card Scanning

The kit provides an asyncronous offline way to scan cards from a camera feed right away in your app. This works great in case of scanning embossed cards. The Kit provides following options:

1. Full screen card scanner.
2. Inline card scanner.
3. Image Decoder.

## Setup
### Configure Full screen card scanner
1. ScanCardIntent handles opening of the scanner and returning back the result to the client after scanning.
Below is the sample to call  ScanCardIntent.
```
Intent intent = new ScanCardIntent.Builder(this).build();
startActivityForResult(intent, SCAN_CARD_ID);
```
2. Handling of the ScanCard by getting the parcelablextra through **ScanCardIntent.RESULT_PAYCARDS_CARD** :
```
 @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
          case SCAN_CARD_ID:
          if (resultCode == Activity.RESULT_OK) {
                    Card card = data.getParcelableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);
                    cardNumber.setText(card.getCardNumber());
                    cardHolder.setText(card.getCardHolderName());
                    expirationDate.setText(card.getExpirationDate());
           }
           break;
```

### Configure Inline card scanner
1. **InlineViewFragment** is responsible for opening the scanner in a specified view set by the client.
The InlineViewFragment will follow the configuration set by the parent activity or fragment.
```
getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.inline_container, new InlineViewFragment())
                .commit();
```
2. To handle the scanresult of InlineViewFragment implement your activity or fragment with interface
  **InlineViewCallback** which generates the methods as below :
```
  @Override
    public void onScanCardFailed(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanCardFinished(Card card, byte[] cardImage) {
        removeInlineScanner();
        cardNumber.setText(card.getCardNumber());
        cardHolder.setText(card.getCardHolderName());
        expirationDate.setText(card.getExpirationDate());
    }
```
### Configure Image Decoder
This feature allows user to scan unembossed cards as well as choose images from the gallery.It uses
TapTextRecognitionML that automates reading of credit cards, which also extracts text from pictures of documents.

1. Declare the **TapTextRecognitionML** in your activity or fragment as shown:
```
 private TapTextRecognitionML textRecognitionML;
```
2. Initialize the TapTextRecognitionML as below:
```
textRecognitionML = new TapTextRecognitionML(this);
```
3.Decode the image from the bitmap as shown
```
 @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
          switch (requestCode) {
          case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                textRecognitionML.decodeImage(bitmap);
                break;
```
4. To handle the scannned result from the textRecognitionML implement the class with **TapTextRecognitionCallBack**
which generates methods as below:
```
    @Override
    public void onRecognitionSuccess(TapCard card) {
        cardNumber.setText(card.getCardNumber());
        cardHolder.setText(card.getCardHolder());
        expirationDate.setText(card.getExpirationDate());
    }

    @Override
    public void onRecognitionFailure(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
```
### Configure TapCountDownTimer (Optional)
This feature allows user to close the view automatically without manual interaction.
The user can set the timer as per the choice as below:
```
final TapCountDownTimer counter = new TapCountDownTimer(this);
        counter.setTimer(15000, 1000);
        counter.start(() -> {
            Toast.makeText(MainActivity.this, "Timed out", Toast.LENGTH_SHORT).show();
            removeInlineScanner();
            finishActivity(SCAN_CARD_ID);
        });
```