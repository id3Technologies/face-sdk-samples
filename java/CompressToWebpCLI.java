import eu.id3.face.*;

public class CompressToWebpCLI {

    public static void main(String[] args) {
        System.out.println("----------------------------------");
        System.out.println("id3 Face Samples CompressToWebpCLI");
        System.out.println("----------------------------------");

        // This basic sample shows how to create a face thumbnail and compress it.

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
        System.out.println("Loading model... ");
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);
        System.out.println("Done.\n");
        /**
         * Load sample image from file.
         */
        System.out.println("Loading images from files... ");
        Image image = Image.fromFile("../data/image1.jpg", PixelFormat.BGR_24_BITS);
        System.out.println("Done.\n");

        // Resize to 512 because of detector limit.
        image.resize(512, 0);
        /**
         * Initialize an instance of face detector that will run on the CPU.
         * This instance has several parameters that can be set:
         * - ConfidenceThreshold: the detection score above which proposals will be
         * considered as detected faces. Default value is 70. In the range [0:100].
         * - ThreadCount : allocating more than 1 thread here can increase the speed of
         * the process.
         */
        FaceDetector faceDetector = new FaceDetector();
        faceDetector.setConfidenceThreshold(50);
        faceDetector.setModel(FaceModel.FACE_DETECTOR_4B);
        faceDetector.setThreadCount(4);
        /**
         * Create a compressed thumbnail of the detected face image.
         * The parameters in this sample should obviously be tuned to fit your
         * requirements.
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
        DetectedFaceList detectedFaceList = faceDetector.detectFaces(image);
        DetectedFace faceToCompress = detectedFaceList.getLargestFace();

        // Get the rectangle shape defining a crop around the face
        Rectangle bounds = faceToCompress.getPortraitBounds(eyeWidthRatio, 0.45F, 1.33F);

        // Crop the original image and resize it to required size in pixels
        Image crop = image.extractRoi(bounds);

        // Resize to target size while keeping the same crop ratio
        int targetImageWidth = targetImageHeight * crop.getWidth() / crop.getHeight();
        crop.resize(targetImageWidth, targetImageHeight);

        // Iterative search to find the optimal webp quality for the required size range
        while (true) {
            int webpQuality = minQuality + (maxQuality - minQuality) / 2;
            System.out.println("Compressing WEBP with quality : " + webpQuality);

            byte[] buffer = crop.toBuffer(ImageFormat.WEBP, webpQuality);
            System.out.println("Resulting size is : " + buffer.length + " bytes");

            if ((buffer.length > maxTargetImageSizeInBytes) && (webpQuality != minQuality)) {
                maxQuality = webpQuality;
            } else if ((buffer.length < minTargetImageSizeInBytes) && (webpQuality != maxQuality)) {
                minQuality = webpQuality;
            } else {
                // Here we save to a file but you can also simply keep the buffer
                crop.toFile("compressed_" + webpQuality + "Q.webp", webpQuality);
                break;
            }
        }

        /**
         * id3 Face SDK Java objects hold native memory and must manually released
         */
        faceDetector.close();
        /**
         * Unload models
         */
        FaceLibrary.unloadModel(FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);

        System.out.println("Sample terminated successfully.");
    }
}
