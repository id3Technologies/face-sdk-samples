import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';
import 'isolate_functions.dart';
import 'package:benchmark_sample/stream_functions.dart' as sf;
import 'package:id3_face/id3_face.dart' as sdk;

import 'utils.dart';

CompleteBenchmark get completeBenchmark => CompleteBenchmark(
      imagesBytes: images,
      faceDetector4AModelBytes: faceDetector4AModelBytes,
      faceDetector4BModelBytes: faceDetector4BModelBytes,
      faceEncoder10AModelBytes: faceEncoder10AModelBytes,
      faceEncoder9BModelBytes: faceEncoder9BModelBytes,
      faceQualityModelBytes: faceQualityModelBytes,
    );

DetectorBenchmark get detector4ABenchmark => DetectorBenchmark(
      faceDetectorModelBytes: faceDetector4AModelBytes,
      faceDetectorModel: sdk.FaceModel.faceDetector4A,
      imagesBytes: images,
    );

DetectorBenchmark get detector4BBenchmark => DetectorBenchmark(
      faceDetectorModelBytes: faceDetector4BModelBytes,
      faceDetectorModel: sdk.FaceModel.faceDetector4B,
      imagesBytes: images,
    );

QualityBenchmark get qualityBenchmark => QualityBenchmark(
      faceEncoderModelBytes: faceEncoder9BModelBytes,
      faceDetectorModelBytes: faceDetector4BModelBytes,
      faceQualityModelBytes: faceQualityModelBytes,
      imagesBytes: images,
    );

EncoderBenchmark get encoder9ABenchmark => EncoderBenchmark(
    faceEncoderModelBytes: faceEncoder10AModelBytes,
    faceEncoderModel: sdk.FaceModel.faceEncoder10A,
    imagesBytes: images,
    faceDetectorModelBytes: faceDetector4BModelBytes);

EncoderBenchmark get encoder9BBenchmark => EncoderBenchmark(
    faceEncoderModelBytes: faceEncoder9BModelBytes,
    faceEncoderModel: sdk.FaceModel.faceEncoder9B,
    imagesBytes: images,
    faceDetectorModelBytes: faceDetector4BModelBytes);

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
    loadFaceDetectorModelBenchmark(
        p, faceDetectorModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);
    faceEncoder.dispose();
    faceDetector.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoderModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetectorModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);
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
        p, faceDetectorModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);
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
        faceDetectorModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);
    yield* sf.faceEncoderBenchmark(
        imagesBytes, faceEncoderModel, faceDetector, faceEncoder);
    faceEncoder.dispose();
    faceDetector.dispose();
  }
}

class CompleteBenchmark extends Benchmark {
  CompleteBenchmark({
    required this.faceDetector4AModelBytes,
    required this.faceDetector4BModelBytes,
    required this.faceEncoder10AModelBytes,
    required this.faceEncoder9BModelBytes,
    required this.faceQualityModelBytes,
    required this.imagesBytes,
  });

  final Uint8List faceDetector4AModelBytes;
  final Uint8List faceDetector4BModelBytes;
  final Uint8List faceEncoder10AModelBytes;
  final Uint8List faceEncoder9BModelBytes;
  final Uint8List faceQualityModelBytes;
  final List<Uint8List> imagesBytes;

  @override
  void runInNewIsolate(SendPort p) {
    p.send("Number of processors : ${Platform.numberOfProcessors}");
    loadFaceDetectorModelBenchmark(
        p, faceDetector4AModelBytes, sdk.FaceModel.faceDetector4A);
    final faceDetector4A = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4A);
    loadFaceDetectorModelBenchmark(
        p, faceDetector4BModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector4B = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);

    loadFaceEncoderModelBenchmark(
        p, faceEncoder10AModelBytes, sdk.FaceModel.faceEncoder10A);
    final faceEncoder10A = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder10A);
    loadFaceEncoderModelBenchmark(
        p, faceEncoder9BModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder9B = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    p.send(separator);
    faceDetectionBenchmark(
      p,
      imagesBytes,
      sdk.FaceModel.faceDetector4A,
      faceDetector4A,
    );
    p.send(separator);
    faceDetectionBenchmark(
      p,
      imagesBytes,
      sdk.FaceModel.faceDetector4B,
      faceDetector4B,
    );
    p.send(separator);
    faceEncoderBenchmark(p, imagesBytes, sdk.FaceModel.faceEncoder10A,
        faceDetector4B, faceEncoder10A);
    p.send(separator);
    faceEncoderBenchmark(p, imagesBytes, sdk.FaceModel.faceEncoder9B,
        faceDetector4B, faceEncoder9B);
    p.send("End of the benchmark.");

    faceDetector4A.dispose();
    faceDetector4B.dispose();
    faceEncoder10A.dispose();
    faceEncoder9B.dispose();
  }

  @override
  Stream<String> runInMainIsolate() async* {
    yield* sf
        .printAndYield("Number of processors : ${Platform.numberOfProcessors}");
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetector4AModelBytes, sdk.FaceModel.faceDetector4A);
    final faceDetector4A = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4A);
    yield* sf.loadFaceDetectorModelBenchmark(
        faceDetector4BModelBytes, sdk.FaceModel.faceDetector4B);
    final faceDetector4B = sdk.FaceDetector()
      ..setModel(sdk.FaceModel.faceDetector4B);

    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoder10AModelBytes, sdk.FaceModel.faceEncoder10A);
    final faceEncoder10A = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder10A);
    yield* sf.loadFaceEncoderModelBenchmark(
        faceEncoder9BModelBytes, sdk.FaceModel.faceEncoder9B);
    final faceEncoder9B = sdk.FaceEncoder()
      ..setModel(sdk.FaceModel.faceEncoder9B);
    yield* sf.printAndYield(separator);
    yield* sf.faceDetectionBenchmark(
      imagesBytes,
      sdk.FaceModel.faceDetector4A,
      faceDetector4A,
    );
    yield* sf.printAndYield(separator);
    yield* sf.faceDetectionBenchmark(
      imagesBytes,
      sdk.FaceModel.faceDetector4B,
      faceDetector4B,
    );
    yield* sf.printAndYield(separator);
    yield* sf.faceEncoderBenchmark(imagesBytes, sdk.FaceModel.faceEncoder10A,
        faceDetector4B, faceEncoder10A);
    yield* sf.printAndYield(separator);
    yield* sf.faceEncoderBenchmark(imagesBytes, sdk.FaceModel.faceEncoder9B,
        faceDetector4B, faceEncoder9B);
    yield* sf.printAndYield("End of the benchmark.");

    faceDetector4A.dispose();
    faceDetector4B.dispose();
    faceEncoder10A.dispose();
    faceEncoder9B.dispose();
  }
}

// sdk.FaceDetector? faceDetector3A;
// sdk.FaceDetector? faceDetector3B;
// sdk.FaceEncoder? faceEncoder10A;
// sdk.FaceEncoder? faceEncoder9B;


// typedef BenchmarkFunction = Stream<String> Function();

