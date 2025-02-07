import eu.id3.face.*;

public class PortraitProcessorCLI {

    public static void main(String[] args) {
        System.out.println("-------------------------------------");
        System.out.println("id3 Face Samples PortraitProcessorCLI");
        System.out.println("-------------------------------------");

        // This sample shows how to use PortraitProcessor to compute face ICAO and
        // landmarks values.

        // Before calling any function of the SDK you must first check a valid license
        // file.
        // To get such a file please use the provided activation tool.
        FaceLicense.checkLicense("../id3Face.lic");

        /**
         * The Face SDK heavily relies on deep learning and hence requires trained
         * models to run.
         * Fill in the correct path to the downloaded models.
         */
        String modelPath = "../models";
        /**
         * Once a model is loaded in the desired processing unit (CPU or GPU) several
         * instances of the associated processor can be created.
         * For instance in this sample, we load a detector and an encoder.
         */
        System.out.println("Loading models... ");
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_COLOR_BASED_PAD_3A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_ENCODING_QUALITY_ESTIMATOR_3A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_AGE_ESTIMATOR_1A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_EXPRESSION_CLASSIFIER_1A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_OCCLUSION_DETECTOR_2A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.EYE_GAZE_ESTIMATOR_2A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.EYE_OPENNESS_DETECTOR_1A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.EYE_REDNESS_DETECTOR_1A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_MASK_CLASSIFIER_2A, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_BACKGROUND_UNIFORMITY_1A, ProcessingUnit.CPU);
        System.out.println("Done.\n");
        /**
         * Load sample image from file.
         */
        System.out.println("Loading image from file... ");
        Image image = Image.fromFile("../data/image1.jpg", PixelFormat.BGR_24_BITS);
        System.out.println("Done.\n");

        // Resize to 512 because of detector limit.
        image.resize(512, 0);

        System.out.println("Initializing portrait from image... ");
        PortraitProcessor processor = new PortraitProcessor();
        Portrait portrait = processor.createPortrait(image);
        System.out.println("Done.\n");

        /**
         * Get age estimation.
         */
        System.out.print("Estimating age... ");
        processor.estimateAge(portrait);
        int age = portrait.getAge();
        System.out.println("\t\t\t" + age + " years");
        /**
         * Get expression estimation.
         */
        System.out.print("Estimating expression... ");
        processor.estimateExpression(portrait);
        FaceExpression expression = portrait.getExpression();
        System.out.println("\t\t" + expression.toString());
        /**
         * Get background uniformity.
         */
        System.out.println("Estimating background uniformity... ");
        processor.estimateBackgroundUniformity(portrait);
        BackgroundUniformity bgUniformity = portrait.getBackgroundUniformity();
        System.out.println("\tColor uniformity:         \t" + bgUniformity.colorUniformity);
        System.out.println("\tStructure uniformity:     \t" + bgUniformity.structureUniformity);
        /**
         * Get ICAO geometric attributes.
         */
        System.out.println("Computing ICAO geometric attributes... ");
        processor.estimateGeometryQuality(portrait);
        GeometricAttributes geomAttributes = portrait.getGeometricAttributes();
        System.out.println("\tHead image height ratio:  \t" + geomAttributes.headImageHeightRatio);
        System.out.println("\tHead image width ratio:   \t" + geomAttributes.headImageWidthRatio);
        System.out.println("\tHorizontal position:      \t" + geomAttributes.horizontalPosition);
        System.out.println("\tVertical position:        \t" + geomAttributes.verticalPosition);
        System.out.println("\tResolution:               \t" + geomAttributes.resolution);
        /**
         * Get face landmarks.
         */
        PointList landmarks = portrait.getLandmarks();

        /**
         * Get face attributes.
         */
        System.out.println("Detecting occlusions... ");
        processor.detectOcclusions(portrait);
        System.out.println("Done.");

        System.out.println("Estimating face attributes... ");
        processor.estimateFaceAttributes(portrait);

        EyeGaze eyeGaze = portrait.getEyeGaze();
        System.out.println("\tLeft eye x gaze:          \t" + eyeGaze.leftEyeXGaze + "°");
        System.out.println("\tLeft eye y gaze:          \t" + eyeGaze.leftEyeYGaze + "°");
        System.out.println("\tRight eye x gaze:         \t" + eyeGaze.rightEyeXGaze + "°");
        System.out.println("\tRight eye y gaze:         \t" + eyeGaze.rightEyeYGaze + "°");

        int leftEyeVisibilityScore = portrait.getLeftEyeVisibility();
        int leftEyeOpeningScore = portrait.getLeftEyeOpening();
        int rightEyeVisibilityScore = portrait.getRightEyeVisibility();
        int rightEyeOpeningScore = portrait.getRightEyeOpening();
        System.out.println("\tLeft eye visibility score:    \t" + leftEyeVisibilityScore);
        System.out.println("\tLeft eye opening score:       \t" + leftEyeVisibilityScore);
        System.out.println("\tRight eye visibility score:   \t" + rightEyeVisibilityScore);
        System.out.println("\tRight eye opening score:      \t" + rightEyeVisibilityScore);

        int genderMaleScore = portrait.getGenderMale();
        int glassesScore = portrait.getGlasses();
        int hatScore = portrait.getHat();
        int lookStraightScore = portrait.getLookStraightScore();
        int makeupScore = portrait.getMakeup();
        int mouthOpeningScore = portrait.getMouthOpening();
        int mouthVisibilityScore = portrait.getMouthVisibility();
        int noseVisibilityScore = portrait.getNoseVisibility();
        int smileScore = portrait.getSmile();
        int faceMaskScore = portrait.getFaceMask();
        System.out.println("\tGender male score:            \t" + genderMaleScore);
        System.out.println("\tGlasses score:                \t" + glassesScore);
        System.out.println("\tHat score:                    \t" + hatScore);
        System.out.println("\tLook Straight score:          \t" + lookStraightScore);
        System.out.println("\tMakeup score:                 \t" + makeupScore);
        System.out.println("\tMouth opening score:          \t" + mouthOpeningScore);
        System.out.println("\tMouth visibility score:       \t" + mouthVisibilityScore);
        System.out.println("\tNose visibility score:        \t" + noseVisibilityScore);
        System.out.println("\tSmile score:                  \t" + smileScore);
        System.out.println("\tFace mask score:              \t" + faceMaskScore);

        /**
         * get ICAO criterias statuses.
         * NOTE: must be called after all estimations
         */
        processor.estimatePhotographicQuality(portrait);
        PortraitQualityCheckpoints icaoCheckpoints = portrait.getQualityCheckpoints();
        /*
         * get global quality score
         * NOTE: must be called after all estimations
         */
        int qualityScore = portrait.getQualityScore();
        System.out.println("Global quality score:           \t" + qualityScore);

        /**
         * Unload models
         */
        FaceLibrary.unloadModel(FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_ENCODER_9B, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_POSE_ESTIMATOR_1A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_COLOR_BASED_PAD_3A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_ENCODING_QUALITY_ESTIMATOR_3A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_AGE_ESTIMATOR_1A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_EXPRESSION_CLASSIFIER_1A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_OCCLUSION_DETECTOR_2A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.EYE_GAZE_ESTIMATOR_2A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.EYE_OPENNESS_DETECTOR_1A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.EYE_REDNESS_DETECTOR_1A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_MASK_CLASSIFIER_2A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_BACKGROUND_UNIFORMITY_1A, ProcessingUnit.CPU);

        System.out.println("Sample terminated successfully.");
    }
}
