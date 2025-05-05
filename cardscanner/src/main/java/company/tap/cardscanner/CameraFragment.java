package company.tap.cardscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.camera.core.*;
import androidx.camera.core.Camera;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.*;
import com.google.firebase.ml.vision.text.*;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.*;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, TapTextRecognitionCallBack {

    private static final String TAG = "CameraFragment";
    private TextView textView;
    private PreviewView mCameraView;
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private Canvas canvas;
    private Paint paint;
    private int displayMetrics;

    private TapTextRecognitionML textRecognitionML;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TapScannerCallback tapScannerCallback;
    private Preview preview;
    private Camera camera;
    private static Context appContext;

    public void setCallBack(TapScannerCallback callback, Context context) {
        this.tapScannerCallback = callback;
        appContext = context.getApplicationContext();
        FirebaseInitializer.initFirebase(appContext);
    }

    public CameraFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
        FirebaseApp.initializeApp(appContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initViews(view);
        startCamera(view);
        return view;
    }

    private void initViews(View view) {
        mCameraView = view.findViewById(R.id.previewView);
        surfaceView = view.findViewById(R.id.overlay);
        surfaceView.setZOrderOnTop(true);
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        textRecognitionML = new TapTextRecognitionML(this);
    }

    private void startCamera(View view) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(appContext);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: ", e);
            }
        }, ContextCompat.getMainExecutor(appContext));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mCameraView.getSurfaceProvider());
        mCameraView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(metrics.widthPixels, metrics.heightPixels))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, image -> analyzeImage(image));

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(ImageProxy image) {
        if (!isAdded() || getContext() == null) return;

        Image mediaImage = image.getImage();
        if (mediaImage == null) {
            image.close();
            return;
        }

        FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromMediaImage(mediaImage,
                degreesToFirebaseRotation(image.getImageInfo().getRotationDegrees()));
        Bitmap bitmap = firebaseImage.getBitmap();

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(FirebaseVisionImage.fromBitmap(bitmap))
                .addOnSuccessListener(firebaseVisionText -> {
                    for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                        for (FirebaseVisionText.Line line : block.getLines()) {
                            textRecognitionML.processScannedCardDetails(line.getText());
                        }
                    }
                    image.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Detection failed", e);
                    image.close();
                });
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0: return FirebaseVisionImageMetadata.ROTATION_0;
            case 90: return FirebaseVisionImageMetadata.ROTATION_90;
            case 180: return FirebaseVisionImageMetadata.ROTATION_180;
            case 270: return FirebaseVisionImageMetadata.ROTATION_270;
            default: throw new IllegalArgumentException("Invalid rotation degree: " + degrees);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawFocusRect(TapTextRecognitionML.getFrameColor());
    }

    private void drawFocusRect(int color) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = mCameraView.getWidth();
        int height = mCameraView.getHeight();
        displayMetrics = metrics.densityDpi;

        int diameter = Math.min(width, height) - (int) (0.05 * Math.min(width, height));
        int left = width / 2 - diameter / 2;
        int top = height / 2 - diameter / 2;
        int right = width / 2 + diameter / 2;
        int bottom = height / 2 + diameter / 2;

        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);

        Path path = createCornersPath(left, top, right, bottom,
                (displayMetrics <= DisplayMetrics.DENSITY_360) ? 50 : 100);
        canvas.drawPath(path, paint);

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
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);
    }

    @Override
    public void onRecognitionSuccess(TapCard card) {
        if (card != null && card.getCardNumber() != null && card.getCardHolder() != null && card.getExpirationDate() != null) {
            TapTextRecognitionML.getListener().onReadSuccess(card);
        }
    }

    @Override
    public void onRecognitionFailure(String error) {
        TapTextRecognitionML.getListener().onReadFailure(error);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null) mCameraView.removeAllViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.removeAllViews();
            mCameraView.clearAnimation();
        }
        if (executor != null) executor.shutdown();
    }
}
