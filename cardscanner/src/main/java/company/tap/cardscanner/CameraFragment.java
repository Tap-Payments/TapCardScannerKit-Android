package company.tap.cardscanner;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 *
 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback ,TapTextRecognitionCallBack{
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
    private TapScannerCallback tapScannerCallback ;
    private static final String TAG = "CameraActivity";


    public void setCallBack(TapScannerCallback tapScannerCallback) {
        this.tapScannerCallback = tapScannerCallback;
    }

    /**
     * Responsible for converting the rotation degrees from CameraX into the one compatible with Firebase ML
     */

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    /**
     * Starting Camera
     * @param view
     */
    void startCamera(View view) {
        mCameraView = view.findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                   bindPreview(cameraProvider ,view);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(getContext()));

    }

    /**
     * Binding to camera
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void bindPreview(ProcessCameraProvider cameraProvider, View view) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mCameraView.createSurfaceProvider());

        //Image Analysis Function
        //Set static size according to your device or write a dynamic function for it
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(720, 1488))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();


        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                //changing normal degrees into Firebase rotation
                int rotationDegrees = degreesToFirebaseRotation(image.getImageInfo().getRotationDegrees());
                if (image == null || image.getImage() == null) {
                    return;
                }
//Getting a FirebaseVisionImage object using the Image object and rotationDegrees
                final Image mediaImage = image.getImage();
                FirebaseVisionImage images = FirebaseVisionImage.fromMediaImage(mediaImage, rotationDegrees);
                //Getting bitmap from FirebaseVisionImage Object
                Bitmap bmp = images.getBitmap();
                //Getting the values for cropping
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = bmp.getHeight();
                int width = bmp.getWidth();

                int left, right, top, bottom, diameter;

                diameter = width;
                if (height < width) {
                    diameter = height;
                }

                int offset = (int) (0.05 * diameter);
                diameter -= offset;

                left = (int) (width / 2 - diameter / 2.5);
                top = (int) (height / 2 - diameter / 2.5);
                right = (int) (width / 2 + diameter / 2.5);
                bottom = (int) (height / 2 + diameter / 2.5);

                xOffset = left;
                yOffset = top;

                //Creating new cropped bitmap
                Bitmap bitmap = Bitmap.createBitmap(bmp, left, top, boxWidth, boxHeight);
                //initializing FirebaseVisionTextRecognizer object
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();
                //Passing FirebaseVisionImage Object created from the cropped bitmap
                Task<FirebaseVisionText> result = detector.processImage(FirebaseVisionImage.fromBitmap(bitmap))
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...
                                textView = view.findViewById(R.id.text);
                                //getting decoded text
                                String text = firebaseVisionText.getText();
                                //Setting the decoded text in the texttview
                                textView.setText(text);
                                // textRecognitionML.decodeImage(bitmap);
                                //for getting blocks and line elements
                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    // System.out.println("blockText ll are"+blockText.length());
                                    //  System.out.println("blockText are"+blockText);
                                    // textRecognitionML.processText(blockText);
                                    textRecognitionML.processScannedCardDetails(blockText);
                                    for (FirebaseVisionText.Line line : block.getLines()) {
                                        String lineText = line.getText();
                                        for (FirebaseVisionText.Element element : line.getElements()) {
                                            String elementText = element.getText();

                                        }
                                        System.out.println("lineText are"+lineText);
                                    }
                                }
                                image.close();
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.e("Error", e.toString());
                                        image.close();
                                    }
                                });
            }


        });
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);

    }
    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_camera, container, false);
        startCamera(view);
        // blurView(this);

        //Create the bounding box
        surfaceView = view.findViewById(R.id.overlay);
        surfaceView.setZOrderOnTop(true);
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        textRecognitionML = new TapTextRecognitionML(this);
        return view;
    }

    /**
     * For drawing the rectangular box
     */
    private void DrawFocusRect(int color) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = mCameraView.getHeight();
        int width = mCameraView.getWidth();

        //cameraHeight = height;
        //cameraWidth = width;

        int left, right, top, bottom, diameter;

        diameter = width;
        if (height < width) {
            diameter = height;
        }

        int offset = (int) (0.05 * diameter);
        diameter -= offset;

        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //border's properties
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);

        left = (int) (width / 2 - diameter / 2.5);
        top = (int) (height / 2 - diameter / 2.5);
        right = (int) (width / 2 + diameter / 2.5);
        bottom = (int) (height / 2 + diameter / 2.5);
/*
                left = width / 2 - 500;
                top = height / 2 - 500;
                right = width / 2 + 500;
                bottom = height / 2 + 500;*/

        // System.out.println("left"+left +"\n"+ "right"+right +"\n"+"top"+top +"\n"+"bottom"+bottom +"\n");
        xOffset = left;
        yOffset = top;
        boxHeight = bottom - top - 200;
        boxWidth = right - left;
        //Changing the value of x in diameter/x will change the size of the box ; inversely proportionate to x
        // canvas.drawRect(left, top, right, bottom, paint);
        // canvas.drawPath(createCornersPath(left/2 - 500, top/2 - 500, right/2  +500, bottom/2 + 500, 150), paint);
        canvas.drawPath(createCornersPath(left,top,right,bottom, 100), paint);
        holder.unlockCanvasAndPost(canvas);
    }

    private Path createCornersPath(int left, int top, int right, int bottom, int cornerWidth){
        Path path = new Path();

        path.moveTo(left, top + cornerWidth);
        path.lineTo(left, top);
        path.lineTo(left + cornerWidth, top);

        path.moveTo(right - cornerWidth, top);
        path.lineTo(right, top);
        path.lineTo(right , top + cornerWidth);

        path.moveTo(left, bottom - cornerWidth);
        path.lineTo(left, bottom);
        path.lineTo(left + cornerWidth, bottom);

        path.moveTo(right - cornerWidth, bottom);
        path.lineTo(right, bottom);
        path.lineTo(right, bottom - cornerWidth);


        return path;
    }
    /**
     * Callback functions for the surface Holder
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Drawing rectangle
        DrawFocusRect(TapTextRecognitionML.getFrameColor());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRecognitionSuccess(TapCard card) {
        if(card!=null){
            if(card.getCardNumber()!=null && card.getCardHolder()!=null &&  card.getExpirationDate()!=null){
                TapTextRecognitionML.getListener().onReadSuccess(card);
                Log.e(TAG, "onRecognitionSuccess: "+card.getCardNumber());
                Log.e(TAG, "onRecognitionSuccess: "+card.getExpirationDate());
                Log.e(TAG, "onRecognitionSuccess: "+card.getCardHolder());
                //finish();
            }
        }



    }

    @Override
    public void onRecognitionFailure(String error) {
        TapTextRecognitionML.getListener().onReadFailure(error);
        //finish();
    }


    @Override
    public void onPause() {
        super.onPause();
        // if you are using MediaRecorder, release it first
        mCameraView.removeAllViews();
    }
}