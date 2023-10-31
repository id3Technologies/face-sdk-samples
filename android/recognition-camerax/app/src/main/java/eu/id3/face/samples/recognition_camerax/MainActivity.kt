package eu.id3.face.samples.recognition_camerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import eu.id3.face.*
import eu.id3.face.samples.recognition_camerax.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private var isCapturing = false

    private val detectorLock = Object()
    private val encoderLock = Object()

    private lateinit var faceDetector: FaceDetector
    private lateinit var faceEncoder: FaceEncoder
    private lateinit var faceMatcher: FaceMatcher
    private lateinit var lastDetectedFace: DetectedFace
    private lateinit var lastDetectedImage: Image
    private lateinit var enrolledTemplate: FaceTemplate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.enrolledFaceImageView.setImageResource(R.drawable.empty_avatar)

        /**
         * Register the SDK License. It must be done before calling any SDK function.
         * Please go to the Credentials.kt to fill in your license information.
         */
        val isLicenseOk =
            Credentials.registerSdkLicense(filesDir!!.absolutePath + "/id3FaceLicense.lic")
        if (!isLicenseOk) {
            exitProcess(-1)
        }

        /**
         * Load models and initialize Face Detector and Face Encoder modules
         */
        loadBiometricModules()

        /**
         * If required permission are granted, start the camera, otherwise request them
         */
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        /**
         * Set the application logic
         */
        viewBinding.startCaptureButton.setOnClickListener { startCaptureClick() }
        viewBinding.enrollButton.setOnClickListener { enrollClick() }
        viewBinding.matchButton.setOnClickListener { matchClick() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun loadBiometricModules() {
        /**
         * Load a face detector.
         * First load the model from the Assets and then initialize the FaceDetector object.
         *
         * The FaceDetector model loading and initialization take some time so the object is initialized
         * once and will be reused during capture.
         *
         * Please note that it is mandatory to set which model will be used in the FaceDetector.
         */
        FaceLibrary.loadModelBuffer(
            assets.open("models/face_detector_v3b.id3nn").readBytes(),
            FaceModel.FACE_DETECTOR_3B, ProcessingUnit.CPU
        )
        faceDetector = FaceDetector()
        faceDetector.model = FaceModel.FACE_DETECTOR_3B

        /**
         * Load a face encoder.
         * First load the model from the Assets and then initialize the FaceEncoder object.
         *
         * The FaceEncoder model loading and initialization take some time so the object is initialized
         * once and will be reused during capture.
         *
         * Please note that it is mandatory to set which model will be used in the FaceEncoder.
         */
        FaceLibrary.loadModelBuffer(
            assets.open("models/face_encoder_v9b.id3nn").readBytes(),
            FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU
        )

        /**
         * To be able to retrieve template encoding quality it is mandatory to load the associated model
         */
        FaceLibrary.loadModelBuffer(
            assets.open("models/face_encoding_quality_estimator_v3a.id3nn").readBytes(),
            FaceModel.FACE_ENCODING_QUALITY_ESTIMATOR_3A, ProcessingUnit.CPU
        )
        faceEncoder = FaceEncoder()
        faceEncoder.model = FaceModel.FACE_ENCODER_9B

        /**
         * Face matcher module does not require any model
         */
        faceMatcher = FaceMatcher()
    }

    /**
     * This sample uses the CameraX Jetpack library: https://developer.android.com/training/camerax
     *
     * This library allows a quick and convenient way to:
     * - display full camera preview (Camerax Preview and PreviewView)
     * - access pixel data in a lower resolution (CameraX ImageAnalysis)
     *
     * This function will setup the camera and the two feeds.
     */
    private fun startCamera() {
        val cameraProviderF = ProcessCameraProvider.getInstance(this)
        cameraProviderF.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderF.get()

            /**
             * Create Preview feed to the viewFinger PreviewView declared in the activity_main.xml
             * layout. The preview resolution will match the view size.
             */
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            /**
             * Create an ImageAnalysis feed plugged in our DetectorAnalyzer class (see below)
             */
            val detectorAnalyser = ImageAnalysis.Builder()
                // Enable automatic rotation management
                .setOutputImageRotationEnabled(true)
                // we do not care of dropping frames if detection takes too long (this will not affect preview)
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                // face detection do not require high resolution so the ImageAnalysis feed has a custom size
                .setTargetResolution(Parameters.detectorWorkingResolution)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, DetectorAnalyzer());
                }
            cameraProvider.unbindAll()
            // bind the two feeds to the chosen camera
            cameraProvider.bindToLifecycle(
                this,
                Parameters.cameraType,
                preview,
                detectorAnalyser
            )
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * This class will receive the ImageProxy frames and perform face detection
     */
    inner class DetectorAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            // Synchronisation with bounds drawing
            synchronized(detectorLock) {
                /**
                 *  The isCapturing boolean is managed by the "Start Capture" Button.
                 *  We capture is off we skip all frames
                 */
                if (isCapturing) {
                    // Convert the YUV ImageProxy to the id3Face.Image class
                    var faceImage = faceImageFromImageProxy(image);

                    /**
                     * The face detector can detect faces in the [16px-512px] range thus it can run
                     * on low resolution images.
                     * For low-end devices if the detection takes too much time it is possible to
                     * downscale the image for this step. The resulting detectedFace will then have to
                     * been rescaled to the original resolution as it is recommended to perform encoding
                     * on the original image.
                     */
                    var lowResScale = 1f
                    var faceImageBackup: Image? = null
                    if (Parameters.lowResDetection) {
                        // saving original image
                        faceImageBackup = faceImage.clone()
                        // heavily downscaling image to be detected
                        lowResScale = faceImage.downscale(Parameters.lowResDetectionMaxSize)
                    }
                    val detectedFaceList = faceDetector.detectFaces(faceImage);
                    if (detectedFaceList.count > 0) {
                        val detectedFace = detectedFaceList.largestFace
                        if (Parameters.lowResDetection) {
                            // if downscaling was applied then we upscale the detectedFace object
                            // to the original resolution
                            detectedFace.rescale(1 / lowResScale)
                            faceImage = faceImageBackup!!
                        }

                        // updating face bounds in the overlay
                        viewBinding.faceOverlay.setFaceBounds(
                            detectedFace.bounds,
                            faceImage.width,
                            faceImage.height,
                            Parameters.cameraType
                        )

                        // Synchronisation with face encoding
                        synchronized(encoderLock) {
                            lastDetectedFace = detectedFace.clone()
                            lastDetectedImage = faceImage.clone()
                        }
                    } else {
                        // when no face has been detected we erase the bounds of the overlay
                        viewBinding.faceOverlay.setFaceBounds(null, 0, 0, Parameters.cameraType)
                    }
                }
            }
            image.close()
        }

        /**
         * Retrieving id3Face.Image object from ImageProxy planes buffer
         */
        private fun faceImageFromImageProxy(src: ImageProxy): eu.id3.face.Image {
            val plane0 = src.planes[0].buffer.asReadOnlyBuffer()
            val p0 = ByteArray(plane0.capacity())
            val plane1 = src.planes[1].buffer.asReadOnlyBuffer()
            val p1 = ByteArray(plane1.capacity())
            val plane2 = src.planes[2].buffer.asReadOnlyBuffer()
            val p2 = ByteArray(plane2.capacity())

            plane0.get(p0)
            plane1.get(p1)
            plane2.get(p2)

            return eu.id3.face.Image.fromYuvPlanes(
                p0, p1, p2,
                src.width,
                src.height,
                src.planes[1].pixelStride,
                src.planes[1].rowStride,
                PixelFormat.BGR_24_BITS
            )
        }
    }

    /**
     * StartCaptureClick() : responsible to enable or disable the ImageProxy processing in the
     * DetectorAnalyser
     */
    private fun startCaptureClick() {
        if (isCapturing) {
            synchronized(detectorLock) {
                isCapturing = false
            }
            viewBinding.startCaptureButton.text = getString(R.string.start_capture_button_label)
            viewBinding.enrollButton.isEnabled = false;
            viewBinding.matchButton.isEnabled = false
            viewBinding.faceOverlay.setFaceBounds(null, 0, 0, Parameters.cameraType)
        } else {
            synchronized(detectorLock) {
                isCapturing = true
            }
            viewBinding.startCaptureButton.text = getString(R.string.stop_capture_button_label)
            viewBinding.enrollButton.isEnabled = true;
        }
    }

    /**
     * enrollClick() : will encode a face template on the last detected face and extract a portrait
     * to display.
     */
    private fun enrollClick() {
        synchronized(encoderLock) {
            enrolledTemplate = faceEncoder.createTemplate(lastDetectedImage, lastDetectedFace)
            val quality = faceEncoder.computeQuality(lastDetectedImage, lastDetectedFace)

            // Update enrollee display with a well cropped portrait view
            val portraitBounds = lastDetectedFace.getPortraitBounds(0.25f, 0.45f, 1.33f)
            val portraitImage = lastDetectedImage.extractRoi(portraitBounds)
            // when using camera in selfie mode, portrait must be horizontally flipped
            if (Parameters.cameraType == CameraSelector.DEFAULT_FRONT_CAMERA) {
                portraitImage.flip(true, false)
            }
            val jpegPortraitImageBuffer = portraitImage.toBuffer(ImageFormat.JPEG, 75.0f)
            val bitmap = BitmapFactory.decodeByteArray(
                jpegPortraitImageBuffer,
                0,
                jpegPortraitImageBuffer.size
            )
            viewBinding.enrolledFaceImageView.setImageBitmap(bitmap)

            // Update quality display
            if (quality >= Parameters.encodingQualityWarningThreshold) {
                viewBinding.qualityEnrolledTextView.text = resources.getString(
                    R.string.quality_text_view_value,
                    quality
                )
            } else {
                viewBinding.qualityEnrolledTextView.text = resources.getString(
                    R.string.quality_text_view_warning,
                    quality
                )
            }
            viewBinding.matchButton.isEnabled = true
        }
    }

    /**
     * enrollClick() : will encode a face template on the last detected face and match it against
     * the previously enrolled template.
     */
    private fun matchClick() {
        synchronized(encoderLock) {
            val candidate = faceEncoder.createTemplate(lastDetectedImage, lastDetectedFace)
            val score = faceMatcher.compareTemplates(enrolledTemplate, candidate)

            /** Check if score is above or below match threshold and print feedback. */
            val txt = if (score >= Parameters.fmrThreshold.value) {
                "Score: $score\nDecision @ ${Parameters.fmrThreshold}: MATCH"
            } else {
                "Score: $score\nDecision @ ${Parameters.fmrThreshold}: NO MATCH"
            }
            viewBinding.decisionTextView.text = txt
        }
    }

    /**
     * Following function request and check permissions status
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "id3FaceCameraXReco"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET
            ).toTypedArray()
    }
}
