package eu.id3.face.samples.recognitionjava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements FaceProcessorListener {
    /**
     * View elements
     */
    private Button startCaptureButton;
    private Button enrollButton;
    private Button matchButton;
    private ImageView enrolledFaceView;
    private TextView qualityEnrolledTextView;
    private TextView qualityMatchTextView;
    private TextView decisionTextView;
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
    private boolean isTemplateEnrolled = false;

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

    public void onLargestFaceEnrolled(FaceProcessor.EnrollLargestFaceResult enrollLargestFaceResult) {
        runOnUiThread(() -> {
            byte[] jpegPortraitImageBuffer = enrollLargestFaceResult.getJpegPortraitImageBuffer();
            int quality = enrollLargestFaceResult.getQuality();

            /* Create a bitmap image for drawing using the portrait image JPEG buffer. */
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    jpegPortraitImageBuffer,
                    0,
                    jpegPortraitImageBuffer.length
            );
            enrolledFaceView.setImageBitmap(bitmap);

            /* Check if quality is high enough for matching and print feedback. */
            if (quality >= Parameters.encodingQualityThreshold) {
                qualityEnrolledTextView.setText(getResources().getString(R.string.quality_text_view_value, quality));
            } else {
                qualityEnrolledTextView.setText(getResources().getString(R.string.quality_text_view_warning, quality));
            }

            /* Enable match button when a template is enrolled. */
            isTemplateEnrolled = true;
            matchButton.setEnabled(true);
        });
    }

    public void onLargestFaceVerified(FaceProcessor.VerifyLargestFaceResult verifyLargestFaceResult) {
        runOnUiThread(() -> {
            int quality = verifyLargestFaceResult.getQuality();
            int score = verifyLargestFaceResult.getScore();

            /* Check if quality is high enough for matching and print feedback. */
            if (quality >= Parameters.encodingQualityThreshold) {
                qualityMatchTextView.setText(getResources().getString(R.string.quality_text_view_value, quality));
            } else {
                qualityMatchTextView.setText(getResources().getString(R.string.quality_text_view_warning, quality));
            }

            /* Check if score is above or below match threshold and print feedback. */
            String txt;
            if (score >= Parameters.fmrThreshold.getValue()) {
                txt = "Score: " + score + "\nDecision @" + Parameters.fmrThreshold + ": MATCH";
            } else {
                txt = "Score: " + score + "\nDecision @" + Parameters.fmrThreshold + ": NO MATCH";
            }
            decisionTextView.setText(txt);

            /*
             * Stop the capture when a match is performed.
             * It forces the user to restart from the beginning (enroll).
             */
            //startCaptureButton.performClick();

            /* Pause the camera preview on the image that was taken as probe for match. */
            //captureFragment.onPause();
            //isCameraPaused = true;
        });
    }

    private void setupViewElements() {
        /* Initialize the capture fragment. */
        captureFragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
        assert captureFragment != null;
        captureFragment.setProcessor(faceProcessor);

        /* Initialize the other view elements. */
        enrolledFaceView = findViewById(R.id.enrolledFaceImageView);
        enrolledFaceView.setImageResource(R.drawable.empty_avatar);

        decisionTextView = findViewById(R.id.decisionTextView);
        qualityEnrolledTextView = findViewById(R.id.qualityEnrolledTextView);
        qualityMatchTextView = findViewById(R.id.qualityMatchTextView);

        startCaptureButton = findViewById(R.id.startCaptureButton);
        enrollButton = findViewById(R.id.enrollButton);
        matchButton = findViewById(R.id.matchButton);

        /* Set start capture button on click listener. */
        startCaptureButton.setOnClickListener(v -> {
            if (isCameraPaused) {
                isCameraPaused = false;
                captureFragment.onResume();
            }

            if (isCapturing) {
                /* Reset enrolment and match information */
                enrolledFaceView.setImageResource(R.drawable.empty_avatar);
                qualityEnrolledTextView.setText(
                        getResources().getString(R.string.quality_text_view_placeholder));
                qualityMatchTextView.setText(
                        getResources().getString(R.string.quality_text_view_placeholder));
                decisionTextView.setText(
                        getResources().getString(R.string.decision_text_view_placeholder));

                /* Reset the button text before stopping the capture. */
                startCaptureButton.setText(
                        getResources().getString(R.string.start_capture_button_label));

                isCapturing = false;
                isTemplateEnrolled = false;
                captureFragment.stopCapture();
            } else {
                /* Reset the button text before starting the capture. */
                startCaptureButton.setText(
                        getResources().getString(R.string.stop_capture_button_label));

                captureFragment.startCapture();
                isCapturing = true;
            }
            enrollButton.setEnabled(isCapturing);
            matchButton.setEnabled(isCapturing && isTemplateEnrolled);
        });

        /* Set enroll button on click listener. */
        enrollButton.setOnClickListener(v -> {
            captureFragment.requestTemplateEnrolment();
            matchButton.setEnabled(isCapturing);
        });
        enrollButton.setEnabled(isCapturing);

        /* Set match button on click listener. */
        matchButton.setOnClickListener(v -> {
            captureFragment.requestTemplateVerification();
        });
        matchButton.setEnabled(isCapturing);
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