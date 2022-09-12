# Face SDK Android samples

This repository contains Android samples of **id3 Technologies** Face SDK. Implementation in Kotlin and Java are provided. Both are identical regarding id3 Face SDK usage.

## Requirements

First of all you must follow the upper README steps to get a license activation key and install the SDK.

Android samples require **Android Studio 2020** (or more recent) to be installed on your PC/Linux.

Once everything is setup you can proceed to the following steps.

## Models

Each sample uses deep learning models from the Face SDK. They must be copied in the *app/src/main/assets/* folder of the project before to build following resulting in the following architecture:

    {project}
    └── app
        ├── src
            ├── androidTest
            ├── main
                ├── assets
                    ├── models
                        ├── face_detector_v3b.id3nn
                        ...
                ├── java
                ├── res
                └── AndroidManifest.xml
            ...
        ...

The following models are required per project:
* Analysis
    * face_attributes_classifier_v2a.id3nn
    * face_detector_v3b.id3nn
    * face_occlusion_detector_v2a.id3nn
    * face_pose_estimator_v1a.id3nn
* PAD
    * face_attack_support_detector_v2a.id3nn
    * face_blurriness_detector_v1a.id3nn
    * face_color_pad_v2a.id3nn
    * face_detector_v3b.id3nn
* Recognition
    * face_detector_v3b.id3nn
    * face_encoder_v9b.id3nn
    * face_encoding_quality_estimator_v3a.id3nn

## License

Each project has a source file called **Credentials.kt** or **Credentials.java**. Replace the zeros by your valid license serial key in the following line:

    private fun getLicenseSerialKey() = "0000-0000-0000-0000"

The license file will be downloaded into the app file system the first time you launch the sample, so you will an internet connexion. 

Then it will simply be checked by the sample without requiring an internet connexion.

## Build and run

Once you have completed the above steps, the project is ready to be build and run on a plugged Android device!
