import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:id3_face/id3_face.dart' as sdk;
import 'package:path_provider/path_provider.dart';

import 'benchmark_page.dart';
import 'credentials.dart';
import 'utils.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  String hardwareCode;
  if (Platform.isAndroid) {
    hardwareCode = sdk.FaceLicense.getHostHardwareCode(sdk.LicenseHardwareCodeType.android);
  } else {
    hardwareCode = sdk.FaceLicense.getHostHardwareCode(sdk.LicenseHardwareCodeType.ios);
  }
  await activateLicense(hardwareCode);

  await loadBiometricParameters();
  await loadAllAssets();
  runApp(const App());
}

class App extends StatelessWidget {
  const App({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Benchmark Sample',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const BenchmarkPage(),
    );
  }
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

    if (login() != "login" && password() != "password" && productReference() != "00000000") {
      licenseBytes = sdk.FaceLicense.activateBuffer(
          hardwareCode, login(), password(), productReference(), "Activated from face capture sample");
    }
    if (!licenseFile.existsSync()) {
      licenseFile.createSync(recursive: true);
    }
    licenseFile.writeAsBytesSync(licenseBytes!);

    sdk.FaceLicense.checkLicense(licensePath);
  }
}
