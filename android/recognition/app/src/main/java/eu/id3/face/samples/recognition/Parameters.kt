package eu.id3.face.samples.recognition

import eu.id3.face.*

/**
 * Contains the parameters needed in the app. Values of these parameters can be changed and should not be seen as "the perfect" values.
 */
internal object Parameters {
    const val detectorThreadCount = 4
    const val detectorConfidenceThreshold = 70
    const val encoderThreadCount = 4
    const val encodingQualityThreshold = 40
    val fmrThreshold = FaceMatcherThreshold.FMR10000
    const val maxProcessingImageSize = 512
}
