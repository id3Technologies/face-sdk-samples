import 'dart:async';

import 'package:flutter/services.dart';

class Id3AndroidLicense {
  static const MethodChannel _channel = MethodChannel('id3_android_license');

  static Future<String> getAndroidHardwareCode() async {
    return await _channel.invokeMethod('getHostHardwareCode') as String;
  }

  static Future<void> checkLicenseAndroid(String path) async {
    await _channel.invokeMethod('checkLicense', {'licensePath': path});
  }
}
