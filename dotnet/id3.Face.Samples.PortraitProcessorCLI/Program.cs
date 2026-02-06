using System;

namespace id3.Face.Samples.PortraitProcessorCLI
{
    using id3.Face;

    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("-------------------------------------");
            Console.WriteLine("id3.Face.Samples.PortraitProcessorCLI");
            Console.WriteLine("-------------------------------------");

            // This sample shows how to use PortraitProcessor to compute face ICAO and landmarks values.
            try
            {
                /*
                 * Before calling any function of the SDK you must first check a valid license file.
                 * To get such a file please use the provided activation tool.
                 */
                FaceLicense.CheckLicense(@"..\..\..\..\id3Face.lic");
            }
            catch (FaceException ex)
            {
                Console.WriteLine("Error during license check: " + ex.Message);
                Environment.Exit(-1);
            }

            /*
             * The Face SDK heavily relies on deep learning technics and hence requires trained models to run.
             * Fill in the correct path to the downloaded models.
             */
            string modelPath = "../../../../models";

            /*
            * Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
            */
            Console.Write("Loading models... ");
            try
            {
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector4B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceEncoder10B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceLandmarksEstimator2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FacePoseEstimator1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceColorBasedPad4A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceAgeEstimator1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceExpressionClassifier1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceAttributesClassifier2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceOcclusionDetector2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.EyeGazeEstimator2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.EyeOpennessDetector1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.EyeRednessDetector1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceMaskClassifier2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceBackgroundUniformity1A, ProcessingUnit.Cpu);
            }
            catch (FaceException ex)
            {
                Console.WriteLine("Error while loading models" + ex.Message);
                Environment.Exit(-1);
            }
            Console.Write("Done.\n");

            /*
             * Load sample images from files.
             */
            try {
                Console.Write("Loading image from file... ");
                Image image = Image.FromFile(@"..\..\..\..\data\image1.jpg", PixelFormat.Bgr24Bits);
                Console.Write("Done.\n");

                // Resize to 512 because of detector limit.
                image.Resize(512, 0);

                Console.Write("Initializing portrait from image... ");
                PortraitProcessor processor = new PortraitProcessor();
                Portrait portrait = processor.CreatePortrait(image);
                Console.Write("Done.\n");

                /*
                 * Get age estimation.
                 */
                Console.Write("Estimating age... ");
                processor.EstimateAge(portrait);
                int age = portrait.Age;
                Console.Write($"\t\t\t{age} years\n");

                /*
                 * Get expression estimation.
                 */
                Console.Write("Estimating expression... ");
                processor.EstimateExpression(portrait);
                FaceExpression expression = portrait.Expression;
                Console.Write($"\t\t{expression.ToString()}\n");

                /*
                 * Get background uniformity.
                 */
                Console.Write("Estimating background uniformity... \n");
                processor.EstimateBackgroundUniformity(portrait);
                BackgroundUniformity bgUniformity = portrait.BackgroundUniformity;
                Console.Write($"\tColor uniformity:         \t{bgUniformity.ColorUniformity}\n");
                Console.Write($"\tStructure uniformity:     \t{bgUniformity.StructureUniformity}\n");

                /*
                 * Get ICAO geometric attributes.
                 */
                Console.Write("Computing ICAO geometric attributes... \n");
                processor.EstimateGeometryQuality(portrait);
                GeometricAttributes geomAttributes = portrait.GeometricAttributes;
                Console.Write($"\tHead image height ratio:  \t{geomAttributes.HeadImageHeightRatio}\n");
                Console.Write($"\tHead image width ratio:   \t{geomAttributes.HeadImageWidthRatio}\n");
                Console.Write($"\tHorizontal position:      \t{geomAttributes.HorizontalPosition}\n");
                Console.Write($"\tVertical position:        \t{geomAttributes.VerticalPosition}\n");
                Console.Write($"\tResolution:               \t{geomAttributes.Resolution}\n");

                /*
                 * Get face landmarks.
                 */
                PointList landmarks = portrait.Landmarks;

                /*
                 * Get face attributes.
                 */
                Console.Write("Detecting occlusions... ");
                processor.DetectOcclusions(portrait);
                Console.Write("Done.\n");

                Console.Write("Estimating face attributes... \n");
                processor.EstimateFaceAttributes(portrait);

                EyeGaze eyeGaze = portrait.EyeGaze;
                Console.Write($"\tLeft eye x gaze:          \t{eyeGaze.LeftEyeXGaze}°\n");
                Console.Write($"\tLeft eye y gaze:          \t{eyeGaze.LeftEyeYGaze}°\n");
                Console.Write($"\tRight eye x gaze:         \t{eyeGaze.RightEyeXGaze}°\n");
                Console.Write($"\tRight eye y gaze:         \t{eyeGaze.RightEyeYGaze}°\n");

                int leftEyeVisibilityScore = portrait.LeftEyeVisibility;
                int leftEyeOpeningScore = portrait.LeftEyeOpening;
                int rightEyeVisibilityScore = portrait.RightEyeVisibility;
                int rightEyeOpeningScore = portrait.RightEyeOpening;
                Console.Write($"\tLeft eye visibility score:    \t{leftEyeVisibilityScore}\n");
                Console.Write($"\tLeft eye opening score:       \t{leftEyeOpeningScore}\n");
                Console.Write($"\tRight eye visibility score:   \t{rightEyeVisibilityScore}\n");
                Console.Write($"\tRight eye opening score:      \t{rightEyeOpeningScore}\n");

                int genderMaleScore = portrait.GenderMale;
                int glassesScore = portrait.Glasses;
                int hatScore = portrait.Hat;
                int lookStraightScore = portrait.LookStraightScore;
                int makeupScore = portrait.Makeup;
                int mouthOpeningScore = portrait.MouthOpening;
                int mouthVisibilityScore = portrait.MouthVisibility;
                int noseVisibilityScore = portrait.NoseVisibility;
                int smileScore = portrait.Smile;
                int faceMaskScore = portrait.FaceMask;
                Console.Write($"\tGender male score:            \t{genderMaleScore}\n");
                Console.Write($"\tGlasses score:                \t{glassesScore}\n");
                Console.Write($"\tHat score:                    \t{hatScore}\n");
                Console.Write($"\tLook Straight score:          \t{lookStraightScore}\n");
                Console.Write($"\tMakeup score:                 \t{makeupScore}\n");
                Console.Write($"\tMouth opening score:          \t{mouthOpeningScore}\n");
                Console.Write($"\tMouth visibility score:       \t{mouthVisibilityScore}\n");
                Console.Write($"\tNose visibility score:        \t{noseVisibilityScore}\n");
                Console.Write($"\tSmile score:                  \t{smileScore}\n");
                Console.Write($"\tFace mask score:              \t{faceMaskScore}\n");

                /*
                 * get ICAO criterias statuses
                 * NOTE: must be called after all estimations
                 */
                processor.EstimatePhotographicQuality(portrait);
                PortraitQualityCheckpoints icaoCheckpoints = portrait.QualityCheckpoints;

                /*
                 * get global quality score
                 * NOTE: must be called after all estimations
                 */
                int qualityScore = portrait.QualityScore;
                Console.Write($"Global quality score:           \t{qualityScore}\n");

                Console.WriteLine("Sample terminated successfully.");
            }
            catch (FaceException ex)
            {
                Console.WriteLine("Error " + ex.Message);
            }
        }
    }
}
