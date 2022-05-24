package eu.id3.face.samples.pad

import android.hardware.camera2.CameraCharacteristics

/**
 * Contains the parameters needed in the app. Values of these parameters can be changed and should not be seen as "the perfect" values.
 */
internal object Parameters {
    const val detectorThreadCount = 4
    const val detectorConfidenceThreshold = 70
    const val blurScoreMaxThreshold = 20
    const val colorScoreThreshold = 90
    const val colorScoreConfidenceThreshold = 70
    const val maxProcessingImageSize = 512
    const val cameraType = CameraCharacteristics.LENS_FACING_FRONT
}
