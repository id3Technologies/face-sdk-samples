# Face SDK Swift iOS samples

This repository contains a swift iOS sample of **id3 Technologies** Face SDK.

This sample does not contains either the id3Face SDK framework or the required model files, to launch the sample please perform the following steps:

1. Extract the id3Face.xcframework folder from the `sdk/bin/apple/id3Face.xcframework.zip` file
2. Open the sample project in Xcode and add the id3Face.xcframework file in the "Frameworks, Librairies and Embedded Content section, select "embed and sign"
3. Add the required model files in the Assets.xcassets section of the sample:
    - face_detector_v3b.id3nn
    - face_encoding_quality_estimator_v3a.id3nn
    - face_encoder_v9b.id3nn
4. Rename `Credentials.swift.template'
4. Update the `Credentials.swift` source file with either a serial key or your id3 account credentials : You may overwrite the values in the code or set the following environment variables :
```
export ID3_LICENSE_SERIAL_KEY=<value>
export ID3_ACCOUNT_LOGIN=<value>
export ID3_ACCOUNT_PASSWORD=<value>
export ID3_PACKAGE_REFERENCE=<value>
```
