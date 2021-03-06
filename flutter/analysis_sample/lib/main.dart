import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:id3_android_license/id3_android_license.dart';
import 'package:id3_face/id3_face.dart' as sdk;
import 'package:path_provider/path_provider.dart';

import 'analyze_page.dart';
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
  final faceAttackSupportDetector =
      await rootBundle.load('assets/models/face_pose_estimator_v1a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceAttackSupportDetector.buffer.asUint8List(),
    sdk.FaceModel.facePoseEstimator1A,
    sdk.ProcessingUnit.cpu,
  );
  final faceBlurrinessDetector =
      await rootBundle.load('assets/models/face_occlusion_detector_v1a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceBlurrinessDetector.buffer.asUint8List(),
    sdk.FaceModel.faceOcclusionDetector1A,
    sdk.ProcessingUnit.cpu,
  );
  final faceColorPad = await rootBundle
      .load('assets/models/face_attributes_classifier_v2a.id3nn');
  sdk.FaceLibrary.loadModelBuffer(
    faceColorPad.buffer.asUint8List(),
    sdk.FaceModel.faceAttributesClassifier2A,
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
      home: const AnalyzePage(),
    );
  }
}
