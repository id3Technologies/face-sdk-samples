import 'dart:async';
import 'dart:io';

import 'dart:isolate';
import 'dart:ui';

import 'package:camera/camera.dart' hide ImageFormat;
import 'package:flutter/foundation.dart';
import 'package:id3_face/id3_face.dart';

class CaptureProcessResult {
  CaptureProcessResult({
    required this.imageWidth,
    required this.imageHeight,
    required this.imageBytes,
    required this.detectedFaceBytes,
    required this.faceBounds,
  });
  final int imageWidth;
  final int imageHeight;
  final Uint8List detectedFaceBytes;
  final Uint8List imageBytes;
  final Rect faceBounds;
}

class CaptureProcess {
  CaptureProcess(int cameraOrientation) {
    init(cameraOrientation);
  }

  Completer<CaptureProcessResult?>? _cameraImageProcessFinish;
  late Isolate _isolate;
  final _isolateReady = Completer<void>();
  late SendPort _sendPort;

  Future<void> get isReady => _isolateReady.future;

  void dispose() {
    _isolate.kill();
  }

  Future<void> init(int cameraOrientation) async {
    final receivePort = ReceivePort();
    final errorPort = ReceivePort();
    errorPort.listen((e) {
      debugPrint(e.toString());
    });

    receivePort.listen(_handleMessage);
    _isolate = await Isolate.spawn(
      _isolateEntry,
      [receivePort.sendPort, cameraOrientation],
      onError: errorPort.sendPort,
    );
  }

  Future<CaptureProcessResult?> processCameraImage(CameraImage cameraImage) {
    _sendPort.send(cameraImage);
    _cameraImageProcessFinish = Completer<CaptureProcessResult?>();
    return _cameraImageProcessFinish!.future;
  }

  void _handleMessage(dynamic message) {
    if (message is SendPort) {
      _sendPort = message;
      _isolateReady.complete();
      return;
    }

    if (message is CaptureProcessResult?) {
      _cameraImageProcessFinish?.complete(message);
      _cameraImageProcessFinish = null;
      return;
    }

    throw UnimplementedError("Undefined behavior for message: $message");
  }

  static void _isolateEntry(dynamic message) {
    late SendPort sendPort;
    late int cameraOrientation;
    final receivePort = ReceivePort();

    final faceDetector = FaceDetector();
    faceDetector
      ..setModel(FaceModel.faceDetector4B)
      ..setNmsIouThreshold(0);

    receivePort.listen((dynamic message) async {
      assert(message is CameraImage);
      final cameraImage = message as CameraImage;

      CaptureProcessResult? result;
      late Image image, resizedImage;
      try {
        if (cameraImage.format.group == ImageFormatGroup.bgra8888) {
          image = Image.fromRawBuffer(
            cameraImage.planes.first.bytes,
            cameraImage.width,
            cameraImage.height,
            cameraImage.planes.first.bytesPerRow,
            PixelFormat.bgra,
            PixelFormat.bgr24Bits,
          );
        } else {
          image = Image.fromYuvPlanes(
            cameraImage.planes[0].bytes,
            cameraImage.planes[1].bytes,
            cameraImage.planes[2].bytes,
            cameraImage.width,
            cameraImage.height,
            cameraImage.planes[1].bytesPerPixel ?? cameraImage.width,
            cameraImage.planes[1].bytesPerRow,
            PixelFormat.bgr24Bits,
          );
        }
        // we need to rotate image to be portrait up oriented in Android
        // or flip image in ios
        if (Platform.isAndroid) {
          image.rotate(360 - cameraOrientation);
        } else {
          image.flip(true, false);
        }

        resizedImage = image.clone();

        // we downscale the image to accelerate the process
        final scale = resizedImage.downscale(256);

        DetectedFaceList detectedFaceList = faceDetector.detectFaces(resizedImage);
        if (detectedFaceList.getCount() > 0) {
          DetectedFace detectedFace = detectedFaceList.getLargestFace()!;
          detectedFace.rescale(1 / scale);
          if (detectedFace.getInterocularDistance() > 64) {
            // we create a flutter rect from the sdk bounds og the face
            Rectangle bounds = detectedFace.getBounds();
            final rect = Rect.fromPoints(
              Offset(
                bounds.topLeft.x.toDouble(),
                bounds.topLeft.y.toDouble(),
              ),
              Offset(
                bounds.bottomRight.x.toDouble(),
                bounds.bottomRight.y.toDouble(),
              ),
            );

            result = CaptureProcessResult(
              imageWidth: image.getWidth(),
              imageHeight: image.getHeight(),
              imageBytes: image.toBuffer(ImageFormat.bmp, 0),
              detectedFaceBytes: detectedFace.toBuffer(),
              faceBounds: rect,
            );
            bounds.dispose();
          }
          detectedFace.dispose();
        }
        detectedFaceList.dispose();
        sendPort.send(result);
      } finally {
        image.dispose();
        resizedImage.dispose();
      }
    });

    if (message is List) {
      cameraOrientation = message[1];
      sendPort = message[0];
      sendPort.send(receivePort.sendPort);
      return;
    }
  }
}
