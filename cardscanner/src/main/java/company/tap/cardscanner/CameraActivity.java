package company.tap.cardscanner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, TapTextRecognitionCallBack {

        TextView textView;
        PreviewView mCameraView;
        SurfaceHolder holder;
        SurfaceView surfaceView;
        Canvas canvas;
        Paint paint;
        int cameraHeight, cameraWidth, xOffset, yOffset, boxWidth, boxHeight;
        private TapTextRecognitionML textRecognitionML;
        private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private TapScannerCallback tapScannerCallback;
        private static final String TAG = "CameraActivity";

        public void setCallBack(TapScannerCallback tapScannerCallback) {
                this.tapScannerCallback = tapScannerCallback;
        }

        /**
         * Starting Camera
         */
        void startCamera() {
                mCameraView = findViewById(R.id.previewView);
                cameraProviderFuture = ProcessCameraProvider.getInstance(this);

                cameraProviderFuture.addListener(() -> {
                        try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                CameraActivity.this.bindPreview(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                                Log.e(TAG, "Camera start error", e);
                        }
                }, ContextCompat.getMainExecutor(this));
        }

        /**
         * Binding to camera
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void bindPreview(ProcessCameraProvider cameraProvider) {
                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(mCameraView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setTargetResolution(new Size(720, 1488))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(executor, this::analyzeImage);

                Camera camera = cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        imageAnalysis,
                        preview
                );
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void analyzeImage(@NonNull ImageProxy image) {
                Image mediaImage = image.getImage();
                if (mediaImage == null) {
                        image.close();
                        return;
                }

                InputImage inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        image.getImageInfo().getRotationDegrees()
                );

                // Run ML Kit text recognition
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(inputImage)
                        .addOnSuccessListener(firebaseVisionText -> {
                                textView = findViewById(R.id.text);
                                String fullText = firebaseVisionText.getText();
                                textView.setText(fullText);

                                for (Text.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                        String blockText = block.getText();
                                        textRecognitionML.processScannedCardDetails(blockText);

                                        for (Text.Line line : block.getLines()) {
                                                String lineText = line.getText();
                                                Log.d(TAG, "Line: " + lineText);
                                        }
                                }
                                image.close();
                        })
                        .addOnFailureListener(e -> {
                                Log.e(TAG, "Detection failed", e);
                                image.close();
                        });
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_camera);

                Intent intent = getIntent();
                tapScannerCallback = (TapScannerCallback) intent.getSerializableExtra("interface");

                Log.d(TAG, "tapScannerCallback: " + tapScannerCallback);

                // Start Camera
                startCamera();

                // Create the bounding box overlay
                surfaceView = findViewById(R.id.overlay);
                surfaceView.setZOrderOnTop(true);
                holder = surfaceView.getHolder();
                holder.setFormat(PixelFormat.TRANSPARENT);
                holder.addCallback(this);
                textRecognitionML = new TapTextRecognitionML(this);
        }

        /**
         * For drawing the rectangular box
         */
        private void DrawFocusRect(int color) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = mCameraView.getHeight();
                int width = mCameraView.getWidth();

                int left, right, top, bottom, diameter;

                diameter = Math.min(width, height);
                int offset = (int) (0.05 * diameter);
                diameter -= offset;

                canvas = holder.lockCanvas();
                if (canvas == null) return;

                canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(color);
                paint.setStrokeWidth(5);

                left = width / 2 - diameter / 2;
                top = height / 2 - diameter / 2;
                right = width / 2 + diameter / 2;
                bottom = height / 2 + diameter / 2;

                canvas.drawPath(createCornersPath(left, top, right, bottom, 100), paint);

                holder.unlockCanvasAndPost(canvas);
        }

        private Path createCornersPath(int left, int top, int right, int bottom, int cornerWidth) {
                Path path = new Path();

                path.moveTo(left, top + cornerWidth);
                path.lineTo(left, top);
                path.lineTo(left + cornerWidth, top);

                path.moveTo(right - cornerWidth, top);
                path.lineTo(right, top);
                path.lineTo(right, top + cornerWidth);

                path.moveTo(left, bottom - cornerWidth);
                path.lineTo(left, bottom);
                path.lineTo(left + cornerWidth, bottom);

                path.moveTo(right - cornerWidth, bottom);
                path.lineTo(right, bottom);
                path.lineTo(right, bottom - cornerWidth);

                return path;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {}

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                DrawFocusRect(TapTextRecognitionML.getFrameColor());
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}

        @Override
        public void onRecognitionSuccess(TapCard card) {
                if (card != null &&
                        card.getCardNumber() != null &&
                        card.getCardHolder() != null &&
                        card.getExpirationDate() != null) {

                        TapTextRecognitionML.getListener().onReadSuccess(card);

                        Log.d(TAG, "Card Number: " + card.getCardNumber());
                        Log.d(TAG, "Expiry: " + card.getExpirationDate());
                        Log.d(TAG, "Holder: " + card.getCardHolder());

                        finish();
                }
        }

        @Override
        public void onRecognitionFailure(String error) {
                TapTextRecognitionML.getListener().onReadFailure(error);
        }

        @Override
        protected void onPause() {
                super.onPause();
                if (mCameraView != null) {
                        mCameraView.removeAllViews();
                }
        }
}
