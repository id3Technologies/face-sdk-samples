import Foundation
import id3Face

/**
 * Contains the parameters needed in the app. Values of these parameters can be changed and should not be seen as "the perfect" values.
 */
public class Parameters {
    public static let detectorThreadCount: Int32 = 4
    public static let detectorConfidenceThreshold: Int32 = 70
    
    public static let encoderThreadCount: Int32 = 2
    public static let qualityThreshold: Int32 = 40
    
    public static let FMRThresholdLabel = "10000"
    public static let decisionScoreThreshold: UInt32 = FaceMatcherThreshold.fmr10000.rawValue.rawValue
    
    // maximum to which of the frame will be resized to
    // lowering this value will lead to even faster detection but may result in lower accuracy of the detected bounds
    public static let detectionMaxSize: Int32 = 256
}
