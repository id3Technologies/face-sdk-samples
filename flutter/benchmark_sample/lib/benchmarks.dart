import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';
import 'isolate_functions.dart';
import 'package:benchmark_sample/stream_functions.dart' as sf;
import 'package:id3_face/id3_face.dart' as sdk;

import 'utils.dart';

CompleteBenchmark get completeBenchmark => CompleteBenchmark(
      imagesBytes: images,
      faceDetector3AModelBytes: faceDetector3AModelBytes,
      faceDetector3BModelBytes: faceDetector3BModelBytes,
      faceEncoder9AModelBytes: faceEncoder9AModelBytes,
      faceEncoder9BModelBytes: faceEncoder9BModelBytes,
      faceQualityModelBytes: faceQualityModelBytes,
    );

DetectorBenchmark get detector3ABenchmark => DetectorBenchmark(
      faceDetectorModelBytes: faceDetector3AModelBytes,
      faceDetectorModel: sdk.FaceModel.faceDetector3A,
      imagesBytes: images,
    );

DetectorBenchmark get detector3BBenchmark => DetectorBenchmark(
      faceDetectorModelBytes: faceDetector3BModelBytes,
      faceDetectorModel: sdk.FaceModel.faceDetector3B,
      imagesBytes: images,
    );

QualityBenchmark get qualityBenchmark => QualityBenchmark(
      faceEncoderModelBytes: faceEncoder9BModelBytes,
      faceDetectorModelBytes: faceDetector3BModelBytes,
      faceQualityModelBytes: faceQualityModelBytes,
      imagesBytes: images,
    );

EncoderBenchmark get encoder9ABenchmark => EncoderBenchmark(
    faceEncoderModelBytes: faceEncoder9AModelBytes,
    faceEncoderModel: sdk.FaceModel.faceEncoder9A,
    imagesBytes: images,
    faceDetectorModelBytes: faceDetector3BModelBytes);

EncoderBenchmark get encoder9BBenchmark => EncoderBenchmark(
    faceEncoderModelBytes: faceEncoder9BModelBytes,
    faceEncoderModel: sdk.FaceModel.faceEncoder9B,
    imagesBytes: images,
    faceDetectorModelBytes: faceDetector3BModelBytes);

abstract class Benchmark {
  void runInNewIsolate(SendPort p);
  Stream<String> runInMainIsolate();
}

class QualityBenchmark extends Benchmark {
  QualityBenchmark({
    required this.faceEncoderModelBytes,
    required this.faceDetectorModelBytes,
    required this.faceQualityModelBytes,
    required this.imagesBytes,
  });

  final Uint8List faceDetectorModelBytes;
  final Uint8List faceEncoderModelBytes;
  final Uint8List faceQualityModelBytes;
  final List<Uint8List> imagesBytes;

  @override
  void runInNewIsolate(SendPort p) {
    loadFaceEncoderModelBenchmark(
        p, faceEncoderModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    loadFaceQualityEstimatorModelBenchmark(p, faceQualityModelBytes);
    loadFaceDetectorModelBenchmark(
        p, faceDetectorModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    faceQualityBenchmark(p, imagesBytes, faceDetector, faceEncoder);
    faceEncoder.dispose();
    faceDetector.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoderModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    yield* sf.loadFaceQualityEstimatorModelBenchmark(faceQualityModelBytes);
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetectorModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    yield* sf.faceQualityBenchmark(imagesBytes, faceDetector, faceEncoder);
    faceEncoder.dispose();
    faceDetector.dispose();
  }
}

class DetectorBenchmark extends Benchmark {
  DetectorBenchmark({
    required this.faceDetectorModelBytes,
    required this.imagesBytes,
    required this.faceDetectorModel,
  });

  final sdk.FaceModel faceDetectorModel;
  final Uint8List faceDetectorModelBytes;
  final List<Uint8List> imagesBytes;

  @override
  void runInNewIsolate(SendPort p) {
    loadFaceDetectorModelBenchmark(
        p, faceDetectorModelBytes, faceDetectorModel);
    final faceDetector = sdk.FaceDetector()..setModel(faceDetectorModel);
    faceDetectionBenchmark(
      p,
      imagesBytes,
      faceDetectorModel,
      faceDetector,
    );
    faceDetector.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetectorModelBytes, faceDetectorModel);
    final faceDetector = sdk.FaceDetector()..setModel(faceDetectorModel);
    yield* sf.faceDetectionBenchmark(
      imagesBytes,
      faceDetectorModel,
      faceDetector,
    );
    faceDetector.dispose();
  }
}

class EncoderBenchmark extends Benchmark {
  EncoderBenchmark({
    required this.faceEncoderModelBytes,
    required this.faceEncoderModel,
    required this.imagesBytes,
    required this.faceDetectorModelBytes,
  });

  final Uint8List faceDetectorModelBytes;
  final sdk.FaceModel faceEncoderModel;
  final Uint8List faceEncoderModelBytes;
  final List<Uint8List> imagesBytes;

  @override
  void runInNewIsolate(SendPort p) {
    loadFaceEncoderModelBenchmark(p, faceEncoderModelBytes, faceEncoderModel);
    final faceEncoder = sdk.FaceEncoder()..setModel(faceEncoderModel);
    loadFaceDetectorModelBenchmark(
        p, faceDetectorModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    faceEncoderBenchmark(
        p, imagesBytes, faceEncoderModel, faceDetector, faceEncoder);
    faceEncoder.dispose();
    faceDetector.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoderModelBytes, faceEncoderModel);
    final faceEncoder = sdk.FaceEncoder()..setModel(faceEncoderModel);
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetectorModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    yield* sf.faceEncoderBenchmark(
        imagesBytes, faceEncoderModel, faceDetector, faceEncoder);
    faceEncoder.dispose();
    faceDetector.dispose();
  }
}

class CompleteBenchmark extends Benchmark {
  CompleteBenchmark({
    required this.faceDetector3AModelBytes,
    required this.faceDetector3BModelBytes,
    required this.faceEncoder9AModelBytes,
    required this.faceEncoder9BModelBytes,
    required this.faceQualityModelBytes,
    required this.imagesBytes,
  });

  final Uint8List faceDetector3AModelBytes;
  final Uint8List faceDetector3BModelBytes;
  final Uint8List faceEncoder9AModelBytes;
  final Uint8List faceEncoder9BModelBytes;
  final Uint8List faceQualityModelBytes;
  final List<Uint8List> imagesBytes;

  @override
  void runInNewIsolate(SendPort p) {
    p.send("Number of processors : ${Platform.numberOfProcessors}");
    loadFaceDetectorModelBenchmark(
        p, faceDetector3AModelBytes, sdk.FaceModel.faceDetector3A);
    final faceDetector3A = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3A);
    loadFaceDetectorModelBenchmark(
        p, faceDetector3BModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector3B = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    loadFaceQualityEstimatorModelBenchmark(p, faceQualityModelBytes);

    loadFaceEncoderModelBenchmark(
        p, faceEncoder9AModelBytes, sdk.FaceModel.faceEncoder9A);
    final faceEncoder9A = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9A);
    loadFaceEncoderModelBenchmark(
        p, faceEncoder9BModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder9B = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    p.send(separator);
    faceDetectionBenchmark(
      p,
      imagesBytes,
      sdk.FaceModel.faceDetector3A,
      faceDetector3A,
    );
    p.send(separator);
    faceDetectionBenchmark(
      p,
      imagesBytes,
      sdk.FaceModel.faceDetector3B,
      faceDetector3B,
    );
    p.send(separator);
    faceQualityBenchmark(p, imagesBytes, faceDetector3B, faceEncoder9B);
    p.send(separator);
    faceEncoderBenchmark(p, imagesBytes, sdk.FaceModel.faceEncoder9A,
        faceDetector3B, faceEncoder9A);
    p.send(separator);
    faceEncoderBenchmark(p, imagesBytes, sdk.FaceModel.faceEncoder9B,
        faceDetector3B, faceEncoder9B);
    p.send("End of the benchmark.");

    faceDetector3A.dispose();
    faceDetector3B.dispose();
    faceEncoder9A.dispose();
    faceEncoder9B.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf
        .printAndYield("Number of processors : ${Platform.numberOfProcessors}");
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetector3AModelBytes, sdk.FaceModel.faceDetector3A);
    final faceDetector3A = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3A);
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetector3BModelBytes, sdk.FaceModel.faceDetector3B);
    final faceDetector3B = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector3B);
    yield* sf.loadFaceQualityEstimatorModelBenchmark(faceQualityModelBytes);

    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoder9AModelBytes, sdk.FaceModel.faceEncoder9A);
    final faceEncoder9A = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9A);
    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoder9BModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder9B = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    yield* sf.printAndYield(separator);
    yield* sf.faceDetectionBenchmark(
      imagesBytes,
      sdk.FaceModel.faceDetector3A,
      faceDetector3A,
    );
    yield* sf.printAndYield(separator);
    yield* sf.faceDetectionBenchmark(
      imagesBytes,
      sdk.FaceModel.faceDetector3B,
      faceDetector3B,
    );
    yield* sf.printAndYield(separator);
    yield* sf.faceQualityBenchmark(imagesBytes, faceDetector3B, faceEncoder9B);
    yield* sf.printAndYield(separator);
    yield* sf.faceEncoderBenchmark(imagesBytes, sdk.FaceModel.faceEncoder9A,
        faceDetector3B, faceEncoder9A);
    yield* sf.printAndYield(separator);
    yield* sf.faceEncoderBenchmark(imagesBytes, sdk.FaceModel.faceEncoder9B,
        faceDetector3B, faceEncoder9B);
    yield* sf.printAndYield("End of the benchmark.");

    faceDetector3A.dispose();
    faceDetector3B.dispose();
    faceEncoder9A.dispose();
    faceEncoder9B.dispose();
  }
}

// sdk.FaceDetector? faceDetector3A;
// sdk.FaceDetector? faceDetector3B;
// sdk.FaceEncoder? faceEncoder9A;
// sdk.FaceEncoder? faceEncoder9B;


// typedef BenchmarkFunction = Stream<String> Function();

