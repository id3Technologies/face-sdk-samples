package eu.id3.id3_android_license

import androidx.annotation.NonNull
import eu.id3.face.*

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.FileNotFoundException

/** Id3AndroidLicensePlugin */
class Id3AndroidLicensePlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "id3_android_license")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
        "getHostHardwareCode" -> {
          val hardwareCode = License.getHostHardwareCode(LicenseHardwareCodeType.ANDROID)
          result.success(hardwareCode);
        }
        "checkLicense" -> {
          val licensePath: String = call.argument<String>("licensePath")!!
          try {
            FaceLibrary.checkLicense(licensePath)
            result.success(null)
          } catch (e: FileNotFoundException) {
            result.error("-1", e.message, "");
          } catch (e: FaceException) {
            result.error(e.errorCode.toString(), e.message, "")
          }
        }
        else -> {
          result.notImplemented()
        }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
