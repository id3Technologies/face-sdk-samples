package eu.id3.face.samples.recognition

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess

private const val LOG_TAG = "MainActivity"

/**
 * Simple single view application displaying the camera output.
 * You can enroll a face and match it with another face thanks to the camera.
 */
class MainActivity : AppCompatActivity(), CameraFragment.FaceProcessorListener {
    /** View elements */
    private lateinit var startCaptureButton: Button
    private lateinit var enrollButton: Button
    private lateinit var matchButton: Button
    private lateinit var enrolledFaceView: ImageView
    private lateinit var qualityEnrolledTextView: TextView
    private lateinit var qualityMatchTextView: TextView
    private lateinit var decisionTextView: TextView
    private lateinit var captureFragment: CameraFragment

    /** Face processor containing id3 Face SDK method calls for face detection and face recognition */
    private lateinit var faceProcessor: FaceProcessor

    /** State variables */
    private var isCapturing = false
    private var isCameraPaused = false
    private var isTemplateEnrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Verify that the application has the permissions requested by the SDK. */
        checkSdkPermissions()
        /**
         * Register the SDK License. It must be done before calling any SDK function.
         * Please go to the Credentials.kt to fill in your license information.
         */
        val isLicenseOk =
            Credentials.registerSdkLicense(filesDir!!.absolutePath + "/id3FaceLicense.lic")
        if (!isLicenseOk) {
            exitProcess(-1)
        }
        /** Init the face processor. */
        faceProcessor = FaceProcessor(applicationContext)
        /** Setup the view elements. */
        setupViewElements()
    }

    override fun onLargestFaceEnrolled(enrollLargestFaceResult: FaceProcessor.EnrollLargestFaceResult) {
        runOnUiThread {
            val jpegPortraitImageBuffer = enrollLargestFaceResult.getJpegPortraitImageBuffer()
            val quality = enrollLargestFaceResult.getQuality()

            /** Create a bitmap image for drawing using the portrait image JPEG buffer. */
            val bitmap = BitmapFactory.decodeByteArray(
                jpegPortraitImageBuffer,
                0,
                jpegPortraitImageBuffer.size
            )
            enrolledFaceView.setImageBitmap(bitmap)

            /** Check if quality is high enough for matching and print feedback. */
            if (quality >= Parameters.encodingQualityThreshold) {
                qualityEnrolledTextView.text = resources.getString(
                    R.string.quality_text_view_value,
                    quality
                )
            } else {
                qualityEnrolledTextView.text = resources.getString(
                    R.string.quality_text_view_warning,
                    quality
                )
            }

            /** Enable match button when a template is enrolled. */
            matchButton.isEnabled = true
        }
    }

    override fun onLargestFaceVerified(verifyLargestFaceResult: FaceProcessor.VerifyLargestFaceResult) {
        runOnUiThread {
            val quality = verifyLargestFaceResult.getQuality()
            val score = verifyLargestFaceResult.getScore()

            /** Check if quality is high enough for matching and print feedback. */
            if (quality >= Parameters.encodingQualityThreshold) {
                qualityMatchTextView.text = resources.getString(
                    R.string.quality_text_view_value,
                    quality
                )
            } else {
                qualityMatchTextView.text = resources.getString(
                    R.string.quality_text_view_warning,
                    quality
                )
            }

            /** Check if score is above or below match threshold and print feedback. */
            val txt = if (score >= Parameters.fmrThreshold.value) {
                "Score: $score\nDecision @ ${Parameters.fmrThreshold}: MATCH"
            } else {
                "Score: $score\nDecision @ ${Parameters.fmrThreshold}: NO MATCH"
            }
            decisionTextView.text = txt

            /**
             * Stop the capture when a match is performed.
             * It forces the user to restart from the beginning (enroll).
             */
            //startCaptureButton.performClick()

            /** Pause the camera preview on the image that was taken as probe for match. */
            //captureFragment.onPause()
            //isCameraPaused = true
        }
    }

    private fun setupViewElements() {
        /** Initialize the capture fragment. */
        captureFragment =
            supportFragmentManager.findFragmentById(R.id.cameraFragment) as CameraFragment
        captureFragment.setProcessor(faceProcessor)

        /** Initialize the other view elements. */
        enrolledFaceView = findViewById(R.id.enrolledFaceImageView)
        enrolledFaceView.setImageResource(R.drawable.empty_avatar)

        decisionTextView = findViewById(R.id.decisionTextView)
        qualityEnrolledTextView = findViewById(R.id.qualityEnrolledTextView)
        qualityMatchTextView = findViewById(R.id.qualityMatchTextView)

        startCaptureButton = findViewById(R.id.startCaptureButton)
        enrollButton = findViewById(R.id.enrollButton)
        matchButton = findViewById(R.id.matchButton)

        /** Set start capture button on click listener. */
        startCaptureButton.setOnClickListener {
            if (isCameraPaused) {
                isCameraPaused = false
                captureFragment.onResume()
            }

            if (isCapturing) {
                /** Reset enrolment and match information */
                enrolledFaceView.setImageResource(R.drawable.empty_avatar)
                qualityEnrolledTextView.text =
                    resources.getString(R.string.quality_text_view_placeholder)
                qualityMatchTextView.text =
                    resources.getString(R.string.quality_text_view_placeholder)
                decisionTextView.text =
                    resources.getString(R.string.decision_text_view_placeholder)

                /** Reset the button text before stopping the capture. */
                startCaptureButton.text =
                    resources.getString(R.string.start_capture_button_label)

                isCapturing = false
                isTemplateEnrolled = false
                captureFragment.stopCapture()
            } else {
                /** Reset the button text before starting the capture. */
                startCaptureButton.text =
                    resources.getString(R.string.stop_capture_button_label)

                captureFragment.startCapture()
                isCapturing = true
            }
            enrollButton.isEnabled = isCapturing
            matchButton.isEnabled = isCapturing && isTemplateEnrolled
        }

        /** Set enroll button on click listener. */
        enrollButton.setOnClickListener {
            captureFragment.requestTemplateEnrolment()
            matchButton.isEnabled = isCapturing
        }
        enrollButton.isEnabled = isCapturing

        /** Set match button on click listener. */
        matchButton.setOnClickListener {
            captureFragment.requestTemplateVerification()
        }
        matchButton.isEnabled = isCapturing
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
    private fun checkSdkPermissions() {
        val requestedPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )
        if (!hasRequestedPermissions(requestedPermissions)) {
            ActivityCompat.requestPermissions(this, requestedPermissions, 0)
            while (!hasRequestedPermissions(requestedPermissions)) {
                Log.v(LOG_TAG, "Waiting for user to accept permissions")
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun hasRequestedPermissions(requested_permissions: Array<String>): Boolean {
        for (permission in requested_permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}
