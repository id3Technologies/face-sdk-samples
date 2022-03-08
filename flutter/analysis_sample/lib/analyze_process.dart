import 'analyze_face_result.dart';
import 'capture_process.dart';
import 'package:id3_face/id3_face.dart';

AnalyzeFaceResult onAnalyze(CaptureProcessResult cameraProcessResult) {
  final faceAnalyzer = FaceAnalyser();
  final image =
      Image.fromBuffer(cameraProcessResult.imageBytes, PixelFormat.bgr24Bits);
  final detectedFace =
      DetectedFace.fromBuffer(cameraProcessResult.detectedFaceBytes);
  final facePose = faceAnalyzer.computePose(detectedFace);
  final faceOcclusionScores =
      faceAnalyzer.detectOcclusions(image, detectedFace);
  final faceAttributes = faceAnalyzer.computeAttributes(image, detectedFace);

  // Extracts the portrait image of the detected face to display it.
  final portraitBounds = detectedFace.getPortraitBounds(0.25, 0.45, 1.33);
  final portraitImage = image.extractRoi(portraitBounds);
  portraitImage.flip(true, false);

  // Compress the portrait image buffer as a JPEG buffer.
  final jpegPortraitImageBuffer =
      portraitImage.toBuffer(ImageFormat.jpeg, 75.0);

  final analyzeFaceResult = AnalyzeFaceResult(
    portraitImage: jpegPortraitImageBuffer,
    yaw: facePose.struct.Yaw,
    pitch: facePose.struct.Pitch,
    roll: facePose.struct.Roll,
    leftEyeOcclusionScore: faceOcclusionScores.struct.LeftEyeOcclusionScore,
    rightEyeOcclusionScore: faceOcclusionScores.struct.RightEyeOcclusionScore,
    mouthOcclusionScore: faceOcclusionScores.struct.MouthOcclusionScore,
    noseOcclusionScore: faceOcclusionScores.struct.NoseOcclusionScore,
    glasses: faceAttributes.struct.Glasses,
    hat: faceAttributes.struct.Hat,
    makeUp: faceAttributes.struct.MakeUp,
    male: faceAttributes.struct.Male,
    mouthOpen: faceAttributes.struct.MouthOpen,
    smile: faceAttributes.struct.Smile,
  );

  faceAnalyzer.dispose();
  image.dispose();
  detectedFace.dispose();
  facePose.dispose();
  faceOcclusionScores.dispose();
  faceAttributes.dispose();
  portraitBounds.dispose();
  portraitImage.dispose();

  return analyzeFaceResult;
}
