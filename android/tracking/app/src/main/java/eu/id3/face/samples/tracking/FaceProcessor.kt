package eu.id3.face.samples.tracking

import android.content.Context
import android.media.Image
import android.util.Log
import eu.id3.face.*
import java.nio.ByteBuffer

private const val LOG_TAG = "FaceProcessor"

class FaceProcessor(context: Context) {

    private lateinit var faceTracker: FaceTracker
    private var trackedFaceList: TrackedFaceList? = null
    init {
        try {
            /*
             * Load and initialize a face tracker.
             *
             * A FaceTracker object needs two main models to be initialized:
             * - a face detector model (Default is FACE_DETECTOR_3B but can be adjusted according to
             * application needs)
             * - a face encoder model (Default is FACE_ENCODER_3B but can be adjusted according to
             * application needs)
             * First load the model from the Assets and then initialize the FaceDetector object.
             * Only one FaceDetector object is needed to perform all of your detection operation.
             */
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_detector_v3b.id3nn").readBytes(),
                FaceModel.FACE_DETECTOR_3B, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_encoder_v9b.id3nn").readBytes(),
                FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU
            )

            faceTracker = FaceTracker()

            // Set the face detector and encoder models

            // Set the face detector and encoder models
            faceTracker.detectionModel = FaceModel.FACE_DETECTOR_3B
            faceTracker.encodingModel = FaceModel.FACE_ENCODER_9B

            // The FaceTracker object has multiple parameters which must be tuned in order to respect
            // your application needs.

            // For example in this sample we raise the internal matching threshold of the tracker to ensure
            // the consistency of the tracked face IDs

            // The FaceTracker object has multiple parameters which must be tuned in order to respect
            // your application needs.

            // For example in this sample we raise the internal matching threshold of the tracker to ensure
            // the consistency of the tracked face IDs
            faceTracker.matchThreshold = 4000

            trackedFaceList = TrackedFaceList()

            Log.v(LOG_TAG, "Load: OK !")
        } catch (e: FaceException) {
            e.printStackTrace()
            Log.e(LOG_TAG, "Error while loading models: " + e.message)
        }
    }

    fun trackFaces(image: eu.id3.face.Image?): TrackedFaceList? {
        // Usage of the face tracker is similar to a detector usage
        // A TrackedFaceList object is re-used on each frame to hold information on the tracked faces
        faceTracker.trackFaces(image, trackedFaceList)
        return trackedFaceList
    }

    fun resetTrackedFaceList() {
        trackedFaceList!!.clear()
    }


    companion object {
        fun prepareImageForProcessing(image: Image): eu.id3.face.Image {
            /** Get the YUV planes of the image. */
            val planes: Array<Image.Plane> = image.planes
            val w = image.width
            val h = image.height
            val uvPixelStride: Int = planes[1].pixelStride
            val uvRowStride: Int = planes[1].rowStride
            val plane0 = planes[0].buffer.asReadOnlyBuffer()
            val plane0array = ByteArray(plane0.capacity())
            plane0.get(plane0array)
            val plane1: ByteBuffer = planes[1].buffer.asReadOnlyBuffer()
            val plane1array = ByteArray(plane1.capacity())
            plane1.get(plane1array)
            val plane2: ByteBuffer = planes[2].buffer.asReadOnlyBuffer()
            val plane2array = ByteArray(plane2.capacity())
            plane2.get(plane2array)

            /** Convert this image to an id3.face Image. */
            return eu.id3.face.Image.fromYuvPlanes(
                plane0array,
                plane1array,
                plane2array,
                w,
                h,
                uvPixelStride,
                uvRowStride,
                PixelFormat.BGR_24BITS
            )
        }
    }
}