package eu.id3.face.samples.portraitprocessorjava

import eu.id3.face.FaceExpression
import eu.id3.face.FacePose

class PortraitAttributes {
    var age: Int = -1
    var expression: FaceExpression = FaceExpression.UNKNOWN
    var genderMaleScore: Int = -1

    var pose: FacePose = FacePose()
    var leftEyeVisibilityScore: Int = -1
    var rightEyeVisibilityScore: Int = -1
    var mouthVisibilityScore: Int = -1
    var noseVisibilityScore: Int = -1
    var glassesVisibilityScore: Int = -1
    var hatScore: Int = -1
    var mouthOpenScore: Int = -1
    var smileScore: Int = -1

    var qualityScore: Int = -1
}