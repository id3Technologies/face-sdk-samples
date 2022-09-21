package eu.id3.face.samples.recognition_camerax

import android.util.Size
import androidx.camera.core.CameraSelector
import eu.id3.face.FaceMatcherThreshold

/**
 * Contains the parameters needed in the app. Values of these parameters can be changed according to
 * your needs and should not be seen as "the perfect" values.
 */
internal object Parameters {
    /**
     * The face detector can detect faces in the [16px-512px] range so the requested resolution should
     * not be too high. For typical "selfie" applications a 480x640 size is more than sufficient.
     */
    val detectorWorkingResolution = Size(480, 640)

    /**
     * For low end devices we can downscale the image even more, however this downscaling will only
     * apply for detection, original image will be kept for encoding and display.
     */
    const val lowResDetection = false
    const val lowResDetectionMaxSize = 250

    const val encodingQualityWarningThreshold = 40
    val fmrThreshold = FaceMatcherThreshold.FMR10000
    val cameraType = CameraSelector.DEFAULT_FRONT_CAMERA
}
