package eu.id3.face.samples.padjava;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.id3.face.ColorBasedPadResult;
import eu.id3.face.DetectedFace;
import eu.id3.face.DetectedFaceAttackSupport;
import eu.id3.face.DetectedFaceList;
import eu.id3.face.FaceDetector;
import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceModel;
import eu.id3.face.FacePad;
import eu.id3.face.Image;
import eu.id3.face.ImageFormat;
import eu.id3.face.ProcessingUnit;
import eu.id3.face.Rectangle;

public class FaceProcessor {
    private FaceDetector faceDetector = null;
    private FacePad facePad = null;

    public FaceProcessor(Context context) {
        String LOG_TAG = "FaceProcessor";
        try {
            /*
             * Load a face detector.
             * First load the model from the Assets and then initialize the FaceDetector object.
             * Only one FaceDetector object is needed to perform all of your detection operation.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_detector_v3b.id3nn")),
                    FaceModel.FACE_DETECTOR_3B, ProcessingUnit.CPU
            );
            faceDetector = new FaceDetector();
            faceDetector.setConfidenceThreshold(Parameters.detectorConfidenceThreshold);
            faceDetector.setModel(FaceModel.FACE_DETECTOR_3B);
            faceDetector.setThreadCount(Parameters.detectorThreadCount);

            /**
             * Load a face presentation attack detector (PAD).
             * First load the models from the Assets and then initialize the FacePad object.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_attack_support_detector_v2a.id3nn")),
                    FaceModel.FACE_ATTACK_SUPPORT_DETECTOR_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_color_pad_v2a.id3nn")),
                    FaceModel.FACE_COLOR_BASED_PAD_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_blurriness_detector_v1a.id3nn")),
                    FaceModel.FACE_BLURRINESS_DETECTOR_1A, ProcessingUnit.CPU
            );
            facePad = new FacePad();
            Log.v(LOG_TAG, "Load models: OK !");
        } catch (FaceException | IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error while loading models: " + e.getMessage());
        }
    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyAllBytes(in, out);
        return out.toByteArray();
    }

    /**
     * Copies all available data from in to out without closing any stream.
     */
    public static void copyAllBytes(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[4096];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            out.write(buffer, 0, read);
        }
    }

    public DetectedFace detectLargestFace(eu.id3.face.Image image) {
        /* Track faces in the image. */
        DetectedFaceList detectedFaceList = faceDetector.detectFaces(image);
        if (detectedFaceList.getCount() > 0) {
            /* At least one face was detected! Return the largest one. */
            return detectedFaceList.getLargestFace();
        } else {
            /* No face was detected. */
            return null;
        }
    }

    public AnalyzeLargestFaceResult analyzeLargestFace(eu.id3.face.Image image, DetectedFace detectedFace) {
        try {
            /* Detects attack support if any. */
            DetectedFaceAttackSupport detectedAttackSupport = facePad.detectAttackSupport(image, detectedFace);

            /* Computes blurriness score. */
            int blurScore = facePad.computeBlurrinessScore(image, detectedFace);

            /* Computes color-based PAD score. */
            ColorBasedPadResult colorScoreResult = facePad.computeColorBasedScore(image, detectedFace);

            /* Extracts the portrait image of the detected face to display it. */
            Rectangle portraitBounds = detectedFace.getPortraitBounds(0.25f, 0.45f, 1.33f);
            Image portraitImage = image.extractRoi(portraitBounds);
            if (Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
                portraitImage.flip(true, false);
            }

            /* Compress the portrait image buffer as a JPEG buffer. */
            byte[] jpegPortraitImageBuffer = portraitImage.toBuffer(ImageFormat.JPEG, 75.0f);

            return new AnalyzeLargestFaceResult(
                    jpegPortraitImageBuffer,
                    detectedAttackSupport,
                    blurScore,
                    colorScoreResult.score,
                    colorScoreResult.confidence,
                    0
            );
        } catch (FaceException e) {
            return new AnalyzeLargestFaceResult(
                    null,
                    new DetectedFaceAttackSupport(),
                    0,
                    0,
                    0,
                    e.getErrorCode()
            );
        }

    }

    static class AnalyzeLargestFaceResult {
        private final byte[] jpegPortraitImageBuffer_;
        private final DetectedFaceAttackSupport detectedFaceAttackSupport_;
        private final int blurScore_;
        private final int colorScore_;
        private final int colorScoreConfidence_;
        private final int errorCode_;

        public AnalyzeLargestFaceResult(byte[] jpegPortraitImageBuffer,
                                        DetectedFaceAttackSupport detectedFaceAttackSupport,
                                        int blurScore,
                                        int colorScore,
                                        int colorScoreConfidence,
                                        int errorCode) {
            jpegPortraitImageBuffer_ = jpegPortraitImageBuffer;
            detectedFaceAttackSupport_ = detectedFaceAttackSupport;
            blurScore_ = blurScore;
            colorScore_ = colorScore;
            colorScoreConfidence_ = colorScoreConfidence;
            errorCode_ = errorCode;
        }

        public byte[] getJpegPortraitImageBuffer() {
            return jpegPortraitImageBuffer_;
        }

        public DetectedFaceAttackSupport getDetectedFaceAttackSupport() {
            return detectedFaceAttackSupport_;
        }

        public int getBlurScore() {
            return blurScore_;
        }

        public int getColorScore() {
            return colorScore_;
        }

        public int getColorScoreConfidence() {
            return colorScoreConfidence_;
        }

        public int getErrorCode() {
            return errorCode_;
        }
    }
}