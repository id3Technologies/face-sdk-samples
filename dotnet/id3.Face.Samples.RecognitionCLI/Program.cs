using System;

namespace id3.Face.Samples.RecognitionCLI
{
    using id3.Face;

    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("-------------------------------");
            Console.WriteLine("id3.Face.Samples.RecognitionCLI");
            Console.WriteLine("-------------------------------");

            // This basic sample shows how to encode two faces and compare them.
            try
            {
                // Before calling any function of the SDK you must first check a valid license file.
                // To get such a file please use the provided activation tool.
                string licensePath = Environment.GetEnvironmentVariable("ID3_LICENSE_PATH");
                FaceLicense.CheckLicense(licensePath);
            }
            catch (FaceException ex)
            {
                Console.WriteLine("Error during license check" + ex.Message);
                Environment.Exit(-1);
            }

            /*
             * The Face SDK heavily relies on deep learning technics and hence requires trained models to run.
             * Fill in the correct path to the downloaded models.
             */
            string modelPath = "../../../../../sdk/models";
            /*
            * Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
            * For instance in this sample, we load a detector and an encoder.
            */
            Console.Write("Loading models... ");
            FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector4B, ProcessingUnit.Cpu);
            FaceLibrary.LoadModel(modelPath, FaceModel.FaceEncoder9A, ProcessingUnit.Cpu);
            Console.Write("Done.\n");
            /*
             * Load sample images from files.
             */
            Console.Write("Loading images from files... ");
            Image image1 = Image.FromFile("../../../../../data/image1.jpg", PixelFormat.Bgr24Bits);
            Image image2 = Image.FromFile("../../../../../data/image2.jpg", PixelFormat.Bgr24Bits);
            Console.Write("Done.\n");
            /*
             * Initialize an instance of face detector that will run on the CPU.
             * This instance has several parameters that can be set:
             * - ConfidenceThreshold: the detection score above which proposals will be considered as detected faces. Default value is 70. In the range [0:100].
             * - ThreadCount : allocating more than 1 thread here can increase the speed of the process.
             */
            FaceDetector faceDetector = new FaceDetector()
            {
                ConfidenceThreshold = 50,
                Model = FaceModel.FaceDetector4B,
                ThreadCount = 4
            };
            /*
             * Detect faces in the images.
             */
            Console.Write("Detecting faces... ");
            DetectedFaceList detectedFaceList1 = faceDetector.DetectFaces(image1);
            DetectedFaceList detectedFaceList2 = faceDetector.DetectFaces(image2);
            Console.Write("Done.\n");
            /*
             * Initialize an instance of face encoder that will run on the CPU using the model 9A (the one previously loaded by FaceLibrary.LoadModel()).
             * This instance has several parameters that can be set:
             * - ThreadCount : allocating more than 1 thread here can increase the speed of the process.
             */
            FaceEncoder faceEncoder = new FaceEncoder()
            {
                Model = FaceModel.FaceEncoder9A,
                ThreadCount = 4
            };
            /*
             * Create the template from the largest detected faces in each image.
             */
            Console.Write("Creating templates... ");
            FaceTemplate faceTemplate1 = faceEncoder.CreateTemplate(image1, detectedFaceList1.GetLargestFace());
            FaceTemplate faceTemplate2 = faceEncoder.CreateTemplate(image2, detectedFaceList2.GetLargestFace());
            Console.Write("Done.\n");
            /*
             * Initialize a face matcher instance.
             */
            FaceMatcher faceMatcher = new FaceMatcher();
            /*
             * Compare the two templates.
             * The matching process returns a score between 0 and 65535. To take a decision this score must be compared to a defined threshold.
             * It is recommended to select a threshold associated to at least an FMR of 1:10000.
             * Please see documentation to get more information on how to choose the threshold.
            */
            Console.Write("Comparing templates... ");
            int score = faceMatcher.CompareTemplates(faceTemplate2, faceTemplate1);
            Console.Write("Done.\n");
            if (score > (int)FaceMatcherThreshold.Fmr10000)
            {
                Console.WriteLine("Match: " + score);
            }
            else
            {
                Console.WriteLine("No match: " + score);
            }

            /**
	         * Face templates can be exported directly into a file or a buffer.
	         * When using the SDK face matcher the id3FaceTemplateBufferType_Normal must be used.
	        */
            Console.Write("Export template 1 as file...");
            faceTemplate1.ToFile("../../../../../data/template1.bin");
            Console.Write("Export template 2 as buffer...");
            byte[] template2Buffer = faceTemplate2.ToBuffer();

            Console.WriteLine("Sample terminated successfully.");
            Console.ReadKey();
        }
    }
}
