import 'package:flutter/services.dart';
import 'package:id3_face/id3_face.dart' as sdk;

Future<void> loadBiometricParameters() async {
  final imgs = <Uint8List>[];
  for (int i = 0; i < 3; i++) {
    final imageBytes =
        (await rootBundle.load("assets/images/im$i.jpg")).buffer.asUint8List();
    imgs.add(imageBytes);
  }
  images = imgs;
}

Future<Uint8List> loadAsset(String assetName) async {
  final byteData = await rootBundle.load('assets/models/$assetName');
  return byteData.buffer.asUint8List();
}

Future<void> loadAllAssets() async {
  faceDetector4AModelBytes = await loadAsset('face_detector_v4a.id3nn');
  faceDetector4BModelBytes = await loadAsset('face_detector_v4b.id3nn');
  faceEncoder9AModelBytes = await loadAsset('face_encoder_v9a.id3nn');
  faceEncoder9BModelBytes = await loadAsset('face_encoder_v9b.id3nn');
  faceQualityModelBytes =
      await loadAsset('face_encoding_quality_estimator_v3a.id3nn');
}

late List<Uint8List> images;
late Uint8List faceDetector4AModelBytes;
late Uint8List faceDetector4BModelBytes;
late Uint8List faceEncoder9AModelBytes;
late Uint8List faceEncoder9BModelBytes;
late Uint8List faceQualityModelBytes;
const separator = "--------------------------------------------------------";

extension FaceModelX on sdk.FaceModel {
  String get modelNumber {
    switch (this) {
      case sdk.FaceModel.faceDetector4A:
        return "4A";
      case sdk.FaceModel.faceDetector4B:
        return "4B";
      case sdk.FaceModel.faceEncoder9A:
        return "9A";
      case sdk.FaceModel.faceEncoder9B:
        return "9B";
      default:
        return "??";
    }
  }
}
