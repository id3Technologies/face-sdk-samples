package eu.id3.face.samples.analysis

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.util.Log
import eu.id3.face.*
import java.nio.ByteBuffer

private const val LOG_TAG = "FaceProcessor"

class FaceProcessor(context: Context) {

    private lateinit var faceDetector: FaceDetector
    private lateinit var faceAnalyser: FaceAnalyser

    init {
        try {
            /**
             * Load a face detector.
             * First load the model from the Assets and then initialize the FaceDetector object.
             * Only one FaceDetector object is needed to perform all of your detection operation.
             */
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_detector_v3b.id3nn").readBytes(),
                FaceModel.FACE_DETECTOR_3B, ProcessingUnit.CPU
            )
            faceDetector = FaceDetector()
            faceDetector.confidenceThreshold = Parameters.detectorConfidenceThreshold
            faceDetector.model = FaceModel.FACE_DETECTOR_3B
            faceDetector.threadCount = Parameters.detectorThreadCount

            /**
             * Load a face presentation attack detector (PAD).
             * First load the models from the Assets and then initialize the FaceAnalysis object.
             */
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_pose_estimator_v1a.id3nn").readBytes(),
                FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_occlusion_detector_v2a.id3nn").readBytes(),
                FaceModel.FACE_OCCLUSION_DETECTOR_2A, ProcessingUnit.CPU
            )
            FaceLibrary.loadModelBuffer(
                context.assets.open("models/face_attributes_classifier_v2a.id3nn").readBytes(),
                FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, ProcessingUnit.CPU
            )
            faceAnalyser = FaceAnalyser()

            Log.v(LOG_TAG, "Load models: OK !")
        } catch (e: FaceException) {
            e.printStackTrace()
            Log.e(LOG_TAG, "Error while loading models: " + e.message)
        }
    }

    fun detectLargestFace(image: eu.id3.face.Image): DetectedFace? {
        /** Detect faces in the image. */
        val detectedFaceList = faceDetector.detectFaces(image)
        return if (detectedFaceList.count > 0) {
            /** At least one face was detected! Return the largest one. */
            detectedFaceList.largestFace
        } else {
            /** No face was detected. */
            null
        }
    }

    class AnalyzeLargestFaceResult(
        private var jpegPortraitImageBuffer: ByteArray,
        private var facePose: FacePose,
        private var faceOcclusionScores: FaceOcclusionScores,
        private var faceAttributes: FaceAttributes,
        private var errorCode: Int
    ) {
        fun getJpegPortraitImageBuffer(): ByteArray {
            return jpegPortraitImageBuffer
        }

        fun getFacePose(): FacePose {
            return facePose
        }

        fun getFaceOcclusionScores(): FaceOcclusionScores {
            return faceOcclusionScores
        }

        fun getFaceAttributes(): FaceAttributes {
            return faceAttributes
        }

        fun getErrorCode(): Int {
            return errorCode
        }
    }

    fun analyzeLargestFace(
        image: eu.id3.face.Image,
        detectedFace: DetectedFace
    ): AnalyzeLargestFaceResult {
        try {
            val facePose = faceAnalyser.computePose(detectedFace)

            val faceOcclusionScores = faceAnalyser.detectOcclusions(image, detectedFace)

            val faceAttributes = faceAnalyser.computeAttributes(image, detectedFace)

            /** Extracts the portrait image of the detected face to display it. */
            val portraitBounds = detectedFace.getPortraitBounds(0.25f, 0.45f, 1.33f)
            val portraitImage = image.extractRoi(portraitBounds)
            if (Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
                portraitImage.flip(true, false)
            }

            /** Compress the portrait image buffer as a JPEG buffer. */
            val jpegPortraitImageBuffer = portraitImage.toBuffer(ImageFormat.JPEG, 75.0f)

            return AnalyzeLargestFaceResult(
                jpegPortraitImageBuffer,
                facePose,
                faceOcclusionScores,
                faceAttributes,
                0
            )
        }
        catch (e: FaceException) {
            return AnalyzeLargestFaceResult(ByteArray(0), FacePose(), FaceOcclusionScores(), FaceAttributes(), e.errorCode)
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