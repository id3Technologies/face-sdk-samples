import Foundation
import SwiftUI
import AVFoundation

import id3Face

/**
 * This view displays the camera preview and handles the face detection process
 */
class FaceCapturePreviewUIView: UIView {
    // Camera attributes
    private var captureSession: AVCaptureSession?
    private var videoOutput: AVCaptureVideoDataOutput = AVCaptureVideoDataOutput()
    private let sampleBufferQueue = DispatchQueue.global(qos: .userInteractive)
    
    private var videoPreviewLayer: AVCaptureVideoPreviewLayer {
        return layer as! AVCaptureVideoPreviewLayer
    }
    
    // Layer for drawing a rectangle around the detected face
    private var boundsLayer: CAShapeLayer
    
    // Biometric objects
    private var faceDetector: FaceDetector? = nil
    private var detectedFaceList: DetectedFaceList? = nil
    
    // Information we need from the other view or the device state
    @ObservedObject public var info: Information
    private var deviceOrientation: UIDeviceOrientation
    
    init(info: Information, detector: FaceDetector, deviceOrientation: UIDeviceOrientation) {
        // Initialization of the attributes
        self.info = info
        self.deviceOrientation = deviceOrientation
        faceDetector = detector
        boundsLayer = CAShapeLayer()
        super.init(frame: .zero)

        do {
            detectedFaceList = try DetectedFaceList()
        } catch let error as FaceException {
            NSLog("Error while initializing the camera preview : " + error.getMessage())
        } catch {
            NSLog("Error while initializing the camera preview : Unknown error")
        }

        // Configuration of the camera
        self.captureSession = setupSession()
    }
    
    /**
     * Setup the capture session
     * - Returns: The capture session
     */
    private func setupSession() -> AVCaptureSession {
        let session = AVCaptureSession()
        session.beginConfiguration()

        // add input device (here we select the front camera)
        let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera,
            for: .video, position: .front)
        
        guard videoDevice != nil, let videoDeviceInput = try? AVCaptureDeviceInput(device: videoDevice!), session.canAddInput(videoDeviceInput) else {
            fatalError("!!! NO CAMERA DETECTED")
        }
        session.addInput(videoDeviceInput)
        
        // configure output
        videoOutput.videoSettings = [String(kCVPixelBufferPixelFormatTypeKey) : kCVPixelFormatType_32BGRA]
        videoOutput.alwaysDiscardsLateVideoFrames = true
        videoOutput.setSampleBufferDelegate(self, queue: sampleBufferQueue)
        guard session.canAddOutput(videoOutput) else {
            fatalError("Cannot add input")
        }
        session.addOutput(videoOutput)
        
        session.commitConfiguration()
        return session
    }

    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    override func didMoveToSuperview() {
        super.didMoveToSuperview()

        if nil != self.superview {
            
            self.videoPreviewLayer.session = self.captureSession
            self.videoPreviewLayer.videoGravity = .resizeAspect
            
            switch deviceOrientation {
            case UIDeviceOrientation.landscapeLeft:
                self.videoPreviewLayer.connection?.videoOrientation = AVCaptureVideoOrientation.landscapeRight
            case UIDeviceOrientation.landscapeRight:
                self.videoPreviewLayer.connection?.videoOrientation = AVCaptureVideoOrientation.landscapeLeft
            default:
                self.videoPreviewLayer.connection?.videoOrientation = AVCaptureVideoOrientation.portrait
            }
            
            self.captureSession?.startRunning()
        } else {
            self.captureSession?.stopRunning()
        }
    }
}


extension FaceCapturePreviewUIView : AVCaptureVideoDataOutputSampleBufferDelegate {
    /**
     * Callback function that is called in order to process the video frames
     */
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        if info.isCapturing {
            // Enable the displaying of the camera
            DispatchQueue.main.async { [self] in
                videoPreviewLayer.connection?.isEnabled = true
            }
            
            do {
                // Retrieve an id3Face.Image object from the camera CMSampleBuffer
                let image = try imageFromCMSampleBuffer(cmsBuffer: sampleBuffer)
                /*
                    The camera is rotated by 90° by default, so we need to rotate the output of the camera
                    in order to process the image correctly
                 */
                if deviceOrientation == UIDeviceOrientation.unknown || deviceOrientation == UIDeviceOrientation.portrait {
                    try image.rotate(angle: 270)
                } else if deviceOrientation == UIDeviceOrientation.landscapeLeft {
                    try image.rotate(angle: 180)
                } else if deviceOrientation == UIDeviceOrientation.landscapeRight {}
                
                /*
                    If you look at the FaceDetector documentation, you will see that the algorithm searches for faces in the
                    range [16px; 512px].
                    So here we resize the image in order to be sure to fit to this range, this also allows the detection process
                    to be faster.
                 */
                // keep the original image
                let imageFullScale = try image.clone()
                let h = try imageFullScale.getHeight()
                let w = try imageFullScale.getWidth()
                
                let downscaleFactor = try image.downscale(maxSize: Parameters.detectionMaxSize)
            
                // Detecting faces on downscaled frame
                let detectedFaceList = try faceDetector!.detectFaces(image: image)
                let countFaces = try detectedFaceList.getCount()
                
                if (countFaces > 0) {
                    // At least one face has been detected !
                    let detectedFace = try detectedFaceList.getLargestFace()
                    // resize the detected face to the original image size
                    try detectedFace.rescale(scale: 1.0 / downscaleFactor)
                    let bounds = try detectedFace.getBounds()
                    // Update the last detected face and image (at original resolution) for the information view and draw the rectangle around the face
                    DispatchQueue.main.async { [self] in
                        info.lastDetectedImage = imageFullScale
                        info.lastDetectedFace = detectedFace
                        drawBounds(bounds: bounds, newWidth: w, newHeight: h)
                    }
                } else {
                    DispatchQueue.main.async { [self] in
                        clearBounds()
                    }
                }
            } catch let error as FaceException {
                NSLog("Face Error while analyzing the camera output : " + error.getMessage())
            } catch {
                NSLog("Error while analyzing the camera output : Unknown error")
            }
        } else {
            // Erase the yellow rectange around the detected face
            DispatchQueue.main.async { [self] in
                boundsLayer.removeFromSuperlayer()
            }
        }
    }
    
    // This function will load the CMSSampleBuffer provided by the camera into a id3Face.Image object will can be used with the id3 SDKs
    private func imageFromCMSampleBuffer(cmsBuffer: CMSampleBuffer) throws -> id3Face.Image {
        let imageBuffer = CMSampleBufferGetImageBuffer(cmsBuffer)!
        CVPixelBufferLockBaseAddress(imageBuffer, CVPixelBufferLockFlags(rawValue: 0))
        let byterPerRow = CVPixelBufferGetBytesPerRow(imageBuffer)
        let height = CVPixelBufferGetHeight(imageBuffer)
        let width = CVPixelBufferGetWidth(imageBuffer)
        let srcBuff = CVPixelBufferGetBaseAddress(imageBuffer)
        CVPixelBufferUnlockBaseAddress(imageBuffer, CVPixelBufferLockFlags(rawValue: 0))
        
        return try id3Face.Image.fromRawBuffer(pixels: srcBuff!.bindMemory(to: UInt8.self, capacity: byterPerRow*height),
                                               pixelsSize: Int32(byterPerRow*height),
                                               width: Int32(width),
                                               height: Int32(height),
                                               stride: Int32(byterPerRow),
                                               srcPixelFormat: PixelFormat.bgra,
                                               dstPixelFormat: PixelFormat.bgr24Bits)
    }
    
    /**
     * Draws a yellow rectange around the detected face
     */
    private func drawBounds(bounds: id3Face.Rectangle, newWidth: Int32, newHeight: Int32) {
        let viewWidth = Int32(self.videoPreviewLayer.frame.width)
        let viewHeight = Int32(self.videoPreviewLayer.frame.height)
        
        var boundsWidth = Int32(bounds.BottomRight.X - bounds.BottomLeft.X)
        var boundsHeight = Int32(bounds.BottomLeft.Y - bounds.TopLeft.Y)
        
        let scale = Float(viewHeight) / Float(newHeight)
        
        /*
            The camera in "selfie" mode acts like a mirror in the preview, but the real output
            is flipped compared to this preview.
         */
        var xPos = newWidth - bounds.TopRight.X
        xPos = Int32(Float(xPos)*scale)
        
        let yPos = Int32(Float(bounds.TopLeft.Y)*scale)
        
        boundsWidth = Int32(Float(boundsWidth)*scale)
        boundsHeight = Int32(Float(boundsHeight)*scale)
        
        // take into account aspect ratio crop
        let xOffset = Int32(Float(viewWidth) - Float(viewWidth)*Float(newWidth)/Float(newHeight)) / 2;
        xPos += xOffset
        
        boundsLayer.path = UIBezierPath(rect: CGRect(x: 0, y: 0, width: Int(boundsWidth), height: Int(boundsHeight))).cgPath
        boundsLayer.position = CGPoint(x: CGFloat(xPos), y: CGFloat(yPos))
        boundsLayer.fillColor = nil
        boundsLayer.lineWidth = 2.0
        boundsLayer.strokeColor = UIColor.yellow.cgColor
        self.videoPreviewLayer.addSublayer(boundsLayer)
    }
    
    private func clearBounds() {
        boundsLayer.strokeColor = UIColor.clear.cgColor
        self.videoPreviewLayer.addSublayer(boundsLayer)
    }
    
    
    public func setDeviceOrientation(deviceOrientation: UIDeviceOrientation) {
        self.deviceOrientation = deviceOrientation
    }
}


/**
 * This struct is used to convert the UIView in an UIViewRepresentable in order to be displayed by SwiftUI
 */
struct FaceCapturePreviewView: UIViewRepresentable {
    private let faceCaptureUIView : FaceCapturePreviewUIView
    @Binding private var deviceOrientation: UIDeviceOrientation
    
    init(info: Information, detector: FaceDetector, deviceOrientation: Binding<UIDeviceOrientation>) {
        self._deviceOrientation = deviceOrientation
        faceCaptureUIView = FaceCapturePreviewUIView(info: info, detector: detector, deviceOrientation: deviceOrientation.wrappedValue)
    }
    
    func makeUIView(context: UIViewRepresentableContext<FaceCapturePreviewView>) -> FaceCapturePreviewUIView {
        faceCaptureUIView
    }
    

    func updateUIView(_ uiView: FaceCapturePreviewUIView, context: UIViewRepresentableContext<FaceCapturePreviewView>) {
        uiView.setDeviceOrientation(deviceOrientation: self.deviceOrientation)
        uiView.didMoveToSuperview()
    }

    typealias UIViewType = FaceCapturePreviewUIView
}

