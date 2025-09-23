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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

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
        //FirebaseInitializer.initFirebase(appContext); // keep your logic
    }

    public CameraFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
        // FirebaseApp.initializeApp(appContext); // ❌ no longer needed for ML Kit
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

    void startCamera(View view) {
        mCameraView = view.findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            if (!isAdded()) return;

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || getContext() == null) return;
                    bindPreview(cameraProvider);
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
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

        imageAnalysis.setAnalyzer(executor, this::analyzeImage);

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(ImageProxy image) {
        if (!isAdded() || getContext() == null) {
            image.close();
            return;
        }

        Image mediaImage = image.getImage();
        if (mediaImage == null) {
            image.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(mediaImage,
                image.getImageInfo().getRotationDegrees());

        com.google.mlkit.vision.text.TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(inputImage)
                .addOnSuccessListener(firebaseVisionText -> {
                    for (Text.TextBlock block : firebaseVisionText.getTextBlocks()) {
                        for (Text.Line line : block.getLines()) {
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
        if (canvas == null) return;

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
