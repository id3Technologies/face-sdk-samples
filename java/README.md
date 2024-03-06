# Face SDK Java samples

This repository contains Java samples of **id3 Technologies** Face SDK.

## Requirements

First of all you must follow the upper README steps to get a license file and install the SDK.

A Java environnment must be available on the system with java and javac tools in path.

Once everything is setup you can proceed to the following steps.

## Building the solution

### Filling the license path

Before to build any of the JAVA samples, you need to fill in the path to your license in the source code. Look for the following line in **RecognitionCLI.java** and overwrite `licensePath` with your correct path.

```
std::string license_path = "id3Face.lic";
```

### Ensuring models are present

Following models are required to be in the sdk/models/ directory:
- For RecognitionCLI sample:
    - face_detector_v3b.id3nn
    - face_encoder_v9a.id3nn

### Build and run
Samples build will directly use the java and javac tools.
For conveniency a .bat windows script and a .sh linux script are provided.