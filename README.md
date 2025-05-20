# Face SDK Samples

[![License](https://img.shields.io/badge/license-Commercial-red)](https://id3.eu)
[![Version](https://img.shields.io/badge/version-9.15.3.4-blue)](README.md)

## Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [License Activation](#license-activation)
- [Sample Projects Overview](#sample-projects-overview)
  - [Android](#android)
  - [C++](#cpp)
  - [C#/.NET](#net)
  - [Flutter](#flutter)
  - [Java](#java)
  - [Python](#python)
  - [Swift](#swift)
- [Troubleshooting](#troubleshooting)
- [Support](#support)
- [License](#license)

## Overview

The id3 Technologies Face SDK Samples repository provides a collection of example implementations showcasing various features of the id3 Face SDK. These samples are designed to help developers quickly understand and integrate the SDK into their applications.

> 📘 **SDK Documentation**: For complete API reference and detailed documentation, visit the [id3 Face SDK Developer Portal](https://developers.id3.eu/face/).

### Features Demonstrated

- Face Recognition
- Presentation Attack Detection (PAD)
- Face Analysis
- Portrait Processing
- Real-time Tracking
- Image Compression

### Supported Languages

- Android (Kotlin & Java)
- C++
- C#/.NET
- Flutter
- Java
- Python
- Swift

### Version Information

The repository version follows a 4-digit format:

- First 3 digits: Face SDK version
- Last digit: Sample updates (evolutions, bug fixes, documentation)

Current versions:
- Samples version: **9.15.3.4**
- Required id3 Face SDK version: **9.15.3**

## Getting Started

### Prerequisites

1. **License and SDK**
   - Contact us at <contact@id3.eu> to obtain:
     - License activation key
     - SDK ZIP archive

2. **Development Environment**
   - Ensure you have the required development tools for your chosen language:
     - Android: Android Studio 2020+
     - C++: CMake >= 2.8.12, GCC (Linux) or Visual Studio >= 15 (Windows)
     - .NET: Microsoft Visual Studio 2017+
     - Flutter: Flutter SDK
     - Java: Java environment with java and javac tools
     - Python: Python 3.9-3.11 (Windows) or appropriate version (Linux)
     - Swift: Xcode (iOS)

### Installation

1. **SDK Setup**
   - Unzip the SDK archive to the `sdk/` subfolder:
   ```
   .
   ├── sdk/
   │   ├── activation/
   │   ├── bin/
   │   └── README.md
   ```

2. **Models Setup**
   - Download required models from the SDK documentation
   - Place models in the `models/` directory:
   ```
   .
   ├── models/
   │   ├── eye_gaze_estimator_v2a.id3nn
   │   ├── eye_openness_detector_v1a.id3nn
   │   └── face_pose_estimator_v1a.id3nn
   ```
   - Note: Not all models are required - check each sample's README for specific requirements

### License Activation

The id3 Face SDK requires a valid license file. You can activate your license using one of the following methods:

#### Mobile Systems (Android/iOS)
```kotlin
// Example for Android in Kotlin
val hardwareCode = FaceLicense.getHostHardwareCode(LicenseHardwareCodeType.ANDROID)
val licenseBuffer = FaceLicense.activateActivationKeyBuffer(
    hardwareCode, 
    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    "Activated from Android"
)
```

#### Windows/Linux Systems
- **Command Line Interface** (in `sdk/activation/cli-tool/`):
  ```bash
  # Windows example
  .\id3LicenseActivationCLI.exe --activate .\id3Face.lic \
      --activationkey="XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
  ```

- **API Activation** (example in C#):
  ```csharp
  string hardwareCode = FaceLicense.GetHostHardwareCode(LicenseHardwareCodeType.WindowsOs);
  FaceLicense.ActivateActivationKey(
      hardwareCode,
      "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
      "Activated through C# API",
      "id3Face.lic"
  );
  ```

#### Windows License Manager (Alternative Method)

If you're on Windows, you can also use the License Manager tool located in `sdk/activation/windows-tool/` to manage your license. Save the license file in the root of the samples directory as `id3Face.lic`.

### Running Your First Sample

1. **Choose a Sample**
   - Browse the [Sample Projects Overview](#sample-projects-overview) below
   - Select a sample that matches your target platform and language

2. **Follow the Sample's README**
   - Each sample has its own README with specific setup instructions
   - Follow the build and run steps carefully

3. **Explore the Code**
   - All samples are heavily commented to help you understand the SDK usage
   - We recommend reading through the code while running the samples

## Sample Projects Overview

This repository contains sample projects in multiple programming languages to help you get started with the id3 Face SDK. Each sample demonstrates different aspects of the SDK's capabilities.

### Android Samples

| Sample | Description | Language |
|--------|-------------|----------|
| Analysis Sample | Face analysis with detection, attributes classification, and pose estimation | Kotlin & Java |
| PAD Sample | Presentation Attack Detection implementation | Kotlin & Java |
| Portrait Processor | Advanced portrait processing functionality | Kotlin & Java |
| Recognition Sample | Face recognition with detection and encoding | Kotlin & Java |
| Recognition with CameraX | Advanced face recognition using CameraX API | Kotlin |
| Tracking Sample | Real-time face tracking implementation | Kotlin & Java |

### C++ Samples

| Sample | Description | Type |
|--------|-------------|------|
| id3FaceRecognitionSampleCLI | Face recognition command-line interface | CLI |
| id3PortraitProcessorSampleCLI | Portrait processing command-line tool | CLI |

### .NET Samples

| Sample | Description | Type |
|--------|-------------|------|
| CompressToWebpCLI | WebP image compression | CLI |
| PadWF | Presentation Attack Detection | Windows Forms |
| PortraitProcessorCLI | Portrait processing | CLI |
| RecognitionCLI | Face recognition | CLI |
| RecognitionWF | Face recognition | Windows Forms |
| RecognitionWPF1toN | 1:N face recognition | WPF |

### Flutter Samples

| Sample | Description | Type |
|--------|-------------|------|
| Analysis Sample | Face analysis implementation | Mobile |
| Benchmark Sample | Performance benchmarking | Mobile |
| PAD Sample | Presentation Attack Detection | Mobile |
| Recognition Sample | Face recognition | Mobile |

### Java Samples

| Sample | Description | Type |
|--------|-------------|------|
| CompressToWebpCLI | WebP image compression | CLI |
| PortraitProcessorCLI | Portrait processing | CLI |
| RecognitionCLI | Face recognition | CLI |

### Python Samples

| Sample | Description | Type |
|--------|-------------|------|
| face_analyse.py | Face analysis with detection and landmarks | CLI |
| face_pad.py | Presentation Attack Detection | CLI |
| face_recognition.py | Face recognition implementation | CLI |
| portrait_processor.py | Portrait processing | CLI |

### Swift Samples

| Sample | Description | Platform |
|--------|-------------|----------|
| iOS Samples | Face recognition and analysis | iOS |
- **iOS Samples**: Native iOS implementations showcasing face recognition and analysis capabilities on Apple devices.

## Troubleshooting

If you encounter any issues, please check:

1. **License Issues**
   - Ensure your license file is correctly placed
   - Verify the license activation key format
   - Check for internet connectivity during activation

2. **SDK Installation**
   - Confirm all required models are present
   - Verify SDK version compatibility
   - Check environment variables are set correctly

3. **Development Environment**
   - Ensure all required dependencies are installed
   - Verify development tools are up to date
   - Check system architecture compatibility

If you still have issues, please contact our support team at <support@id3.eu> with:
- The error message you're encountering
- Your system configuration
- The SDK version you're using
- Any relevant log files

## Support

For support, please contact:
- Email: <support@id3.eu>
- Website: [id3technologies.com](https://id3technologies.com)

## License

This software is licensed for commercial use only. For licensing inquiries, please contact:
- Email: <contact@id3.eu>
- Website: [id3technologies.com](https://id3technologies.com)

## Contributing

We welcome contributions from the community. Please follow these guidelines:
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## Changelog

For detailed changes in each version, see the [CHANGELOG.md](CHANGELOG.md) file.

## Acknowledgments

- id3 Technologies development team
- All contributors and users who have provided feedback and suggestions
