package eu.id3.face.samples.pad

import android.content.Context
import android.media.Image
import android.util.Log
import eu.id3.face.DetectedFace
import eu.id3.face.FaceDetector
import eu.id3.face.FaceException
import eu.id3.face.FaceLibrary
import eu.id3.face.FaceModel
import eu.id3.face.ImageFormat
import eu.id3.face.PadStatus
import eu.id3.face.PixelFormat
import eu.id3.face.Portrait
import eu.id3.face.PortraitInstruction
import eu.id3.face.PortraitProcessor
import eu.id3.face.ProcessingUnit
import java.nio.ByteBuffer


private const val LOG_TAG = "FaceProcessor"

class FaceProcessor(context: Context) {

    private lateinit var faceDetector: FaceDetector

    private lateinit var processor: PortraitProcessor
    private lateinit var portrait: Portrait
    private var portraitCreated = false

    init {
        try {
            /**
             * Load a face detector.
             * First load the model from the Assets and then initialize the FaceDetector object.
             * Only one FaceDetector object is needed to perform all of your detection operation.
             */
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_detector_v4b.id3nn").readBytes(),
                FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU
            )
            faceDetector = FaceDetector()
            faceDetector.confidenceThreshold = Parameters.detectorConfidenceThreshold
            faceDetector.model = FaceModel.FACE_DETECTOR_4B
            faceDetector.threadCount = Parameters.detectorThreadCount

            /**
             * Load portrait processor.
             * First load the models from the Assets and then initialize the PortraitProcessor object.
             */
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_encoder_v10b.id3nn").readBytes(),
                FaceModel.FACE_ENCODER_10B, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_landmarks_estimator_v2a.id3nn").readBytes(),
                FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_pose_estimator_v1a.id3nn").readBytes(),
                FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_color_pad_v3a.id3nn").readBytes(),
                FaceModel.FACE_COLOR_BASED_PAD_4A, ProcessingUnit.CPU
            )
            processor = PortraitProcessor()
            portraitCreated = false

            Log.v(LOG_TAG, "Load models: OK !")
        } catch (e: FaceException) {
            e.printStackTrace()
            Log.e(LOG_TAG, "Error while loading models: " + e.message)
        }
    }

    fun detectLargestFace(image: eu.id3.face.Image): DetectedFace? {
        /** Track faces in the image. */
        val detectedFaceList = faceDetector.detectFaces(image)
        return if (detectedFaceList.count > 0) {
            /** At least one face was detected! Return the largest one. */
            detectedFaceList.largestFace
        } else {
            /** No face was detected. */
            null
        }
    }

    fun resetPortrait() {
        portraitCreated = false
    }

    fun analyzeLargestFace(
        image: eu.id3.face.Image,
        detectedFace: DetectedFace
    ): AnalyzeLargestFaceResult {
        try {
            /** Initialize or update portrait. */
            if (!portraitCreated) {
                portrait = Portrait()
                portraitCreated = true
            }

            /** Detects attack support with portrait processor. */
            processor.updatePortrait(portrait, image)
            processor.estimatePhotographicQuality(portrait)
            processor.detectPresentationAttack(portrait)

            /** Extracts the portrait image of the detected face to display it. */
            val portraitBounds = detectedFace.getPortraitBounds(0.25f, 0.45f, 1.33f)
            val portraitImage = image.extractRoi(portraitBounds)
            portraitImage.flip(true, false)

            /** Compress the portrait image buffer as a JPEG buffer. */
            val jpegPortraitImageBuffer = portraitImage.toBuffer(ImageFormat.JPEG, 75.0f)

            return AnalyzeLargestFaceResult(
                jpegPortraitImageBuffer,
                portrait.instruction,
                portrait.padStatus,
                portrait.padScore,
                0
            )
        } catch (e: FaceException) {
            return AnalyzeLargestFaceResult(
                ByteArray(0),
                PortraitInstruction.NONE,
                PadStatus.UNKNOWN,
                0,
                e.errorCode
            )
        }
    }

    class AnalyzeLargestFaceResult(
        private var jpegPortraitImageBuffer: ByteArray,
        private var instruction: PortraitInstruction,
        private var status: PadStatus,
        private var score: Int,
        private var errorCode: Int
    ) {
        fun getJpegPortraitImageBuffer(): ByteArray {
            return jpegPortraitImageBuffer
        }

        fun getInstruction(): PortraitInstruction {
            return instruction
        }

        fun getStatus(): PadStatus {
            return status
        }

        fun getScore(): Int {
            return score
        }

        fun getErrorCode(): Int {
            return errorCode
        }
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
                PixelFormat.BGR_24_BITS
            )
        }
    }
}