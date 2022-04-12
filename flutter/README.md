# Face SDK Flutter samples

This repository contains Flutter samples of **id3 Technologies** Face SDK.

## Requirements

First of all you must follow the upper README steps to get a license activation key and install the SDK for each platform where you want to run your app.

**Flutter SDK** is obviously needed to run the following samples, see instructions [here](https://docs.flutter.dev/get-started/install) for install.

## Special consideration regarding iOS support

The flutter samples support running on iOS device, however an additional configuration step is required.

The iOS id3Face SDK is delivered as a zip file containing an Apple XCFramework containing both desktop and mobile frameworks. 

For the flutter samples we need to extract the mobile version only, this can be done with the following steps:
- unzip the id3Face.xcframework.zip file
- copy the `sdk/bin/apple/id3Face.xcframework/ios-arm64_armv7_armv7s/id3Face.framework` folder to the `flutter/{chosen_sample}/ios/Framework` folder
- final structure should be `flutter/{chosen_sample}/ios/Framework/id3Face.framework/...`

For conveniency you can use the provided `extract_ios_frameworks.sh` script as such : 
```
cd flutter && sh extract_ios_frameworks.sh
```

## Models

Each sample uses deep learning from the Face SDK. They must be copied in the *assets/models/* folder of the sample before build, resulting in the following architecture:

    {project}
       └── assets
           ├── models
               ├── face_detector_v3b.id3nn
                ...
       ├── lib
       ...

The following models are required per project:
* Benchmark
    * face_detector_v3a.id3nn
    * face_detector_v3b.id3nn
    * face_encoder_v9a.id3nn
    * face_encoder_v9b.id3nn
    * face_encoding_quality_estimator_v3a.id3nn
* PAD
    * face_attack_support_detector_v2a.id3nn
    * face_blurriness_detector_v1a.id3nn
    * face_color_pad_v2a.id3nn
    * face_detector_v3b.id3nn
    * face_moire_detector_v1a.id3nn
* Recognition
    * face_detector_v3b.id3nn
    * face_encoder_v9b.id3nn
    * face_encoding_quality_estimator_v3a.id3nn
* Analysis
    * face_attributes_classifier_v2a.id3nn
    * face_detector_v3b.id3nn
    * face_occlusion_detector_v1a.id3nn
    * face_pose_estimator_v1a.id3nn

## License

Each project has a source file called **credentials.dart**. Replace the zeros by your valid license serial key in the following line:

    String serialKey = "0000-0000-0000-0000";

License can also be retrieved using a login/password/product reference triplet.

The license file will be downloaded into the app file system the first time you launch the sample, so you will need an internet connexion for the first run. Then it will be stored on the device and simply be checked by the sample without requiring an internet connexion anymore.

## Build and run

Once you have completed the above steps, the project is ready to be build and run on a plugged Android or IOS device!