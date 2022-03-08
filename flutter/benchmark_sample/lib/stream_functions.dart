import 'dart:developer';
import 'dart:typed_data';

import 'utils.dart';
import 'package:flutter/foundation.dart';
import 'package:id3_face/id3_face.dart' as sdk;

Stream<String> printAndYield(String data) async* {
  debugPrint(data);
  yield data;
}

Stream<String> loadFaceDetectorModelBenchmark(
    Uint8List modelBytes, sdk.FaceModel faceModel) async* {
  final stopwatch = Stopwatch()..start();
  sdk.FaceLibrary.loadModelBuffer(
    modelBytes,
    faceModel,
    sdk.ProcessingUnit.cpu,
  );

  yield* printAndYield(
      "FaceDetector${faceModel.modelNumber} model loading : ${stopwatch.elapsedMilliseconds} ms");
}

Stream<String> loadFaceQualityEstimatorModelBenchmark(
    Uint8List modelBytes) async* {
  final stopwatch = Stopwatch()..start();

  sdk.FaceLibrary.loadModelBuffer(
    modelBytes,
    sdk.FaceModel.faceEncodingQualityEstimator3A,
    sdk.ProcessingUnit.cpu,
  );

  yield* printAndYield(
      "FaceEncodingQualityEstimator model loading : ${stopwatch.elapsedMilliseconds} ms");
}

Stream<String> loadFaceEncoderModelBenchmark(
    Uint8List faceModelBytes, sdk.FaceModel faceModel) async* {
  yield* printAndYield(
      "Beginning of face encoder benchmark ${faceModel.modelNumber}");

  final stopwatch = Stopwatch()..start();

  sdk.FaceLibrary.loadModelBuffer(
    faceModelBytes,
    faceModel,
    sdk.ProcessingUnit.cpu,
  );

  yield* printAndYield(
      "FaceEncoder${faceModel.modelNumber} model loading : ${stopwatch.elapsedMilliseconds} ms");
}

/// Times the face detection on the images. Analyze the influence of the thread count.
/// @param images: the images on which we will perform the detections.
Stream<String> faceDetectionBenchmark(List<Uint8List> imagesBytes,
    sdk.FaceModel faceModel, sdk.FaceDetector faceDetector) async* {
  yield* printAndYield(
      "Beginning of face detection benchmark ${faceModel.modelNumber}");
  var faceDetectorThreadCount = 1;
  final images = imagesBytes
      .map((e) => sdk.Image.fromBuffer(e, sdk.PixelFormat.bgr24Bits));
  for (var i = 0; i < 3; i++) {
    for (var img in images) {
      /// Benchmark face detection. We do 10 detections on all the images and take the mean
      /// of the time needed to perform each detection. Once the 10 detections are achieved,
      /// we increase the threadCount of the detector and we restart the 10 detections in
      /// order to study its impact on the detection time.

      yield* printAndYield(
          "Face detection in ${img.getHeight()}x${img.getWidth()} image with ThreadCount=$faceDetectorThreadCount");
      int meanDetectionTime = 0;

      for (var j = 0; j < 9; j++) {
        var stopwatch = Stopwatch()..start();
        faceDetector.detectFaces(img);
        meanDetectionTime += stopwatch.elapsedMilliseconds;
      }
      meanDetectionTime ~/= 10;
      yield* printAndYield("Done: ${meanDetectionTime}ms");
    }
    faceDetectorThreadCount *= 2;

    faceDetector.setThreadCount(faceDetectorThreadCount);
  }
  yield* printAndYield("End of face detection benchmark");
}

Stream<String> faceQualityBenchmark(List<Uint8List> imagesBytes,
    sdk.FaceDetector faceDetector, sdk.FaceEncoder faceEncoder) async* {
  yield* printAndYield("Beginning of face quality computing benchmark");

  // The dimension of an image has no impact on the quality computing time.
  // So we only benchmark the quality computing time on the first image.

  final image = sdk.Image.fromBuffer(imagesBytes[0], sdk.PixelFormat.bgr24Bits);

  final detectedFaces = faceDetector.detectFaces(image);

  if (detectedFaces.getCount() == 0 || detectedFaces.getCount() > 1) {
    log("Error : the image 0 should contain 1 face, not ${detectedFaces.getCount()}");
  } else {
    //   Benchmark quality estimator. We compute the quality of the image 10 times and we
    //   take the mean of the time needed to perform each quality estimation.

    yield* printAndYield(
        "Quality estimation in ${image.getHeight()}x${image.getWidth()} image");

    final detectedFace = detectedFaces.get(0);
    var meanQualityComputingTime = 0;
    for (int i = 0; i < 9; i++) {
      final stopwatch = Stopwatch()..start();
      faceEncoder.computeQuality(image, detectedFace);
      meanQualityComputingTime += stopwatch.elapsedMilliseconds;
    }
    meanQualityComputingTime ~/= 10;
    yield* printAndYield("Done: ${meanQualityComputingTime}ms");
    yield* printAndYield("End of face quality computing benchmark");
  }
}

Stream<String> faceEncoderBenchmark(
    List<Uint8List> imagesBytes,
    sdk.FaceModel faceModel,
    sdk.FaceDetector faceDetector,
    sdk.FaceEncoder faceEncoder) async* {
  var faceEncoderThreadCount = 1;

  final image = sdk.Image.fromBuffer(imagesBytes[0], sdk.PixelFormat.bgr24Bits);
  final detectedFaces = faceDetector.detectFaces(image);

  if (detectedFaces.getCount() == 0 || detectedFaces.getCount() > 1) {
    log("Error : the image 0 should contain 1 face, not ${detectedFaces.getCount()}");
  } else {
    //  Benchmark encoder. We compute the template of the image 10 times and we take the
    //  mean of the time needed to perform each template extraction. Once the 10
    //  extractions are achieved, we increase the threadCount of the encoder and we
    //  restart the 10 extractions in order to study its impact on the extraction time.

    yield* printAndYield(
        "Beginning of face encoder ${faceModel.modelNumber} benchmark");
    final detectedFace = detectedFaces.get(0);
    for (int i = 0; i < 3; i++) {
      yield* printAndYield(
          "Extraction in ${image.getHeight()}x${image.getWidth()} image with ThreadCount=$faceEncoderThreadCount");
      var meanExtractionTime = 0;
      for (int i = 0; i < 9; i++) {
        final stopwatch = Stopwatch()..start();
        faceEncoder.createTemplate(image, detectedFace);
        meanExtractionTime += stopwatch.elapsedMilliseconds;
      }
      meanExtractionTime ~/= 10;
      yield* printAndYield("Done: ${meanExtractionTime}ms");
      faceEncoderThreadCount *= 2;
      faceEncoder.setThreadCount(faceEncoderThreadCount);
    }
  }
  yield* printAndYield(
      "End of face face encoder ${faceModel.modelNumber} benchmark");
}
