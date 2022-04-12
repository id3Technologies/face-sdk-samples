import 'dart:io';
import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:id3_android_license/id3_android_license.dart';
import 'package:id3_face/id3_face.dart' as sdk;
import 'package:path_provider/path_provider.dart';
import 'bounds_painter.dart';
import 'capture_process.dart';
import 'constants.dart';
import 'credentials.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  String hardwareCode;
  if (Platform.isAndroid) {
    hardwareCode = await Id3AndroidLicense.getAndroidHardwareCode();
  } else {
    hardwareCode =
        sdk.License.getHostHardwareCode(sdk.LicenseHardwareCodeType.iOS);
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
  final licensePath = (await getTemporaryDirectory()).path +
      '/id3/id3license/id3license_$productReference.lic';
  final licenseFile = File(licensePath);
  try {
    if (Platform.isAndroid) {
      await Id3AndroidLicense.checkLicenseAndroid(licensePath);
    } else {
      sdk.FaceLibrary.checkLicense(licensePath);
    }
  } catch (_) {
    Uint8List? licenseBytes;
    if (serialKey != "0000-0000-0000-0000") {
      licenseBytes = sdk.License.activateSerialKeyBuffer(
        hardwareCode,
        serialKey,
        "Activated from Face capture sample",
      );
    }

    if (login != "login" &&
        password != "password" &&
        productReference != "00000000") {
      licenseBytes = sdk.License.activateBuffer(hardwareCode, login, password,
          productReference, "Activated from face capture sample");
    }
    if (!licenseFile.existsSync()) {
      licenseFile.createSync(recursive: true);
    }
    licenseFile.writeAsBytesSync(licenseBytes!);
    if (Platform.isAndroid) {
      await Id3AndroidLicense.checkLicenseAndroid(licensePath);
    } else {
      sdk.FaceLibrary.checkLicense(licensePath);
    }
  }
}

Future<void> loadModels() async {
  final faceDetector =
      await rootBundle.load('assets/models/face_detector_v3b.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceDetector.buffer.asUint8List(),
    sdk.FaceModel.faceDetector3B,
    sdk.ProcessingUnit.cpu,
  );

  final faceAttackSupportDetector = await rootBundle
      .load('assets/models/face_attack_support_detector_v1a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceAttackSupportDetector.buffer.asUint8List(),
    sdk.FaceModel.faceAttackSupportDetector2A,
    sdk.ProcessingUnit.cpu,
  );
  final faceBlurrinessDetector =
      await rootBundle.load('assets/models/face_blurriness_detector_v1a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceBlurrinessDetector.buffer.asUint8List(),
    sdk.FaceModel.faceBlurrinessDetector1A,
    sdk.ProcessingUnit.cpu,
  );
  final faceColorPad =
      await rootBundle.load('assets/models/face_color_pad_v2a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceColorPad.buffer.asUint8List(),
    sdk.FaceModel.faceColorBasedPad2A,
    sdk.ProcessingUnit.cpu,
  );
  final faceMoireDetector =
      await rootBundle.load('assets/models/face_moire_detector_v1a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceMoireDetector.buffer.asUint8List(),
    sdk.FaceModel.faceMoireDetector1A,
    sdk.ProcessingUnit.cpu,
  );
}

class App extends StatelessWidget {
  const App({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Pad Sample',
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
  Future<AnalyzeFaceResult>? analyzeResultFuture;
  CameraController? controller;
  CaptureProcessResult? lastResult;
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
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton(
                onPressed: () async {
                  await process?.isReady;
                  if (controller?.value.isStreamingImages ?? false) {
                    await controller?.stopImageStream();
                    setState(() {
                      lastResult = null;
                    });
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
                    //  final result = await ;
                    setState(() {
                      analyzeResultFuture = compute(onAnalyze, lastResult!);
                    });
                  }
                },
                child: const Text('ANALYZE FACE'),
              ),
            ],
          ),
          const SizedBox(height: 32),
          FutureBuilder<AnalyzeFaceResult>(
            future: analyzeResultFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const CircularProgressIndicator();
              } else if (snapshot.connectionState == ConnectionState.done) {
                final analyzeResult = snapshot.data!;
                return Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Image.memory(
                      analyzeResult.portraitImage,
                      width: 100,
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        PadResult(
                          spoofCondition: analyzeResult.isAttackSpoof,
                          bonaFideText: "No attack support",
                          spoofText:
                              "Attack support detected ${analyzeResult.detectedAttackSupport.name}",
                        ),
                        PadResult(
                          spoofCondition: analyzeResult.isBlurSpoof,
                          bonaFideText: "Not blurry",
                          spoofText: "Blurry",
                        ),
                        PadResult(
                          spoofCondition: analyzeResult.isColorSpoof,
                          bonaFideText: "Authentic colors",
                          spoofText: "Non-authentic colors",
                        ),
                        PadResult(
                          spoofCondition: analyzeResult.isMoireSpoof,
                          bonaFideText: "No moire effect",
                          spoofText: "Moire effect detected",
                        ),
                        PadResult(
                          spoofCondition: analyzeResult.isSpoof,
                          bonaFideText: "The presentation is bona-fide",
                          spoofText: "The presentation is an attack",
                        ),
                      ],
                    )
                  ],
                );
              } else {
                return const SizedBox.shrink();
              }
            },
          ),
        ],
      ),
    );
  }
}

class PadResult extends StatelessWidget {
  const PadResult({
    Key? key,
    required this.spoofCondition,
    required this.bonaFideText,
    required this.spoofText,
  }) : super(key: key);

  final String bonaFideText;
  final bool spoofCondition;
  final String spoofText;

  @override
  Widget build(BuildContext context) {
    return RichText(
      text: TextSpan(
        style: const TextStyle(color: Colors.black),
        children: spoofCondition
            ? [
                const TextSpan(
                  text: "NOT OK",
                  style: TextStyle(color: Colors.red),
                ),
                TextSpan(text: ' : $spoofText'),
              ]
            : [
                const TextSpan(
                  text: "OK",
                  style: TextStyle(color: Colors.green),
                ),
                TextSpan(text: ' : $bonaFideText'),
              ],
      ),
    );
  }
}

class AnalyzeFaceResult {
  AnalyzeFaceResult({
    required this.portraitImage,
    required this.blurScore,
    required this.colorScore,
    required this.colorScoreConfidence,
    required this.detectedAttackSupport,
    required this.moireScore,
    this.errorCode = 0,
  });

  factory AnalyzeFaceResult.error(int errorCode) {
    return AnalyzeFaceResult(
      portraitImage: Uint8List(0),
      blurScore: 0,
      colorScore: 0,
      colorScoreConfidence: 0,
      detectedAttackSupport: sdk.FaceAttackSupport.none,
      moireScore: 0,
      errorCode: errorCode,
    );
  }

  final int blurScore;
  final int colorScore;
  final int colorScoreConfidence;
  final sdk.FaceAttackSupport detectedAttackSupport;
  final int errorCode;
  final int moireScore;
  final Uint8List portraitImage;

  bool get isAttackSpoof => detectedAttackSupport != sdk.FaceAttackSupport.none;

  bool get isBlurSpoof => blurScore >= blurScoreMaxThreshold;

  bool get isColorSpoof => colorScore < colorScoreThreshold;

  bool get isMoireSpoof => moireScore >= moireScoreThreshold;

  bool get isSpoof =>
      isAttackSpoof || isBlurSpoof || isColorSpoof || isMoireSpoof;
}

AnalyzeFaceResult onAnalyze(CaptureProcessResult result) {
  final facePad = sdk.FacePad();
  try {
    final image =
        sdk.Image.fromBuffer(result.imageBytes, sdk.PixelFormat.bgr24Bits);

    final detectedFace = sdk.DetectedFace.fromBuffer(result.detectedFaceBytes);

    final croppingBounds = detectedFace.getPortraitBounds(0.25, 0.45, 1.33);

    final cropped = image.extractRoi(croppingBounds);

    cropped.flip(true, false);

    final detectedAttackSupport =
        facePad.detectAttackSupport(image, detectedFace);

    final faceAttackSupport = sdk.FaceAttackSupportX.fromValue(
      detectedAttackSupport.struct.AttackSupport,
    );

    final blurScore = facePad.computeBlurrinessScore(image, detectedFace);

    final colorScoreResults =
        facePad.computeColorBasedScore(image, detectedFace);
    int colorScore = colorScoreResults.struct.Score;
    int colorScoreConfidence = colorScoreResults.struct.Confidence;

    final moireScore = facePad.computeMoireScore(image, detectedFace);

    final jpg = cropped.toBuffer(sdk.ImageFormat.jpeg, 75);

    image.dispose();
    detectedFace.dispose();
    croppingBounds.dispose();
    cropped.dispose();
    detectedAttackSupport.dispose();
    colorScoreResults.dispose();

    return AnalyzeFaceResult(
      portraitImage: jpg,
      blurScore: blurScore,
      colorScore: colorScore,
      colorScoreConfidence: colorScoreConfidence,
      detectedAttackSupport: faceAttackSupport,
      moireScore: moireScore,
    );
  } on sdk.FaceException catch (e) {
    return AnalyzeFaceResult.error(e.errorCode);
  } finally {
    facePad.dispose();
  }
}
