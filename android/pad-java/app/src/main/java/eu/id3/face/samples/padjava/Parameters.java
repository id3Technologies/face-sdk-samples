package eu.id3.face.samples.padjava;

import android.hardware.camera2.CameraCharacteristics;

public class Parameters {
    public final static int detectorThreadCount = 4;
    public final static int detectorConfidenceThreshold = 70;
    public final static int blurScoreMaxThreshold = 20;
    public final static int colorScoreThreshold = 90;
    public final static int colorScoreConfidenceThreshold = 70;
    public final static int maxProcessingImageSize = 512;
    public final static int cameraType = CameraCharacteristics.LENS_FACING_FRONT;
}
