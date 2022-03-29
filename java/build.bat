rem Build the sample with the jar from the sdk
javac -cp ".;../sdk/java/eu.id3.face.jar" RecognitionCLI.java

rem Copy native library to current directory
copy ..\\sdk\\bin\\windows\\x64\\id3Face.dll id3Face.dll

rem Run the sample
java -cp ".;../sdk/java/eu.id3.face.jar" RecognitionCLI