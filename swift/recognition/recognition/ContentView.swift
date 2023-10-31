//
//  ContentView.swift
//  captureSampleIOS
//
//  Created by Simon Eudeline on 01/06/2021.
//

import Foundation
import SwiftUI
import AVFoundation
import id3Face

/**
 * Simple single view application displaying the camera output.
 * You can enroll a face and match it with another face thanks to the camera.
 */
struct ContentView: View {
    // Path for the license
    private let SAVED_LICENSE_DIR = "/id3/id3license/"
    private let SAVED_LICENSE_FILE: String = "id3license_recognitionsampleface_" + Credentials.getLicenseSerialKey()[0..<4] + ".lic"
    
    // Biometric attributes
    private var faceDetector: FaceDetector? = nil
    private var faceAnalyser: FaceAnalyser? = nil
    private var faceEncoder: FaceEncoder? = nil

    private var informationView = InformationView()
    
    @State private var deviceOrientation = UIDeviceOrientation.unknown
    
    init() {
        // License check
        registerSdkLicense()
        
        /*
            The usage of most of the SDK modules require to load model files.
            Those model files are not included in the sample and must be retrieved from id3 cloud (see SDK reame file) and added as Assets of the sample application.
            This sample requires the following models:
                - face_detector_v3b.id3nn
                - face_encoding_quality_estimator_v3a.id3nn
                - face_encoder_v9b.id3nn
         */
        do {
            /* Initialize the face detector :
                - first we load the model from the Assets
                - then we initialize the FaceDetector object
            */
            let nsdata_detector = NSDataAsset(name: "face_detector_v3b")!.data
            try FaceLibrary.loadModelBuffer(modelBuffer: [UInt8](nsdata_detector),
                                            faceModel: FaceModel.faceDetector4B,
                                            processingUnit: ProcessingUnit.cpu)
            
            faceDetector = try FaceDetector()
            try faceDetector!.setModel(model: FaceModel.faceDetector4B)
            try faceDetector!.setThreadCount(threadCount: Parameters.detectorThreadCount)
            
            // Load the quality analyser in the same way
            let nsdata_quality = NSDataAsset(name: "face_encoding_quality_estimator_v3a")!.data
            try FaceLibrary.loadModelBuffer(modelBuffer: [UInt8](nsdata_quality),
                                            faceModel: FaceModel.faceEncodingQualityEstimator3A,
                                            processingUnit: ProcessingUnit.cpu)
            faceAnalyser = try FaceAnalyser()

            // Load the face encoder in the same way
            let nsdata_encoder = NSDataAsset(name: "face_encoder_v9b")!.data
            try FaceLibrary.loadModelBuffer(modelBuffer: [UInt8](nsdata_encoder),
                                            faceModel: FaceModel.faceEncoder9B,
                                            processingUnit: ProcessingUnit.cpu)
            faceEncoder = try FaceEncoder()
            try faceEncoder!.setModel(model: FaceModel.faceEncoder9B)
            try faceEncoder!.setThreadCount(threadCount: Parameters.encoderThreadCount)
            
            // Give the biometric attributes to the subview
            informationView.setBioEngines(faceDetector: faceDetector!, faceAnalyser: faceAnalyser!, faceEncoder: faceEncoder!)
            
        } catch let error as FaceException {
            NSLog("Error while loading the models : " + error.getMessage())
        } catch {
            NSLog("Error while loading the models : Unknown error")
        }
    }
    
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 15) {
                FaceCapturePreviewView(info: informationView.info, detector: faceDetector!, deviceOrientation: $deviceOrientation)
                    .frame(width: 256, height: 256, alignment: .top)
                
                informationView
            }
            .onRotate { newOrientation in
                // Handle the rotation of the device
                if newOrientation != UIDeviceOrientation.portraitUpsideDown && newOrientation != UIDeviceOrientation.faceDown && newOrientation != UIDeviceOrientation.faceUp {
                    deviceOrientation = newOrientation
                }
            }
        }
    }
    
    
    /**
     * The id3 Face Toolkit needs a valid license to work.
     *
     * This SDK provides a specific API to directly download license files from
     * the developed applications.
     *
     * This function will try to load a license file from external storage and register it.
     *
     * If the license file do not exist it will try to download a new license using the specified
     * serial key.
     *
     * This function check the validity of the license
     */
    private func registerSdkLicense() {
        do {
            NSLog("Beginning register...\n")
            
            let hardwareCode: String = try FaceLicense.getHostHardwareCode(hardwareCodeType: LicenseHardwareCodeType.iOS)

            // Check if the license is registered in the device
            let documentsDirectory = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
            let docURL = URL(string: documentsDirectory)!
            let licenseURL: URL = docURL.appendingPathComponent(SAVED_LICENSE_DIR).appendingPathComponent(SAVED_LICENSE_FILE)
            let licenseAlreadySaved = FileManager.default.fileExists(atPath: licenseURL.path)

            if licenseAlreadySaved {
                NSLog("The license is saved in this device")
                
                // The license has been founded, now we check if this license is valid
                try FaceLicense.checkLicense(licensePath: licenseURL.path)
                NSLog("Check License: OK !")
            
            } else {
                // The license has not been found
                let hardwareCode: String = try FaceLicense.getHostHardwareCode(hardwareCodeType: LicenseHardwareCodeType.iOS)
                
                NSLog("Hardware code: " + hardwareCode + "  OK !\n")
                let licBuff = try FaceLicense.activateBuffer(hardwareCode: hardwareCode,
                                           login: Credentials.getAccountLogin(),
                                           password: Credentials.getAccountPassword(),
                                           productReference: Credentials.getPackageReference(),
                                           commentary: "Activated from iOS Recognition sample")
                
                saveLicense(licenseBuffer: licBuff)
                NSLog("License saved: OK !\n")
                
                // Finally checking the license file to enable SDK functions
                try FaceLicense.checkLicense(licensePath: licenseURL.path)
                NSLog("Check License: OK !")
            }
        } catch let error as FaceException {
            NSLog("Error during license check : " + error.getMessage())
        } catch {
            NSLog("Error during license check : Unknown error")
        }
    }
    
    
    /**
     * Save the license buffer in the device
     * - Parameter licenseBuffer: the license buffer we want to save
     */
    private func saveLicense(licenseBuffer: [UInt8]) {
        do {
            // We check that the folder of the containing the license already exists
            let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
            let documentsDirectory = paths[0]
            let docURL = URL(string: documentsDirectory)!
            let dataPath: URL = docURL.appendingPathComponent(SAVED_LICENSE_DIR)
            
            if !FileManager.default.fileExists(atPath: dataPath.path) {
                // If not we try to create this folder
                do {
                    try FileManager.default.createDirectory(atPath: dataPath.path, withIntermediateDirectories: true, attributes: nil)
                    NSLog("Folder Created")
                } catch {
                    NSLog(error.localizedDescription)
                }
            } else {
                NSLog("The folder already exists.")
            }
            
            // Then we save the file in the correct folder
            let filePath = dataPath.appendingPathComponent(SAVED_LICENSE_FILE)
            let data = Data(licenseBuffer)
            
            FileManager.default.createFile(atPath: filePath.path, contents: data, attributes: nil)
            NSLog("License saved successfully !")
        }
    }
}


// a custom view modifier to track rotation and
// call our action
struct DeviceRotationViewModifier: ViewModifier {
    let action: (UIDeviceOrientation) -> Void

    func body(content: Content) -> some View {
        content
            .onAppear()
            .onReceive(NotificationCenter.default.publisher(for: UIDevice.orientationDidChangeNotification)) { _ in
                action(UIDevice.current.orientation)
            }
    }
}


// A View wrapper to make the modifier easier to use
extension View {
    func onRotate(perform action: @escaping (UIDeviceOrientation) -> Void) -> some View {
        self.modifier(DeviceRotationViewModifier(action: action))
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}


extension String {
  // Allow open ranges like 'string[0..<n]'
  subscript(range: Range<Int>) -> String {
    let start = self.index(self.startIndex, offsetBy: range.lowerBound)
    let end = self.index(self.startIndex, offsetBy: range.upperBound)
    return String(self[start..<end])
  }
}



