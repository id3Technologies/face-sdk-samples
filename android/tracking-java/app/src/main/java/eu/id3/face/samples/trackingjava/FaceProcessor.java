package eu.id3.face.samples.trackingjava;

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
import eu.id3.face.FaceError;
import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceMatcher;
import eu.id3.face.FaceTracker;
import eu.id3.face.FaceModel;
import eu.id3.face.FaceTemplate;
import eu.id3.face.ImageFormat;
import eu.id3.face.ProcessingUnit;
import eu.id3.face.TrackedFaceList;

public class FaceProcessor {
    private FaceTracker faceTracker = null;
    private TrackedFaceList trackedFaceList = null;

    public FaceProcessor(Context context) {
        String LOG_TAG = "FaceProcessor";
        try {
            /*
             * Load and initialize a face tracker.
             *
             * A FaceTracker object needs two main models to be initialized:
             * - a face detector model (Default is FACE_DETECTOR_3B but can be adjusted according to
             * application needs)
             * - a face encoder model (Default is FACE_ENCODER_4B but can be adjusted according to
             * application needs)
             * First load the model from the Assets and then initialize the FaceTracker object.
             * Only one Facetracker object is needed to perform all of your tracking operation.
             */
            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_detector_v4b.id3nn")),
                    FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU
            );

            FaceLibrary.loadModelBuffer(
                    readAllBytes(context.getAssets().open("models/face_encoder_v9b.id3nn")),
                    FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU
            );
            Log.v(LOG_TAG, "Load models: OK !");

            faceTracker = new FaceTracker();

            // Set the face detector and encoder models
            faceTracker.setDetectionModel(FaceModel.FACE_DETECTOR_4B);
            faceTracker.setEncodingModel(FaceModel.FACE_ENCODER_9B);

            // The FaceTracker object has multiple parameters which must be tuned in order to respect
            // your application needs.

            // For example in this sample we raise the internal matching threshold of the tracker to ensure
            // the consistency of the tracked face IDs
            faceTracker.setMatchThreshold(4000);

            trackedFaceList = new TrackedFaceList();
            Log.v(LOG_TAG, "Load : OK !");
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

    public TrackedFaceList trackFaces(eu.id3.face.Image image) {
        // Usage of the face tracker is similar to a detector usage
        // A TrackedFaceList object is re-used on each frame to hold information on the tracked faces
        faceTracker.trackFaces(image,trackedFaceList);
        return trackedFaceList;
    }

    public void resetTrackedFaceList() {
        trackedFaceList.clear();
    }

}
