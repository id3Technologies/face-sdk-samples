package eu.id3.face.samples.trackingjava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    /**
     * View elements
     */
    private Button startCaptureButton;
    private CameraFragment captureFragment = new CameraFragment();

    /**
     * Face processor containing id3 Face SDK method calls for face tracking
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

    private void setupViewElements() {
        /* Initialize the capture fragment. */
        captureFragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
        assert captureFragment != null;
        captureFragment.setProcessor(faceProcessor);

        /* Initialize the other view elements. */
        startCaptureButton = findViewById(R.id.startCaptureButton);

        /* Set start capture button on click listener. */
        startCaptureButton.setOnClickListener(v -> {
            if (isCameraPaused) {
                isCameraPaused = false;
                captureFragment.onResume();
            }

            if (isCapturing) {
                /* Reset the button text before stopping the capture. */
                startCaptureButton.setText(
                        getResources().getString(R.string.start_capture_button_label));

                isCapturing = false;
                captureFragment.stopCapture();
                // reset the tracked face list
                faceProcessor.resetTrackedFaceList();
            } else {
                /* Reset the button text before starting the capture. */
                startCaptureButton.setText(
                        getResources().getString(R.string.stop_capture_button_label));

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