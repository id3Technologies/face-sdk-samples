package eu.id3.face.samples.analysisjava;

import android.hardware.camera2.CameraCharacteristics;

public class Parameters {
    public final static int detectorThreadCount = 4;
    public final static int detectorConfidenceThreshold = 70;
    public final static int yawMaxThreshold = 10;
    public final static int pitchMaxThreshold = 20;
    public final static int rollMaxThreshold = 10;

    public final static int eyeOcclusionMaxThreshold = 50;
    public final static int noseOcclusionMaxThreshold = 50;
    public final static int mouthOcclusionMaxThreshold = 30;

    public final static int hatMaxThreshold = 10;
    public final static int mouthOpenMaxThreshold = 35;
    public final static int smileMaxThreshold = 75;

    public final static int maxProcessingImageSize = 512;
    public final static int cameraType = CameraCharacteristics.LENS_FACING_FRONT;
}
