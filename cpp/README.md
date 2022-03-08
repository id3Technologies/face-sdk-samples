# C/C++ samples

This CMake project provides the following C/C++ samples: 
* **id3FaceRecognitionSampleCLI**

## Build requirements

* CMake >= 2.8.12
* [Linux] gcc
* [Windows] Visual Studio >= 15

## Before to build

There are some source code modifications to make before to build.

### License path

To run the sample you will need a license file. To retrieve this file you can use the CLI tool to get your hardware ID and then use either the windows activation tool to retrieve the file or contact id3 with it.

id3 Face SDK needs to check this license before any other operation. You need to fill in the path to your license in the source files:

    std::string license_path = "your_license_path_here";

### Models directory

id3 Face SDK is deeply based on machine learning methods and requires model files to perform image processing operations. Please read the provided documentation to understand which models you need and then download them from the following URL: https://cloud.id3.eu/index.php/s/y63PysbS3w6NPm6

For this sample you need:
- face_detector_v3b.id3nn
- face_encoder_v9b.id3nn

You can put the models wherever you want on your PC and then fill in the path to your models in the source files:
```c++    
std::string models_dir = "../../../sdk/models/";
```

## Linux build steps

### id3FaceRecognitionSampleCLI

To build and run **id3FaceRecognitionSampleCLI**, use the following command lines:

```bash
mkdir build
cd build
cmake -DLINUX_BUILD=ON ..
make
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:../../../../bin/linux/x64
./id3FaceRecognitionSampleCLI
```

## Windows build steps

### id3FaceRecognitionSampleCLI

To build and run **id3FaceRecognitionSampleCLI**, use the following command lines:
```bat
mkdir build
cd build
cmake -G "Visual Studio 15 2017 Win64" -DWINDOWS_BUILD=ON ..
cmake --build .
cd Debug
.\id3FaceRecognitionSampleCLI.exe
```