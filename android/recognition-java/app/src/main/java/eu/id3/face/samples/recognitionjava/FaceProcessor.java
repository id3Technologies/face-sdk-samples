package eu.id3.face.samples.recognitionjava;

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
import eu.id3.face.FaceEncoder;
import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceMatcher;
import eu.id3.face.FaceModel;
import eu.id3.face.FaceTemplate;
import eu.id3.face.ImageFormat;
import eu.id3.face.Portrait;
import eu.id3.face.PortraitProcessor;
import eu.id3.face.ProcessingUnit;

public class FaceProcessor {
    private FaceDetector faceDetector = null;
    private FaceEncoder faceEncoder = null;

    private PortraitProcessor processor = null;
    private Portrait portrait = null;

    private FaceTemplate enrolledTemplate;

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
             * Load a face encoder.
             * First load the model from the Assets and then initialize the FaceEncoder object.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_encoder_v9b.id3nn")),
                    FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_pose_estimator_v1a.id3nn")),
                    FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_occlusion_detector_v2a.id3nn")),
                    FaceModel.FACE_OCCLUSION_DETECTOR_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_attributes_classifier_v2a.id3nn")),
                    FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_landmarks_estimator_v2a.id3nn")),
                    FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU
            );
            faceEncoder = new FaceEncoder();
            faceEncoder.setModel(FaceModel.FACE_ENCODER_9B);
            faceEncoder.setThreadCount(Parameters.encoderThreadCount);

            processor = new PortraitProcessor();

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

    EnrollLargestFaceResult enrollLargestFace(eu.id3.face.Image image, DetectedFace detectedFace) {
        /* Create template of the detected face. */
        enrolledTemplate = faceEncoder.createTemplate(image, detectedFace);

        /* Compute template quality to make sure it will good enough for face recognition. */
        int quality = computeQuality(image);

        /* Extracts the portrait image of the detected face to display it. */
        eu.id3.face.Rectangle portraitBounds = detectedFace.getPortraitBounds(0.25f, 0.45f, 1.33f);
        eu.id3.face.Image portraitImage = image.extractRoi(portraitBounds);
        if (Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
            portraitImage.flip(true, false);
        }

        /* Compress the portrait image buffer as a JPEG buffer. */
        byte[] jpegPortraitImageBuffer = portraitImage.toBuffer(ImageFormat.JPEG, 75.0f);

        return new EnrollLargestFaceResult(jpegPortraitImageBuffer, quality);
    }

    VerifyLargestFaceResult verifyLargestFace(eu.id3.face.Image image, DetectedFace detectedFace) {
        /* Create template of the detected face. */
        FaceTemplate probeTemplate = faceEncoder.createTemplate(image, detectedFace);

        /* Compute template quality to make sure it will good enough for face recognition. */
        int quality = computeQuality(image);

        /* Initialize a face matcher and compare probe template to the previously enrolled one. */
        FaceMatcher faceMatcher = new FaceMatcher();
        int score = faceMatcher.compareTemplates(probeTemplate, enrolledTemplate);

        return new VerifyLargestFaceResult(quality, score);
    }

    int computeQuality(eu.id3.face.Image image) {
        portrait = processor.createPortrait(image);
        processor.detectOcclusions(portrait);
        processor.estimateFaceAttributes(portrait);
        processor.estimatePhotographicQuality(portrait);

        return portrait.getQualityScore();
    }

    static class EnrollLargestFaceResult {
        private final byte[] jpegPortraitImageBuffer_;
        private final int quality_;

        public EnrollLargestFaceResult(byte[] jpegPortraitImageBuffer, int quality) {
            jpegPortraitImageBuffer_ = jpegPortraitImageBuffer;
            quality_ = quality;
        }

        public byte[] getJpegPortraitImageBuffer() {
            return jpegPortraitImageBuffer_;
        }

        public int getQuality() {
            return quality_;
        }
    }

    class VerifyLargestFaceResult {
        private final int quality_;
        private final int score_;

        public VerifyLargestFaceResult(int quality, int score) {
            score_ = score;
            quality_ = quality;
        }

        public int getScore() {
            return score_;
        }

        public int getQuality() {
            return quality_;
        }
    }
}
