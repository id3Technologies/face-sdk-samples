using System;

namespace id3.Face.Samples.CompressToWebpCLI
{
    using id3.Face;

    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("----------------------------------");
            Console.WriteLine("id3.Face.Samples.CompressToWebpCLI");
            Console.WriteLine("----------------------------------");

            // This basic sample shows how to create a face thumbnail and compress it.
            try
            {
                // Before calling any function of the SDK you must first check a valid license file.
                // To get such a file please use the provided activation tool.
                FaceLibrary.CheckLicense(@"your_license_path_here");
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
            string modelPath = "..\\..\\..\\..\\..\\sdk\\models";
            /*
            * Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
            * For instance in this sample, we load a detector.
            */
            FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector3B, ProcessingUnit.Cpu);

            /*
             * Load sample image from file.
             */
            Image image = Image.FromFile("..\\..\\..\\..\\sample_data\\image1.jpg", PixelFormat.Bgr24Bits);
            /*
             * Initialize an instance of face detector that will run on the CPU.
             * This instance has several parameters that can be set:
             * - ConfidenceThreshold: the detection score above which proposals will be considered as detected faces. Default value is 70. In the range [0:100].
             * - ThreadCount : allocating more than 1 thread here can increase the speed of the process.
             */
            FaceDetector faceDetector = new FaceDetector()
            {
                ConfidenceThreshold = 70,
                Model = FaceModel.FaceDetector3B,
                ProcessingUnit = ProcessingUnit.Cpu,
                ThreadCount = 4
            };

            /*
             * Create a compressed thumbnail of the detected face image.
             * The parameters in this sample should obviously be tuned to fit your requirements.
             */

            // Size in pixels of the targeted thumbnail image height
            int targetImageHeight = 112;

            // Range of the size in bytes of the targeted thumbnail image buffer
            int minTargetImageSizeInBytes = 950;
            int maxTargetImageSizeInBytes = 1000;

            // Range of the authorized WEBP quality range
            int minQuality = 1;
            int maxQuality = 100;

            // Eye / Image width ratio in ]0;1[
            // The higher this parameter is the tightest is the crop
            float eyeWidthRatio = 0.45f;

            // Get the largest face from the image
            DetectedFaceList detectedFaceList = faceDetector.DetectFaces(image);
            DetectedFace faceToCompress = detectedFaceList.GetLargestFace();

            // Get the rectangle shape defining a crop around the face
            Rectangle bounds = faceToCompress.GetPortraitBounds(eyeWidthRatio, 0.45F, 1.33F);

            // Crop the original image and resize it to required size in pixels
            Image crop = image.ExtractRoi(bounds);

            // Resize to target size while keeping the same crop ratio
            int targetImageWidth = targetImageHeight * crop.Width / crop.Height;
            crop.Resize(targetImageWidth, targetImageHeight);

            // Iterative search to find the optimal webp quality for the required size range
            while (true)
            {
                int webpQuality = minQuality + (maxQuality - minQuality) / 2;
                Console.WriteLine("Compressing WEBP with quality : " + webpQuality);

                byte[] buffer = crop.ToBuffer(ImageFormat.Webp, webpQuality);
                Console.WriteLine("Resulting size is : " + buffer.Length + "bytes");

                if ((buffer.Length > maxTargetImageSizeInBytes) && (webpQuality != minQuality))
                {
                    maxQuality = webpQuality;
                }
                else if ((buffer.Length < minTargetImageSizeInBytes) && (webpQuality != maxQuality))
                {
                    minQuality = webpQuality;
                }
                else
                {
                    // Here we save to a file but you can also simply keep the buffer
                    crop.Save("compressed_" + webpQuality + "Q.webp", webpQuality);
                    break;
                }
            }

            Console.WriteLine("Sample terminated successfully.");
        }
    }
}
