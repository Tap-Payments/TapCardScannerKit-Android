package company.tap.cardscanner;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, TapTextRecognitionCallBack {

        private static final String TAG = "CameraActivity";

        private TextView textView;
        private PreviewView mCameraView;
        private SurfaceHolder holder;
        private SurfaceView surfaceView;
        private Canvas canvas;
        private Paint paint;
        private int xOffset, yOffset, boxWidth, boxHeight;

        private TapTextRecognitionML textRecognitionML;
        private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private TapScannerCallback tapScannerCallback;

        public void setCallBack(TapScannerCallback tapScannerCallback) {
                this.tapScannerCallback = tapScannerCallback;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_camera);

                // Get callback interface from intent extras or set elsewhere
                tapScannerCallback = (TapScannerCallback) getIntent().getSerializableExtra("interface");

                textView = findViewById(R.id.text);
                mCameraView = findViewById(R.id.previewView);

                surfaceView = findViewById(R.id.overlay);
                surfaceView.setZOrderOnTop(true);
                holder = surfaceView.getHolder();
                holder.setFormat(PixelFormat.TRANSPARENT);
                holder.addCallback(this);

                textRecognitionML = new TapTextRecognitionML(this);

                startCamera();
        }

        void startCamera() {
                cameraProviderFuture = ProcessCameraProvider.getInstance(this);

                cameraProviderFuture.addListener(() -> {
                        try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                bindPreview(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                                Log.e(TAG, "Camera initialization failed.", e);
                        }
                }, ContextCompat.getMainExecutor(this));
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void bindPreview(ProcessCameraProvider cameraProvider) {
                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(mCameraView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(720, 1280)) // adjust as needed
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, this::analyzeImage);

                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
        }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @SuppressLint("UnsafeExperimentalUsageError")
        private void analyzeImage(@NonNull ImageProxy image) {
                Image mediaImage = image.getImage();
                if (mediaImage == null) {
                        image.close();
                        return;
                }

                int rotationDegrees = image.getImageInfo().getRotationDegrees();

                InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees);

                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                recognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> {
                                String resultText = visionText.getText();
                                textView.setText(resultText);

                                // Process each block with your existing logic
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                        textRecognitionML.processScannedCardDetails(block.getText());
                                }

                                image.close();
                        })
                        .addOnFailureListener(e -> {
                                Log.e(TAG, "Text recognition failed: ", e);
                                image.close();
                        });
        }

        private void drawFocusRect(int color) {
                int width = mCameraView.getWidth();
                int height = mCameraView.getHeight();

                int diameter = Math.min(width, height);
                int offset = (int) (0.05 * diameter);
                diameter -= offset;

                int left = width / 2 - diameter / 2;
                int top = height / 2 - diameter / 2;
                int right = width / 2 + diameter / 2;
                int bottom = height / 2 + diameter / 2;

                canvas = holder.lockCanvas();
                if (canvas == null) return;

                canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(color);
                paint.setStrokeWidth(5);

                Path path = createCornersPath(left, top, right, bottom, 100);
                canvas.drawPath(path, paint);

                holder.unlockCanvasAndPost(canvas);

                xOffset = left;
                yOffset = top;
                boxWidth = right - left;
                boxHeight = bottom - top;
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
        public void surfaceCreated(SurfaceHolder holder) {
                // No-op
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                drawFocusRect(TapTextRecognitionML.getFrameColor());
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
                // No-op
        }

        @Override
        public void onRecognitionSuccess(TapCard card) {
                if (card != null &&
                        card.getCardNumber() != null &&
                        card.getCardHolder() != null &&
                        card.getExpirationDate() != null) {

                        TapTextRecognitionML.getListener().onReadSuccess(card);

                        Log.i(TAG, "Card Number: " + card.getCardNumber());
                        Log.i(TAG, "Expiration Date: " + card.getExpirationDate());
                        Log.i(TAG, "Card Holder: " + card.getCardHolder());

                        finish();
                }
        }

        @Override
        public void onRecognitionFailure(String error) {
                TapTextRecognitionML.getListener().onReadFailure(error);
                // optionally finish() or retry
        }

        @Override
        protected void onPause() {
                super.onPause();
                if (mCameraView != null) {
                        mCameraView.removeAllViews();
                }
        }
}
