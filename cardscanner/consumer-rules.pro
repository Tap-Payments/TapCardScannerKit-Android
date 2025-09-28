########################################
## ML Kit (Text + Barcode)
########################################
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Play Services base (required for ML Kit)
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

########################################
## CameraX
########################################
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

########################################
## Blurry library
########################################
-keep class jp.wasabeef.blurry.** { *; }
-dontwarn jp.wasabeef.blurry.**

########################################
## Your library’s public API
########################################
-keep class company.tap.cardscanner.** { *; }
