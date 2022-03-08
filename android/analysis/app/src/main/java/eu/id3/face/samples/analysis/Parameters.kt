package eu.id3.face.samples.analysis

/**
 * Contains the parameters needed in the app. Values of these parameters can be changed and should not be seen as "the perfect" values.
 */
internal object Parameters {
    const val detectorThreadCount = 4
    const val detectorConfidenceThreshold = 70

    const val yawMaxThreshold = 10
    const val pitchMaxThreshold = 20
    const val rollMaxThreshold = 10

    const val eyeOcclusionMaxThreshold = 50
    const val noseOcclusionMaxThreshold = 50
    const val mouthOcclusionMaxThreshold = 30

    const val hatMaxThreshold = 10
    const val mouthOpenMaxThreshold = 35
    const val smileMaxThreshold = 75

    const val maxProcessingImageSize = 512
}
