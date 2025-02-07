package eu.id3.face.samples.pad

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.system.exitProcess

private const val MAX_PREVIEW_WIDTH = 1920
private const val MAX_PREVIEW_HEIGHT = 1080
private const val REQUEST_CAMERA_PERMISSION = 200

private const val LOG_TAG = "CameraFragment"

/**
 * Fragment that displays the camera output.
 */
class CameraFragment : Fragment() {

    private lateinit var cameraPreview: FaceCapturePreviewView
    private lateinit var boundsView: BoundsView
    private var textureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera(width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    private var cameraId: String = ""
    var cameraDevice: CameraDevice? = null
    private val cameraDeviceStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                //This is called when the camera is open
                cameraDevice = camera
                createCameraPreview()
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice?.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                cameraDevice?.close()
                cameraDevice = null
            }
        }
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var sensorOrientation = 0
    private var displayOrientation = 0
    private var isCapturing = false
    private var needsToProcess = false
    private var needsToEnrollTemplate = false
    private var needsToVerifyTemplate = false
    private var faceProcessor: FaceProcessor? = null
    private var faceProcessorListener: FaceProcessorListener? = null

    /**
     * Links the MainActivity to this fragment so they can communicate.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (faceProcessorListener == null) {
                faceProcessorListener =
                    if (parentFragment != null) parentFragment as FaceProcessorListener
                    else activity as FaceProcessorListener
            }
        } catch (e: ClassCastException) {
            e.printStackTrace()
            exitProcess(-1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    /**
     * Sets up the view when it is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraPreview = view.findViewById(R.id.cameraPreview)
        boundsView = view.findViewById(R.id.boundsView)
        val boundsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        boundsPaint.color = Color.GREEN
        boundsPaint.strokeWidth = 3.toFloat()
        boundsPaint.style = Paint.Style.STROKE
        boundsView.setPaint(boundsPaint)
        boundsView.setWillNotDraw(false)
        boundsView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Sets the paint for bounds drawing.
     */
    fun setBoundsPaint(boundsPaint: Paint) {
        boundsView.setPaint(boundsPaint)
    }

    /**
     * Sets and configures the face processor for stream processing.
     */
    fun setProcessor(processor: FaceProcessor) {
        faceProcessor = processor
    }

    /**
     * Requires the camera background thread to enroll a face template.
     * It triggers the enrolment operation later in the 'onImageAvailableListener'.
     */
    fun requestTemplateEnrolment() {
        needsToEnrollTemplate = true
    }

    /**
     * Requires the camera background thread to verify a face template.
     * It triggers the verification operation later in the 'onImageAvailableListener'.
     */
    fun requestTemplateVerification() {
        needsToVerifyTemplate = true
    }

    /**
     * Requires the camera background thread to process a face image.
     * It triggers the process operation later in the 'onImageAvailableListener'.
     */
    fun requestProcessing() {
        needsToProcess = true
    }

    /**
     * Starts the capture upon request from UI. It allows the processing to occur in the
     * 'onImageAvailableListener'.
     */
    fun startCapture() {
        isCapturing = true
    }

    /**
     * Stops the capture upon request from UI. It clears the bounds view and stops the processing
     * from occurring in the 'onImageAvailableListener'.
     */
    fun stopCapture() {
        boundsView.update(null, 0, 0)
        isCapturing = false
    }

    /**
     * Starts the background camera thread.
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /**
     * Stops the background camera thread.
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Resumes the camera preview.
     */
    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (cameraPreview.isAvailable) {
            openCamera(cameraPreview.width, cameraPreview.height)
        } else {
            cameraPreview.surfaceTextureListener = textureListener
        }
    }

    /**
     * Pauses the camera preview.
     */
    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    /**
     * Creates the camera preview.
     */
    private fun createCameraPreview() {
        try {
            /** Select the view that will display the camera preview and the image reader that will analyze the output */
            val texture: SurfaceTexture = cameraPreview.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            captureRequestBuilder!!.addTarget(imageReader!!.surface)

            val cameraStateCallBack = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (null == cameraDevice) {
                        Log.e(LOG_TAG, "updatePreview error, return")
                        return
                    }
                    cameraCaptureSessions = session
                    updatePreview()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }

            /** Create the capture session (2 ways to do this depending on the Android version installed on the device) */
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                /** Configure the session */
                val sessionConfiguration = SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listOf(
                        OutputConfiguration(surface),
                        OutputConfiguration(imageReader!!.surface)
                    ),
                    this.requireActivity().mainExecutor,
                    cameraStateCallBack
                )

                cameraDevice?.createCaptureSession(sessionConfiguration)
            } else {
                @Suppress("DEPRECATION")
                cameraDevice?.createCaptureSession(
                    listOf(surface, imageReader!!.surface),
                    cameraStateCallBack, null
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Opens the camera in order to display its output.
     */
    private fun openCamera(width: Int, height: Int) {
        Log.v(LOG_TAG, "open camera")
        val manager =
            this.requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            /** Select the cameraId of the front camera */
            for (camId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(camId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cOrientation == Parameters.cameraType) {
                    cameraId = camId
                    break
                }
            }

            /** Get characteristics we need */
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

            /** For still image captures, we use the largest available size. */
            val largest: Size = Collections.max(
                map.getOutputSizes(ImageFormat.JPEG).toMutableList(),
                CompareSizesByArea()
            )

            /** Get the display rotation of the device (2 ways to do this depending on the Android version installed on the device) */
            val displayRotation: Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                displayRotation = requireActivity().display!!.rotation
            } else {
                @Suppress("DEPRECATION")
                displayRotation = requireActivity().windowManager!!.defaultDisplay.rotation
            }

            /** Compare the sensor orientation of the camera to the display rotation to know if we need to swap dimensions. */
            sensorOrientation = characteristics[CameraCharacteristics.SENSOR_ORIENTATION]!!
            var swappedDimensions = false
            when (displayRotation) {
                Surface.ROTATION_0 -> {
                    displayOrientation = 0
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true
                    }
                }
                Surface.ROTATION_180 -> {
                    displayOrientation = 180
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true
                    }
                }
                Surface.ROTATION_90 -> {
                    displayOrientation = 90
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                    displayOrientation = 270
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                }
                Surface.ROTATION_270 -> {
                    displayOrientation = 270
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                }
                else -> Log.e(LOG_TAG, "Display rotation is invalid: $displayRotation")
            }

            /** Get the size of the preview window */
            val previewSize = if (swappedDimensions) {
                chooseOptimalSize(
                    map.getOutputSizes(android.graphics.SurfaceTexture::class.java),
                    height, width, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, largest
                )!!
            } else {
                chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    width, height, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, largest
                )!!
            }

            imageDimension = previewSize

            /** Create the image reader object that will analyze the camera output. */
            imageReader = ImageReader.newInstance(
                previewSize.width, previewSize.height,
                ImageFormat.YUV_420_888, 10
            )
            imageReader!!.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)

            /** Fit the aspect ratio of TextureView to the size of preview we picked. */
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                cameraPreview.setAspectRatio(previewSize.width, previewSize.height)
                boundsView.setAspectRatio(previewSize.width, previewSize.height)
            } else {
                cameraPreview.setAspectRatio(previewSize.height, previewSize.width)
                boundsView.setAspectRatio(previewSize.height, previewSize.width)
            }

            /** Add permission for camera and let user grant the permission. */
            if (ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }

            /** Finally open the camera */
            manager.openCamera(cameraId, cameraDeviceStateCallback, backgroundHandler)
            Log.v(LOG_TAG, "Camera opened successfully")
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Closes the camera.
     */
    private fun closeCamera() {
        if (cameraDevice != null)
            cameraDevice!!.close()
        if (imageReader != null)
            imageReader!!.close()
    }

    /**
     * Starts the camera capture session.
     */
    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(LOG_TAG, "updatePreview error, return")
        }
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            captureRequestBuilder?.let { builder ->
                cameraCaptureSessions?.setRepeatingRequest(
                    builder.build(),
                    null,
                    backgroundHandler
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Processes the image once it is available from the camera reader.
     */
    private val onImageAvailableListener = OnImageAvailableListener { reader ->
        /** Get the image from the reader. */
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener

        if (isCapturing) {
            /** Convert the image from input format to BGR for id3 Face SDK processing functions. */
            val processingImage = FaceProcessor.prepareImageForProcessing(image)

            /** Rotate image if necessary. */
            var rotationDegrees = displayOrientation - sensorOrientation
            if (rotationDegrees < 0) {
                rotationDegrees += 360
            }
            processingImage.rotate(rotationDegrees)

            /**
             * Resize image if larger or higher than 'maxProcessingImageSize'.
             * This operation allows to speed up the face detection process.
             */
            val downscaledImage = processingImage.clone()
            val downscaleRatio = downscaledImage.downscale(Parameters.maxProcessingImageSize)

            /** Track faces. */
            val detectedFace = faceProcessor?.detectLargestFace(downscaledImage)
            if (detectedFace != null) {
                /*
                 * For PAD operations it is better to work on the original image so we rescale the detectedFace.
                 */
                detectedFace.rescale(1 / downscaleRatio)
                val bounds = detectedFace.bounds

                /**
                 * Convert id3.face Rectangle object to android.graphics Rect object and update
                 * bounds view.
                 */
                val rect = Rect(
                    bounds.topLeft.x,
                    bounds.topLeft.y,
                    bounds.bottomRight.x,
                    bounds.bottomRight.y
                )
                boundsView.update(rect, processingImage.width, processingImage.height)

                /** Process frame if requested by UI. */
                needsToProcess = true
                if (needsToProcess) {
                    val analyzeLargestFaceResult =
                        faceProcessor!!.analyzeLargestFace(processingImage, detectedFace)
                    faceProcessorListener!!.onLargestFaceProcessed(analyzeLargestFaceResult)
                    needsToProcess = false
                }
            } else {
                faceProcessor!!.resetPortrait()
                boundsView.update(null, 0, 0)
            }
        } else {
            faceProcessor!!.resetPortrait()
            boundsView.update(null, 0, 0)
        }

        image.close()
    }

    /**
     * Chooses the optimal size for the camera preview.
     */
    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size? {
        /** Collect the supported resolutions that are at least as big as the preview Surface */
        val bigEnough: MutableList<Size> = ArrayList()

        /** Collect the supported resolutions that are smaller than the preview Surface */
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        /**
         * Pick the smallest of those big enough. If there is no one big enough, picks the largest
         * of those not big enough.
         */
        return when {
            bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
            notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
            else -> {
                Log.e(LOG_TAG, "Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }

    /**
     * Internal class that implements a comparator for Size objects, by comparing their area.
     */
    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(o1: Size, o2: Size): Int {
            /** We cast here to ensure the multiplications won't overflow */
            return java.lang.Long.signum(o1.width.toLong() * o1.height - o2.width.toLong() * o2.height)
        }
    }

    /**
     * Interface that allows a communication between the main activity and this processor.
     */
    interface FaceProcessorListener {
        fun onLargestFaceProcessed(analyzeLargestFaceResult: FaceProcessor.AnalyzeLargestFaceResult)
    }
}
