rem Copy native library to current directory
copy ..\\sdk\\bin\\windows\\x64\\id3Face.dll id3Face.dll

rem Build and run samples with the jar from the sdk

javac -cp ".;../sdk/java/eu.id3.face.jar" RecognitionCLI.java
java  -cp ".;../sdk/java/eu.id3.face.jar" RecognitionCLI

javac -cp ".;../sdk/java/eu.id3.face.jar" CompressToWebpCLI.java
java  -cp ".;../sdk/java/eu.id3.face.jar" CompressToWebpCLI

javac -cp ".;../sdk/java/eu.id3.face.jar" PortraitProcessorCLI.java
java  -cp ".;../sdk/java/eu.id3.face.jar" PortraitProcessorCLI