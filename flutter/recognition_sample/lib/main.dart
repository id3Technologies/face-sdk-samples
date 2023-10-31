import 'dart:io';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:id3_face/id3_face.dart' as sdk;
import 'package:path_provider/path_provider.dart';

import 'bounds_painter.dart';
import 'capture_process.dart';
import 'credentials.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  String hardwareCode;

  if (Platform.isAndroid) {
    hardwareCode =
        sdk.FaceLicense.getHostHardwareCode(sdk.LicenseHardwareCodeType.android);
  } else {
    hardwareCode =
        sdk.FaceLicense.getHostHardwareCode(sdk.LicenseHardwareCodeType.iOS);
  }

  await activateLicense(hardwareCode);
  await loadModels();
  runApp(const App());
}

Future<void> activateLicense(String hardwareCode) async {
  /*
    The id3 Face Toolkit needs a valid license to work.

    An id3 license file is only valid for ONE application. Each application must retrieve its own
    license file.

    This SDK provides a specific API to directly download license files from
      the developed applications.

    This function will try to load a license file from internal storage and register it.

    If the license file do not exist it will try to download a new license using the specified
    serial key.

    For deployment purposes there is also an API to use your id3 account to retrieve the license file.
    This API need the login/password and product package of the SDK you use.
  */
  final licensePath = '${(await getTemporaryDirectory()).path}/id3/id3license/id3license_$productReference.lic';
  final licenseFile = File(licensePath);
  try {
   sdk.FaceLicense.checkLicense(licensePath);
  } catch (_) {
    Uint8List? licenseBytes;
    if (serialKey() != "0000-0000-0000-0000") {
      licenseBytes = sdk.FaceLicense.activateSerialKeyBuffer(
        hardwareCode,
        serialKey(),
        "Activated from Face capture sample",
      );
    }

    if (login() != "login" &&
        password() != "password" &&
        productReference() != "00000000") {
      licenseBytes = sdk.FaceLicense.activateBuffer(hardwareCode, login(), password(),
          productReference(), "Activated from face capture sample");
    }
    if (!licenseFile.existsSync()) {
      licenseFile.createSync(recursive: true);
    }
    licenseFile.writeAsBytesSync(licenseBytes!);
 
    sdk.FaceLicense.checkLicense(licensePath);
  }
}

Future<void> loadModels() async {
  final faceDetector =
      await rootBundle.load('assets/models/face_detector_v4b.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceDetector.buffer.asUint8List(),
    sdk.FaceModel.faceDetector4B,
    sdk.ProcessingUnit.cpu,
  );
  final faceEncoder =
      await rootBundle.load('assets/models/face_encoder_v9b.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceEncoder.buffer.asUint8List(),
    sdk.FaceModel.faceEncoder9B,
    sdk.ProcessingUnit.cpu,
  );
  final faceQuality = await rootBundle
      .load('assets/models/face_encoding_quality_estimator_v3a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceQuality.buffer.asUint8List(),
    sdk.FaceModel.faceEncodingQualityEstimator3A,
    sdk.ProcessingUnit.cpu,
  );
}

class App extends StatelessWidget {
  const App({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Recognition Sample',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const CapturePage(),
    );
  }
}

class CapturePage extends StatefulWidget {
  const CapturePage({Key? key}) : super(key: key);

  @override
  State<CapturePage> createState() => _CapturePageState();
}

class _CapturePageState extends State<CapturePage> {
  CameraController? controller;
  EnrollResult? enrollResult;
  CaptureProcessResult? lastResult;
  MatchResult? matchResult;
  CaptureProcess? process;
  bool processingImage = false;

  @override
  void dispose() {
    controller?.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    // we check available cameras and create controller with the front camera
    availableCameras().then((cameras) {
      controller = CameraController(
        cameras.firstWhere(
            (camera) => camera.lensDirection == CameraLensDirection.front),
        ResolutionPreset.max,
        enableAudio: false,
      );
      controller!.initialize().then((_) {
        if (!mounted) {
          return;
        }
        process = CaptureProcess(controller!.description.sensorOrientation);
        setState(() {});
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SizedBox(
            height: 256,
            child: (controller == null || !controller!.value.isInitialized)
                ? Container()
                : CustomPaint(
                    foregroundPainter: BoundsPainter(
                      bounds: lastResult?.faceBounds ?? Rect.zero,
                      imageWidth: lastResult?.imageWidth ?? 0,
                      imageHeight: lastResult?.imageHeight ?? 0,
                    ),
                    child: CameraPreview(controller!)),
          ),
          Text("Quality: ${matchResult?.quality}"),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton(
                onPressed: () async {
                  await process?.isReady;
                  if (controller?.value.isStreamingImages ?? false) {
                    await controller?.stopImageStream();
                    lastResult = null;
                  } else {
                    controller?.startImageStream((cameraImage) async {
                      if (!processingImage) {
                        processingImage = true;
                        process?.processCameraImage(cameraImage).then((result) {
                          setState(() {
                            lastResult = result;
                            processingImage = false;
                          });
                        });
                      }
                    });
                  }
                },
                child: Text((controller?.value.isStreamingImages ?? false)
                    ? 'STOP CAPTURE'
                    : 'START CAPTURE'),
              ),
              ElevatedButton(
                onPressed: () async {
                  if (lastResult != null) {
                    final result = onEnroll(lastResult!);
                    setState(() {
                      enrollResult = result;
                    });
                  }
                },
                child: const Text('ENROLL'),
              ),
              ElevatedButton(
                onPressed: () async {
                  if (lastResult != null && enrollResult != null) {
                    final result = onMatch(lastResult!, enrollResult!);

                    setState(() {
                      matchResult = result;
                    });
                  }
                },
                child: const Text('MATCH'),
              ),
            ],
          ),
          if (enrollResult != null)
            Image.memory(
              enrollResult!.croppedBytes,
              height: 200,
            ),
          Text("Quality: ${enrollResult?.quality}"),
          Text("Score: ${matchResult == null ? '' : matchResult?.score}"),
          Text(
              "Decision @FMR 10000: ${matchResult == null ? '' : matchResult!.score > sdk.FaceMatcherThreshold.fmr10000.value}"),
        ],
      ),
    );
  }
}

class EnrollResult {
  EnrollResult(this.croppedBytes, this.templateBytes, this.quality);

  final Uint8List croppedBytes;
  final int quality;
  final Uint8List templateBytes;
}

/// to enroll we need to extract template from face
/// for that we get the result from CaptureProcess and recreate native objects
/// we need a FaceEncoder, an image with associate DetectedFaced
EnrollResult onEnroll(CaptureProcessResult result) {
  final faceEncoder = sdk.FaceEncoder();
  faceEncoder.setModel(sdk.FaceModel.faceEncoder9B);

  final image =
      sdk.Image.fromBuffer(result.imageBytes, sdk.PixelFormat.bgr24Bits);

  final detectedFace = sdk.DetectedFace.fromBuffer(result.detectedFaceBytes);
  // get a cropped image of face with ICAO settings
  final croppingBounds = detectedFace.getPortraitBounds(0.25, 0.45, 1.33);
  final cropped = image.extractRoi(croppingBounds);
  cropped.flip(true, false);

  final jpg = cropped.toBuffer(sdk.ImageFormat.jpeg, 75);

  final template = faceEncoder.createTemplate(image, detectedFace);

  final quality = faceEncoder.computeQuality(image, detectedFace);

  return EnrollResult(
    jpg,
    template.toBuffer(),
    quality,
  );
}

class MatchResult {
  MatchResult(this.quality, this.score);

  final int quality;
  final int score;
}

MatchResult onMatch(
    CaptureProcessResult captureResult, EnrollResult enrollResult) {
  final faceEncoder = sdk.FaceEncoder();
  faceEncoder.setModel(sdk.FaceModel.faceEncoder9B);

  final image =
      sdk.Image.fromBuffer(captureResult.imageBytes, sdk.PixelFormat.bgr24Bits);

  final detectedFace =
      sdk.DetectedFace.fromBuffer(captureResult.detectedFaceBytes);

  final quality = faceEncoder.computeQuality(image, detectedFace);

  final template = faceEncoder.createTemplate(image, detectedFace);

  final faceMatcher = sdk.FaceMatcher();
  final refTemplate = sdk.FaceTemplate.fromBuffer(enrollResult.templateBytes);
  final score = faceMatcher.compareTemplates(refTemplate, template);

  return MatchResult(quality, score);
}
