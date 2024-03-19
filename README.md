# Face SDK samples

## Introduction

### Content

This repository contains basic samples for multiple features (mostly recognition, PAD and portrait quality analysis) and languages (C/C++, C#, Dart, Java, Swift) supported by the **id3 Technologies** Face SDK.

Going through those samples before trying to integrate it in your applications is strongly recommended.

### A word on version format

The version of this repository is made of 4 digits:
* The first 3 correspond to the version of the Face SDK that is currently supported in this repository.
* The forth digit contains updates dedicated to the samples (evolutions, bug fixes, doc, etc).

This strategy is employed to ensure version consistency among the various supported languages. When updating the Face SDK version, all the samples are updated as well.

For this release of the samples the version numbers are : 
* Samples version: **9.10.0.2**
* Required id3 Face SDK version: **9.10.0**

## Getting started

### Step 1: Get a license and the SDK

To run any of the samples, you need to get a license from **id3 Technologies**. For that, you must contact us at the following e-mail address: contact@id3.eu.

Then, once your request will be accepted, you will receive both a license activation key and a ZIP archive containing the SDK itself. Once you are here, you can move forward to step 2.

### Step 2: Install the SDK and models

Once you have the SDK ZIP archive, you need to unzip it in the *sdk/* subfolder resulting in the following architecture.

    .
    ├── android
    ├── cpp
    ...
    ├── flutter
    ├── sdk
        ├── activation
        ├── bin
        ...
        └── README.md
    └── README.md

Then you need to install the necessary models in the *models/* subfolder resulting in the following architecture. 

    .
    ├── android
    ├── cpp
    ...
    ├── flutter
    ├── models
        ├── eye_gaze_estimator_v2a.id3nn
        ├── eye_openness_detector_v1a.id3nn
        ...
        └── face_pose_estimator_v1a.id3nn
    ...
    ├── sdk
        ├── activation
        ├── bin
        ...
        ├── java
        └── README.md
    └── README.md

**Notes**
* The download address for models can be found in the SDK documentation (developer guide).
* All models are not mandatory. You do not need to download them all at once. For more details about the required models per sample, please refer to each sample's README file.

Once you are here, you can move forward to step 3.

### Step 3: Activate a license

The id3 Face SDK needs a license file to be used. To retrieve this license file you will need either:
- An id3 serial key (formatted as XXXX-XXXX-XXXX-XXXX): it allows activation (and further retrieval of the license file) on a single computer
- An id3 activation key (formatted as email-XXXXXXXX-XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX): it allows activations (and further retrievals of the license file) on several computers and can be re-credited upon request
- (A login / password / product reference) triplet: same behaviour as the activation key

This license file can be retrieved through different methods depending of your operating system:
- **On mobile systems:**
    - On most android and ios implementations, the system does not allow retrieval of a true unique hardware identifier to the application developers for privacy purposes. The side effect of this behavior is that you may get a different (but fixed) hardware code for each app you develop even on the same device.
    - As the hardware code you lock your id3 license on may be different on each app you need to retrieve the license through the app using the FaceLicense_Activate...() APIs
    - id3 recommends to run the activation at the first launch of the app and then store the license on the device for further uses, this is the behavior which is demonstrated in the mobile samples of this repository
    - For example the following code retrieves a license on android in Kotlin calling the Java API:
        ```kotlin
        val hardwareCode = FaceLicense.getHostHardwareCode(LicenseHardwareCodeType.ANDROID)
        var licenseBuffer: ByteArray? = null
        licenseBuffer = FaceLicense.activateSerialKeyBuffer(hardwareCode, "XXXX-XXXX-XXXX-XXXX", "Activated from Android")
        ```
    - Notes:
        - To use the activation APIs you must ensure that your application have the internet usage permission
        - When using the id3Face Flutter wrapper, the android license activation and check must be performed from a native android class, this behavior is demonstrated in the flutter samples of this repository
- **On Windows or Linux systems:**
    - Using the command line interface tool in *sdk/activation/cli-tool*
        - For example on Windows you can run:
            - Serial key activation: `.\sdk\activation\cli-tool\windows\x64\id3LicenseActivationCLI.exe --activate .\id3Face.lic --serialkey="XXXX-XXXX-XXXX-XXXX"`
            - Activation key activation: `.\sdk\activation\cli-tool\windows\x64\id3LicenseActivationCLI.exe --activate .\id3Face.lic --activationkey="email-XXXXXXXX-XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"`
            - Account activation: `.\sdk\activation\cli-tool\windows\x64\id3LicenseActivationCLI.exe --activate .\id3Face.lic --login="login@domain.com" --password="myPassword" --reference="XXXXXXXX"`
        - Same examples on Linux:
            - Serial key activation: `./sdk/activation/cli-tool/linux/x64/id3LicenseActivationCLI --activate ./id3Face.lic --serialkey="XXXX-XXXX-XXXX-XXXX"`
            - Activation key activation: `./sdk/activation/cli-tool/linux/x64/id3LicenseActivationCLI --activate ./id3Face.lic --activationkey="email-XXXXXXXX-XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"`
            - Account activation: `./sdk/activation/cli-tool/linux/x64/id3LicenseActivationCLI --activate ./id3Face.lic --login="login@domain.com" --password="myPassword" --reference="XXXXXXXX"`
    - Using the FaceLicense_Activate...() APIs from the SDK:
        - For example the following code retrieves a license file using the C# API:
            ```c#
            string hardwareCode = id3.Face.FaceLicense.GetHostHardwareCode(id3.Face.LicenseHardwareCodeType.WindowsOs);
            id3.Face.FaceLicense.ActivateSerialKey(hardwareCode,"XXXX-XXXX-XXXX-XXXX", "Activated through C# API", "id3Face.lic");
            ```
        - Please see API documentation for more details about usage (retrieve license as file, as buffer, ...)
    - Using the License Manager tool in *sdk/activation/windows-tool* (only on Windows)
    Save the license file under the root of samples with the name `id3Face.lic`.
    .
    ├── android
    ...
    ├── sdk
    ├── README.md
    └── id3Face.lic

### Step 4: Play around with the samples

You are now ready to go straight to the directory of your favorite language/platform which will contain a readme file with additional information on how to run the samples.

Sample code is heavily commented in order to give you an overview of the id3 Face SDK usage. We recommend you to read through the code while you run the samples.

## Troubleshooting

If you get stuck at any step of this procedure, please contact us: support@id3.eu.
