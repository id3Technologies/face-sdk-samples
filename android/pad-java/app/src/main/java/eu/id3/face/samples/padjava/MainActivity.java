package eu.id3.face.samples.padjava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import eu.id3.face.FaceError;
import eu.id3.face.PadStatus;
import eu.id3.face.PortraitInstruction;

public class MainActivity extends AppCompatActivity implements FaceProcessorListener {
    private final SpannableString okLabel = new SpannableString("OK");
    private final int colorOk = Color.GREEN;
    private final SpannableString notOkLabel = new SpannableString("NOT OK");
    private final int colorNotOk = Color.RED;
    /**
     * View elements
     */
    private Button startCaptureButton;
    private ImageView portraitFaceView;
    private TextView padAnalysisTextView;
    private CameraFragment captureFragment = new CameraFragment();
    /**
     * Face processor containing id3 Face SDK method calls for face detection and face recognition
     */
    private FaceProcessor faceProcessor;

    /**
     * State variables
     */
    private boolean isCapturing = false;
    private boolean isCameraPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* Verify that the application has the permissions requested by the SDK. */
        checkSdkPermissions();
        /*
         * Register the SDK License. It must be done before calling any SDK function.
         * Please go to the Credentials.kt to fill in your license information.
         */
        boolean isLicenseOk = Credentials.registerSdkLicense(getFilesDir().getAbsolutePath() + "/id3FaceLicense.lic");
        if (!isLicenseOk) {
            finish();
            System.exit(-1);
        }
        /* Init the face processor. */
        faceProcessor = new FaceProcessor(getApplicationContext());
        /* Setup the view elements. */
        setupViewElements();
    }

    public void onLargestFaceProcessed(FaceProcessor.AnalyzeLargestFaceResult analyzeLargestFaceResult) {
        runOnUiThread(() -> {
            int errorCode = analyzeLargestFaceResult.getErrorCode();
            if (errorCode == 0) {
                byte[] jpegPortraitImageBuffer = analyzeLargestFaceResult.getJpegPortraitImageBuffer();
                SpannableStringBuilder padAnalysisText = new SpannableStringBuilder();

                /* Create a bitmap image for drawing using the portrait image JPEG buffer. */
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        jpegPortraitImageBuffer,
                        0,
                        jpegPortraitImageBuffer.length
                );
                portraitFaceView.setImageBitmap(bitmap);

                /* Display user instruction */
                PortraitInstruction instruction = analyzeLargestFaceResult.getInstruction();
                if (instruction == PortraitInstruction.NONE) {
                    padAnalysisText.append("\n");
                } else {
                    padAnalysisText.append(instruction.name().replace('_', ' ')).append("\n");
                }

                padAnalysisText.append("\n");

                /* Display PAD score */
                int score = analyzeLargestFaceResult.getScore();
                padAnalysisText.append("Score : ");
                padAnalysisText.append(String.valueOf(score)).append("\n");

                /* Display PAD status */
                PadStatus status = analyzeLargestFaceResult.getStatus();
                padAnalysisText.append("Status : ");
                padAnalysisText.append(status.name());

                padAnalysisTextView.setText(padAnalysisText);
            } else {
                if (errorCode == FaceError.IOD_TOO_SMALL.getValue()) {
                    Toast.makeText(
                                    this,
                                    "Face is too far, get closer to the camera.",
                                    Toast.LENGTH_SHORT
                            )
                            .show();
                }
            }
        });
    }

    public void onResetFaceProcessed() {
        runOnUiThread(() -> {
            portraitFaceView = findViewById(R.id.portraitFaceView);
            portraitFaceView.setImageResource(R.drawable.empty_avatar);

            padAnalysisTextView.setText("");
        });
    }

    private void setupViewElements() {
        /* Initialize the capture fragment. */
        captureFragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
        assert captureFragment != null;
        captureFragment.setProcessor(faceProcessor);

        /* Initialize the other view elements. */
        portraitFaceView = findViewById(R.id.portraitFaceView);
        portraitFaceView.setImageResource(R.drawable.empty_avatar);
        padAnalysisTextView = findViewById(R.id.padAnalysisTextView);

        startCaptureButton = findViewById(R.id.startCaptureButton);

        /* Set start capture button on click listener. */
        startCaptureButton.setOnClickListener(v -> {
            if (isCameraPaused) {
                isCameraPaused = false;
                captureFragment.onResume();
            }
            if (isCapturing) {
                /* Reset enrolment and match information */
                portraitFaceView.setImageResource(R.drawable.empty_avatar);

                /* Reset the button text before stopping the capture. */
                startCaptureButton.setText(getResources().getString(R.string.start_capture_button_label));
                isCapturing = false;
                captureFragment.stopCapture();
            } else {
                /* Reset the button text before starting the capture. */
                startCaptureButton.setText(getResources().getString(R.string.stop_capture_button_label));

                captureFragment.startCapture();
                isCapturing = true;
            }
        });
    }


    /**
     * id3 Face SDK may need the following permissions:
     * - If using cameras: CAMERA
     * - If using online license retrieving: INTERNET
     * Requested permissions must be declared in the Android Manifest.
     * Moreover, for devices > Android 6.0 permissions must also be requested at runtime
     * (except INTERNET which is only classified as a normal permission)
     * This function will request INTERNET and CAMERA permissions.
     */
    private void checkSdkPermissions() {
        String[] requested_permissions = {Manifest.permission.CAMERA,
                Manifest.permission.INTERNET};
        if (!hasRequestedPermissions(requested_permissions)) {
            ActivityCompat.requestPermissions(this, requested_permissions, 0);
            while (!hasRequestedPermissions(requested_permissions)) {
                Log.v("Main activity", "Waiting for user to accept permissions");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasRequestedPermissions(String[] requested_permissions) {
        for (String permission : requested_permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}