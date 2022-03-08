import 'dart:typed_data';

import 'package:analysis_sample/constants.dart';

class AnalyzeFaceResult {
  AnalyzeFaceResult({
    required this.portraitImage,
    required this.yaw,
    required this.pitch,
    required this.roll,
    required this.leftEyeOcclusionScore,
    required this.rightEyeOcclusionScore,
    required this.mouthOcclusionScore,
    required this.noseOcclusionScore,
    required this.glasses,
    required this.hat,
    required this.makeUp,
    required this.male,
    required this.mouthOpen,
    required this.smile,
    this.errorCode = 0,
  });

  factory AnalyzeFaceResult.error(int errorCode) {
    return AnalyzeFaceResult(
      portraitImage: Uint8List(0),
      errorCode: errorCode,
      yaw: 0,
      pitch: 0,
      roll: 0,
      leftEyeOcclusionScore: 0,
      rightEyeOcclusionScore: 0,
      mouthOcclusionScore: 0,
      noseOcclusionScore: 0,
      glasses: 0,
      hat: 0,
      makeUp: 0,
      male: 0,
      mouthOpen: 0,
      smile: 0,
    );
  }

  final int errorCode;
  final int leftEyeOcclusionScore;
  final int mouthOcclusionScore;
  final int noseOcclusionScore;
  final Uint8List portraitImage;
  final int rightEyeOcclusionScore;
  final double yaw, pitch, roll;
  final int glasses, hat, makeUp, male, mouthOpen, smile;

  bool get isPoseOk =>
      yaw.abs() < yawMaxThreshold &&
      pitch.abs() < pitchMaxThreshold &&
      roll.abs() < rollMaxThreshold;
  bool get isOcclusionOk =>
      leftEyeOcclusionScore < eyeOcclusionMaxThreshold &&
      rightEyeOcclusionScore < eyeOcclusionMaxThreshold &&
      noseOcclusionScore < noseOcclusionMaxThreshold &&
      mouthOcclusionScore < mouthOcclusionMaxThreshold;
  bool get hasHat => hat < hatMaxThreshold;
  bool get hasMouthOpen => mouthOpen < mouthOpenMaxThreshold;
  bool get hasSmile => smile < smileMaxThreshold;
}
