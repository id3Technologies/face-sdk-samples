package eu.id3.face.samples.portraitprocessorjava;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.id3.face.DetectedFace;
import eu.id3.face.DetectedFaceList;
import eu.id3.face.EyeGaze;
import eu.id3.face.FaceDetector;
import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceModel;
import eu.id3.face.GeometricAttributes;
import eu.id3.face.Image;
import eu.id3.face.ImageFormat;
import eu.id3.face.PointList;
import eu.id3.face.Portrait;
import eu.id3.face.PortraitProcessor;
import eu.id3.face.PortraitQualityCheckpoints;
import eu.id3.face.ProcessingUnit;
import eu.id3.face.Rectangle;

public class FaceProcessor {
    private FaceDetector faceDetector = null;

    PortraitProcessor processor = new PortraitProcessor();
    private Portrait portrait;
    boolean portraitCreated = false;

    public FaceProcessor(Context context) {
        String LOG_TAG = "FaceProcessor";
        try {
            /*
             * Load a face detector.
             * First load the models from the Assets and then initialize the FaceDetector object.
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
             * Load other face analysis models.
             */
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
                    readAllBytes(context.getAssets().open("models/face_encoder_v10b.id3nn")),
                    FaceModel.FACE_ENCODER_10B, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_landmarks_estimator_v2a.id3nn")),
                    FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_age_estimator_v1a.id3nn")),
                    FaceModel.FACE_AGE_ESTIMATOR_1A, ProcessingUnit.CPU
            );
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_expression_classifier_v1a.id3nn")),
                    FaceModel.FACE_EXPRESSION_CLASSIFIER_1A, ProcessingUnit.CPU
            );

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

    public void resetPortrait()
    {
        portraitCreated = false;
    }

    AnalyzeLargestFaceResult analyzeLargestFace(eu.id3.face.Image image, DetectedFace detectedFace) {

        String LOG_TAG = "PortraitProcessor";

        PortraitAttributes portraitAttributes = new PortraitAttributes();

        try {
            if (!portraitCreated) {
                portrait = processor.createPortrait(image);
                portraitCreated = true;
            }
            else
                processor.updatePortrait(portrait, image);

            /* Get age estimation. */
            processor.estimateAge(portrait);
            portraitAttributes.setAge(portrait.getAge());

            /* Get expression estimation. */
            processor.estimateExpression(portrait);
            portraitAttributes.setExpression(portrait.getExpression());

            /* Get ICAO geometric attributes. */
            processor.estimateGeometryQuality(portrait);
            GeometricAttributes geomAttributes = portrait.getGeometricAttributes();

            /* Get face landmarks. */
            PointList landmarks = portrait.getLandmarks();

            /* Get face attributes. */
            processor.detectOcclusions(portrait);
            processor.estimateFaceAttributes(portrait);

            EyeGaze eyeGaze = portrait.getEyeGaze();

            /* Get ICAO portrait attributes */
            portraitAttributes.setPose(portrait.getPose());
            portraitAttributes.setLeftEyeVisibilityScore(portrait.getLeftEyeVisibility());
            portraitAttributes.setRightEyeVisibilityScore(portrait.getRightEyeVisibility());
            portraitAttributes.setMouthVisibilityScore(portrait.getMouthVisibility());
            portraitAttributes.setNoseVisibilityScore(portrait.getNoseVisibility());
            portraitAttributes.setGlassesVisibilityScore(portrait.getGlasses());
            portraitAttributes.setGenderMaleScore(portrait.getGenderMale());
            portraitAttributes.setHatScore(portrait.getHat());
            portraitAttributes.setSmileScore(portrait.getSmile());
            portraitAttributes.setMouthOpenScore(portrait.getMouthOpening());

            /*
             * get ICAO criterias statuses
             * NOTE: must be called after all estimations
             */
            processor.estimatePhotographicQuality(portrait);
            PortraitQualityCheckpoints icaoCheckpoints = portrait.getQualityCheckpoints();

            /*
             * get global quality score
             * NOTE: must be called after all estimations
             */
            portraitAttributes.setQualityScore(portrait.getQualityScore());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error while processing portrait: " + e.getMessage());
        }

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
                portraitAttributes,
                0
        );
    }

    class AnalyzeLargestFaceResult {
        private final byte[] jpegPortraitImageBuffer_;
        private final PortraitAttributes portraitAttributes_;
        private final int errorCode_;

        public AnalyzeLargestFaceResult(byte[] jpegPortraitImageBuffer,
                                        PortraitAttributes portraitAttributes,
                                        int errorCode) {
            jpegPortraitImageBuffer_ = jpegPortraitImageBuffer;
            portraitAttributes_ = portraitAttributes;
            errorCode_ = errorCode;
        }

        public byte[] getJpegPortraitImageBuffer() {
            return jpegPortraitImageBuffer_;
        }

        public PortraitAttributes getPortraitAttributes() {
            return portraitAttributes_;
        }

        public int getErrorCode() {
            return errorCode_;
        }
    }
}
