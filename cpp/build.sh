#!/bin/bash
mkdir -p build/release
cd build/release
cmake -DLINUX_BUILD=ON ..
make
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:../../../../bin/linux/x64
./id3FaceRecognitionSampleCLI