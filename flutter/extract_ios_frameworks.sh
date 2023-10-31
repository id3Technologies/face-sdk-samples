#!/bin/sh
samples=("recognition_sample" "pad_sample" "benchmark_sample" "analysis_sample")
unzip -q -o ../sdk/bin/apple/id3Face.xcframework.zip -d ../sdk/bin/apple/ 
for sample in ${samples[@]}; do
  cp -r ../sdk/bin/apple/id3Face.xcframework/ios-arm64/id3Face.framework ./$sample/ios/Frameworks/
done