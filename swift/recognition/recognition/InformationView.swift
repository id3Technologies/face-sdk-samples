import Foundation
import SwiftUI
import id3Face

/**
 * The object containing all the information we have to share between the two sub-views
 */
class Information: ObservableObject {
    @Published public var isCapturing = false
    @Published public var lastDetectedImage: id3Face.Image? = nil
    @Published public var lastDetectedFace: DetectedFace? = nil
}

/**
 * This view contains all the information that we want to display
 */
struct InformationView: View {
    @State private var captureButtonLabel = "Start Capture"
    @ObservedObject public var info = Information()
    @State private var enrollTemplate: FaceTemplate? = nil
    @State private var score: Int32 = 0
    @State private var decision = ""
    
    private var faceDetector: FaceDetector? = nil
    private var faceAnalyser: FaceAnalyser? = nil
    private var faceEncoder: FaceEncoder? = nil
    
    @State private var imgEnroll: UIImage = UIImage(imageLiteralResourceName: "empty-avatar")
    
    @State private var qualityEncodee: Int32? = nil
    @State private var qualityMatch: Int32? = nil
    
    public mutating func setBioEngines(faceDetector: FaceDetector, faceAnalyser: FaceAnalyser, faceEncoder: FaceEncoder) {
        self.faceDetector = faceDetector
        self.faceAnalyser = faceAnalyser
        self.faceEncoder = faceEncoder
    }
    
    var body: some View {
        VStack(spacing: 15) {
            VStack {
                if let qltyMatch = qualityMatch {
                    Text("Quality : " + String(qltyMatch))
                    if qltyMatch < Parameters.qualityThreshold {
                        Text("Warning : the quality of the picture is low")
                    }
                } else {
                    Text("Quality : ")
                }
            }
            
            HStack {
                Button(action: actionCaptureButton) {
                    Text(captureButtonLabel).textButtonStyle(isDisabled: false)
                }

                Button(action: actionEnrollButton) {
                    Text("Enroll").textButtonStyle(isDisabled: !info.isCapturing)
                }
                .disabled(!info.isCapturing)
                
                Button(action: actionMatchButton) {
                    Text("Match").textButtonStyle(isDisabled: !info.isCapturing || enrollTemplate == nil)
                }
                .disabled(!info.isCapturing || enrollTemplate == nil)
            }
            
            VStack {
                SwiftUI.Image(uiImage: imgEnroll)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 150, height: 150, alignment: .center)
            
                Text("Enrollee")
                if let qltyEncodee = qualityEncodee {
                    Text("Quality : " + String(qltyEncodee))
                    if qltyEncodee < Parameters.qualityThreshold {
                        Text("Warning : the quality of the picture is low")
                    }
                } else {
                    Text("Quality : ")
                }
            }
            
            VStack {
                Text("Score : " + String(score))
                Text("Decision @ FMR " + Parameters.FMRThresholdLabel + " : " + decision)
            }
        }
    }
    
    /**
     * The action that starts/stops the capture when you click on the capture button
     */
    private func actionCaptureButton() {
        NSLog(captureButtonLabel)
        if info.isCapturing {
            captureButtonLabel = "Start Capture"
        } else {
            // Reset the parameters before starting the capture
            captureButtonLabel = "Stop Capture"
            imgEnroll = UIImage(imageLiteralResourceName: "empty-avatar")
            enrollTemplate = nil
            qualityEncodee = nil
            qualityMatch = nil
            score = 0
            decision = ""
        }
        info.isCapturing.toggle()
    }
    
    
    /**
     * The action that encodes a template on the last detected face and displays it when you click on the enroll button
     */
    private func actionEnrollButton() {
        if (info.isCapturing) {
            NSLog("Enroll process started")
            do {
                // First we display the detected face in our UI using default portait bounds parameters
                let croppingBounds = try info.lastDetectedFace!.getPortraitBounds(eyeImageWidthRatio: 0.25,
                                                                                  eyeImageHeightRatio: 0.45,
                                                                                  imageRatio: 1.33)
                let cropped = try info.lastDetectedImage?.extractRoi(bounds: croppingBounds)
                // to display a frontal camera image it is convenient to horizontally flip the image
                try cropped?.flip(flipHorizontally: true, flipVertically: false)
                
                imgEnroll = try faceImagetoUIImage(image: cropped!, imageFormat: ImageFormat.jpeg, compressionLevel: 75.0)
                
                // Then a reference face template is encoded using the previously initialized faceEncoder
                enrollTemplate = try faceEncoder!.createTemplate(image: info.lastDetectedImage!, detectedFace: info.lastDetectedFace!)
                // Quality can be computed in the same manner using the faceEncoder module
                qualityEncodee = try faceEncoder?.computeQuality(image: info.lastDetectedImage!, detectedFace: info.lastDetectedFace!)
            } catch let error as FaceException {
                NSLog("Error while enrolling the last detected face : " + error.getMessage())
            } catch {
                NSLog("Error while enrolling the last detected face : Unknown error")
            }
            NSLog("Enroll process end")
        }
    }
        
    /**
     * The action that encode a candidate face template from the last detected face and compares it to the enrolled template.
     */
    private func actionMatchButton() {
        if info.isCapturing, let enrollFaceTemplate = enrollTemplate {
            do {
                NSLog("Match process started")
                // Create the candidate template in order to be compared to the enrolled template
                let candidate = try faceEncoder!.createTemplate(image: info.lastDetectedImage!, detectedFace: info.lastDetectedFace!)

                let faceMatcher = try FaceMatcher()
                score = try faceMatcher.compareTemplates(reference: enrollFaceTemplate, probe: candidate)
                if score >= Parameters.decisionScoreThreshold {
                    decision = "MATCH"
                } else {
                    decision = "NO MATCH"
                }
                NSLog("Match process end")
            } catch let error as FaceException {
                NSLog("Error while matching the enrolled face with the last detected face : " + error.getMessage())
            } catch {
                NSLog("Error while matching the enrolled face with the last detected face : Unknown error")
            }
        }
    }
    
    func faceImagetoUIImage(image: id3Face.Image, imageFormat: ImageFormat, compressionLevel: Float) throws -> UIImage {
        let imbuff = try image.toBuffer(imageFormat: ImageFormat.jpeg, compressionLevel: 75.0)
        let dat = Data(imbuff)
        let im = UIImage(data: dat)
        return im!
    }

}

// Simple style for the buttons of the sample
struct TextButton: ViewModifier {
    private let isDisabled: Bool
    
    public init(isDisabled: Bool) {
        self.isDisabled = isDisabled
    }
    
    func body(content: Content) -> some View {
        content
            .padding(10)
            .background(isDisabled ? Color(red: Double(214) / 255, green: Double(219) / 255, blue: Double(223) / 255) : Color.gray)
            .foregroundColor(.white)
            .font(.system(size: 25))
    }
}

extension View {
    func textButtonStyle(isDisabled: Bool) -> some View {
        self.modifier(TextButton(isDisabled: isDisabled))
    }
}
