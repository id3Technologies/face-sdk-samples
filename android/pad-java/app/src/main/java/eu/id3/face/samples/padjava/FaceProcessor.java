package eu.id3.face.samples.padjava;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.id3.face.DetectedFace;
import eu.id3.face.DetectedFaceList;
import eu.id3.face.FaceDetector;
import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceModel;
import eu.id3.face.Image;
import eu.id3.face.ImageFormat;
import eu.id3.face.PadStatus;
import eu.id3.face.Portrait;
import eu.id3.face.PortraitInstruction;
import eu.id3.face.PortraitProcessor;
import eu.id3.face.ProcessingUnit;
import eu.id3.face.Rectangle;

public class FaceProcessor {
    private FaceDetector faceDetector = null;

    private PortraitProcessor processor;
    private Portrait portrait;
    private boolean portraitCreated;

    public FaceProcessor(Context context) {
        String LOG_TAG = "FaceProcessor";
        try {
            /*
             * Load a face detector.
             * First load the model from the Assets and then initialize the FaceDetector object.
             * Only one FaceDetector object is needed to perform all of your detection operation.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_detector_v4b.id3nn")),
                    FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU
            );
            faceDetector = new FaceDetector();
            faceDetector.setConfidenceThreshold(Parameters.detectorConfidenceThreshold);
            faceDetector.setModel(FaceModel.FACE_DETECTOR_4B);
            faceDetector.setThreadCount(Parameters.detectorThreadCount);

            /*
             * Load portrait processor.
             * First load the models from the Assets and then initialize the PortraitProcessor object.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_encoder_v10b.id3nn")),
                    FaceModel.FACE_ENCODER_10B, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_landmarks_estimator_v2a.id3nn")),
                    FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_pose_estimator_v1a.id3nn")),
                    FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_color_pad_v3a.id3nn")),
                    FaceModel.FACE_COLOR_BASED_PAD_4A, ProcessingUnit.CPU
            );

            processor = new PortraitProcessor();
            portraitCreated = false;

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

    public void resetPortrait() {
        portraitCreated = false;
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
            /* Initialize or update portrait */
            if (!portraitCreated)
            {
                portrait = new Portrait();
                portraitCreated = true;
            }

            /* Detects attack support with portrait processor. */
            processor.updatePortrait(portrait, image);
            processor.estimatePhotographicQuality(portrait);
            processor.detectPresentationAttack(portrait);

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
                    portrait.getInstruction(),
                    portrait.getPadStatus(),
                    portrait.getPadScore(),
                    0
            );
        } catch (FaceException e) {
            return new AnalyzeLargestFaceResult(
                    null,
                    PortraitInstruction.NONE,
                    PadStatus.UNKNOWN,
                    0,
                    e.getErrorCode()
            );
        }
    }

    static class AnalyzeLargestFaceResult {
        private final byte[] jpegPortraitImageBuffer_;
        private final PortraitInstruction instruction_;
        private final PadStatus status_;
        private final int score_;
        private final int errorCode_;

        public AnalyzeLargestFaceResult(byte[] jpegPortraitImageBuffer,
                                        PortraitInstruction instruction,
                                        PadStatus status,
                                        int score,
                                        int errorCode) {
            jpegPortraitImageBuffer_ = jpegPortraitImageBuffer;
            instruction_ = instruction;
            status_ = status;
            score_ = score;
            errorCode_ = errorCode;
        }

        public byte[] getJpegPortraitImageBuffer() {
            return jpegPortraitImageBuffer_;
        }

        public PortraitInstruction getInstruction() {
            return instruction_;
        }

        public PadStatus getStatus() {
            return status_;
        }

        public int getScore() {
            return score_;
        }

        public int getErrorCode() {
            return errorCode_;
        }
    }
}