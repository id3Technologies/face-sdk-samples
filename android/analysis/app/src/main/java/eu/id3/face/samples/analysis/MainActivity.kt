package eu.id3.face.samples.analysis

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import eu.id3.face.FaceError
import kotlin.math.absoluteValue
import kotlin.system.exitProcess

private const val LOG_TAG = "MainActivity"

private val okLabel = SpannableString("OK")
private const val colorOk = Color.GREEN
private val notOkLabel = SpannableString("NOT OK")
private const val colorNotOk = Color.RED

/**
 * Simple single view application displaying the camera output.
 * You can enroll a face and match it with another face thanks to the camera.
 */
class MainActivity : AppCompatActivity(), CameraFragment.FaceProcessorListener {
    private var permissionsGranted: Boolean = false

    /** View elements */
    private lateinit var startCaptureButton: Button
    private lateinit var analyzeButton: Button
    private lateinit var portraitFaceView: ImageView
    private lateinit var analysisAnalysisTextView: TextView
    private lateinit var captureFragment: CameraFragment

    /** Face processor containing id3 Face SDK method calls for face detection and face analysis */
    private lateinit var faceProcessor: FaceProcessor

    /** State variables */
    private var isCapturing = false
    private var isCameraPaused = false

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

    override fun onLargestFaceProcessed(analyzeLargestFaceResult: FaceProcessor.AnalyzeLargestFaceResult) {
        runOnUiThread {
            val errorCode = analyzeLargestFaceResult.getErrorCode()
            if (errorCode == 0) {
                val jpegPortraitImageBuffer = analyzeLargestFaceResult.getJpegPortraitImageBuffer()
                val analysisAnalysisText = SpannableStringBuilder()
                var isIcaoCompliant = true

                /** Create a bitmap image for drawing using the portrait image JPEG buffer. */
                val bitmap = BitmapFactory.decodeByteArray(
                    jpegPortraitImageBuffer,
                    0,
                    jpegPortraitImageBuffer.size
                )
                portraitFaceView.setImageBitmap(bitmap)

                /** Check if pose is OK and print feedback. */
                val facePose = analyzeLargestFaceResult.getFacePose()
                if (facePose.yaw.absoluteValue < Parameters.yawMaxThreshold && facePose.pitch.absoluteValue < Parameters.pitchMaxThreshold && facePose.roll.absoluteValue < Parameters.rollMaxThreshold) {
                    okLabel.setSpan(
                        ForegroundColorSpan(colorOk),
                        0,
                        2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    analysisAnalysisText.append(okLabel)
                    analysisAnalysisText.append(" : Correct pose\n")

                    /** Check if there are occlusions and print feedback. */
                    val occlusionScores = analyzeLargestFaceResult.getFaceOcclusionScores()
                    if (occlusionScores.leftEyeOcclusionScore < Parameters.eyeOcclusionMaxThreshold && occlusionScores.rightEyeOcclusionScore < Parameters.eyeOcclusionMaxThreshold && occlusionScores.noseOcclusionScore < Parameters.noseOcclusionMaxThreshold && occlusionScores.mouthOcclusionScore < Parameters.mouthOcclusionMaxThreshold) {
                        okLabel.setSpan(
                            ForegroundColorSpan(colorOk),
                            0,
                            2,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        analysisAnalysisText.append(okLabel)
                        analysisAnalysisText.append(" : No occlusion\n")

                        /** Check attributes and print feedback. */
                        val faceAttributes = analyzeLargestFaceResult.getFaceAttributes()
                        if (faceAttributes.hat < Parameters.hatMaxThreshold) {
                            okLabel.setSpan(
                                ForegroundColorSpan(colorOk),
                                0,
                                2,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(okLabel)
                            analysisAnalysisText.append(" : No hat\n")
                        } else {
                            isIcaoCompliant = false
                            notOkLabel.setSpan(
                                ForegroundColorSpan(colorNotOk),
                                0,
                                6,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(notOkLabel)
                            analysisAnalysisText.append(" : Hat detected\n")
                        }
                        if (faceAttributes.mouthOpen < Parameters.mouthOpenMaxThreshold) {
                            okLabel.setSpan(
                                ForegroundColorSpan(colorOk),
                                0,
                                2,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(okLabel)
                            analysisAnalysisText.append(" : Mouth closed\n")
                        } else {
                            isIcaoCompliant = false
                            notOkLabel.setSpan(
                                ForegroundColorSpan(colorNotOk),
                                0,
                                6,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(notOkLabel)
                            analysisAnalysisText.append(" : Mouth open detected\n")
                        }
                        if (faceAttributes.smile < Parameters.smileMaxThreshold) {
                            okLabel.setSpan(
                                ForegroundColorSpan(colorOk),
                                0,
                                2,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(okLabel)
                            analysisAnalysisText.append(" : No smile\n\n")
                        } else {
                            isIcaoCompliant = false
                            notOkLabel.setSpan(
                                ForegroundColorSpan(colorNotOk),
                                0,
                                6,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            analysisAnalysisText.append(notOkLabel)
                            analysisAnalysisText.append(" : Smile detected\n\n")
                        }
                    } else {
                        isIcaoCompliant = false
                        notOkLabel.setSpan(
                            ForegroundColorSpan(colorNotOk),
                            0,
                            6,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        analysisAnalysisText.append(notOkLabel)
                        analysisAnalysisText.append(" : Occlusion detected\n\n")
                    }
                } else {
                    isIcaoCompliant = false
                    notOkLabel.setSpan(
                        ForegroundColorSpan(colorNotOk),
                        0,
                        6,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    analysisAnalysisText.append(notOkLabel)
                    analysisAnalysisText.append(" : Incorrect pose\n\n")
                }

                if (isIcaoCompliant) {
                    okLabel.setSpan(
                        ForegroundColorSpan(colorOk),
                        0,
                        2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    analysisAnalysisText.append(okLabel)
                    analysisAnalysisText.append(" : The portrait is valid")
                } else {
                    notOkLabel.setSpan(
                        ForegroundColorSpan(colorNotOk),
                        0,
                        6,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    analysisAnalysisText.append(notOkLabel)
                    analysisAnalysisText.append(" : The portrait is not valid")
                }

                analysisAnalysisTextView.text = analysisAnalysisText
            } else {
                if (errorCode == FaceError.INVALID_IOD.value) {
                    Toast.makeText(
                        this,
                        "Face is too far, get closer to the camera.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun setupViewElements() {
        /** Initialize the capture fragment. */
        captureFragment =
            supportFragmentManager.findFragmentById(R.id.cameraFragment) as CameraFragment
        captureFragment.setProcessor(faceProcessor)

        /** Initialize the other view elements. */
        portraitFaceView = findViewById(R.id.portraitFaceView)
        portraitFaceView.setImageResource(R.drawable.empty_avatar)
        analysisAnalysisTextView = findViewById(R.id.analysisAnalysisTextView)

        startCaptureButton = findViewById(R.id.startCaptureButton)
        analyzeButton = findViewById(R.id.analyzeFaceButton)

        /** Set start capture button on click listener. */
        startCaptureButton.setOnClickListener {
            if (isCameraPaused) {
                isCameraPaused = false
                captureFragment.onResume()
            }

            if (isCapturing) {
                /** Reset enrolment and match information */
                portraitFaceView.setImageResource(R.drawable.empty_avatar)

                /** Reset the button text before stopping the capture. */
                startCaptureButton.text =
                    resources.getString(R.string.start_capture_button_label)

                isCapturing = false
                captureFragment.stopCapture()
            } else {
                /** Reset the button text before starting the capture. */
                startCaptureButton.text =
                    resources.getString(R.string.stop_capture_button_label)

                captureFragment.startCapture()
                isCapturing = true
            }
            analyzeButton.isEnabled = isCapturing
        }

        /** Set analysis button on click listener. */
        analyzeButton.setOnClickListener {
            captureFragment.requestProcessing()
        }
        analyzeButton.isEnabled = isCapturing
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

        permissionsGranted = true
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
