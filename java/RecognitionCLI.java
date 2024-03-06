import eu.id3.face.*;

public class RecognitionCLI {

    public static void main(String[] args) {
        System.out.println("-------------------------------");
        System.out.println("id3 Face Samples RecognitionCLI");
        System.out.println("-------------------------------");

        // This basic sample shows how to encode two faces and compare them.

        // Before calling any function of the SDK you must first check a valid license file.
        // To get such a file please use the provided activation tool.
        FaceLicense.checkLicense("../id3Face.lic");

        /*
         * The Face SDK heavily relies on deep learning and hence requires trained models to run.
         * Fill in the correct path to the downloaded models.
         */
        String modelPath = "../models";
        /*
         * Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
         * For instance in this sample, we load a detector and an encoder.
         */
        System.out.println("Loading models... ");
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);
        FaceLibrary.loadModel(modelPath, FaceModel.FACE_ENCODER_9A, ProcessingUnit.CPU);
        System.out.println("Done.\n");
        /*
         * Load sample images from files.
         */
        System.out.println("Loading images from files... ");
        Image image1 = Image.fromFile("../data/image1.jpg", PixelFormat.BGR_24_BITS);
        Image image2 = Image.fromFile("../data/image2.jpg", PixelFormat.BGR_24_BITS);
        System.out.println("Done.\n");

        // Resize to 512 because of detector limit.
        image1.resize(512, 0);
        /*
         * Initialize an instance of face detector that will run on the CPU.
         * This instance has several parameters that can be set:
         * - ConfidenceThreshold: the detection score above which proposals will be considered as detected faces. Default value is 70. In the range [0:100].
         * - ThreadCount : allocating more than 1 thread here can increase the speed of the process.
         */
        FaceDetector faceDetector = new FaceDetector();
        faceDetector.setConfidenceThreshold(70);
        faceDetector.setModel(FaceModel.FACE_DETECTOR_4B);
        faceDetector.setThreadCount(4);
        /*
         * Detect faces in the images.
         */
        System.out.println("Detecting faces... ");
        DetectedFaceList detectedFaceList1 = faceDetector.detectFaces(image1);
        DetectedFaceList detectedFaceList2 = faceDetector.detectFaces(image2);
        System.out.println("Done.\n");
        /*
         * Initialize an instance of face encoder that will run on the CPU using the model 9A (the one previously loaded by FaceLibrary.LoadModel()).
         * This instance has several parameters that can be set:
         * - ThreadCount : allocating more than 1 thread here can increase the speed of the process.
         */
        FaceEncoder faceEncoder = new FaceEncoder();
        faceEncoder.setModel(FaceModel.FACE_ENCODER_9A);
        faceEncoder.setThreadCount(4);
        /*
         * Create the template from the largest detected faces in each image.
         */
        System.out.println("Creating templates... ");
        DetectedFace detectedFace1 = detectedFaceList1.getLargestFace();
        FaceTemplate faceTemplate1 = faceEncoder.createTemplate(image1, detectedFace1);
        detectedFace1.close();
        detectedFaceList1.close();
        image1.close();

        DetectedFace detectedFace2 = detectedFaceList2.getLargestFace();
        FaceTemplate faceTemplate2 = faceEncoder.createTemplate(image2, detectedFace2);
        detectedFace2.close();
        detectedFaceList2.close();
        image2.close();
        System.out.println("Done.\n");

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
        System.out.println("Comparing templates... ");
        int score = faceMatcher.compareTemplates(faceTemplate2, faceTemplate1);
        System.out.println("Done.\n");
        if (score > FaceMatcherThreshold.FMR10000.getValue())
        {
            System.out.println("Match: " + score);
        }
        else
        {
            System.out.println("No match: " + score);
        }

        /**
         * Face templates can be exported directly into a file or a buffer.
         * When using the SDK face matcher the id3FaceTemplateBufferType_Normal must be used.
         */
        System.out.println("Export template 1 as file...");
        faceTemplate1.toFile("../data/template1.bin");
        System.out.println("Export template 2 as buffer...");
        byte[] template2Buffer = faceTemplate2.toBuffer();

        /**
         * When the face match will be performed on a smartcard then it is most likely that
         * the templates must be sent to the smartcard either as BIT or BDT buffers.
         */

        /**
         * To enroll a template in a card, most implementations take a Biometric Information Template (BIT) buffer as input. 
         * This buffer can be prepared using the FaceTemplate.toBit() API.
         * Using this api require to set the desired matching threshold according to the chosen face encoder.
         * To do so please refer to the documentation of the smartcard. 
         * If you tell us which model of smartcard it is please know that id3 can also provide support on this subject.
         */
        int matchingThreshold = 744; // Using threshold for FalseMatchRate 1/10k for id3 FaceEncoder 9A (from id3 GC452 documentation)
        byte referenceDataQualifier = 1;
        byte[] referenceTemplateBIT = faceTemplate1.toBit(matchingThreshold, referenceDataQualifier);
        
        /**
         * To verify a template in a card, most implementations take a Biometric DataTemplate (BDT) buffer as input.
         * This buffer can be prepared using the FaceTemplate.toBdt() API.
         */
        byte[] verifyTemplateBDT = faceTemplate2.toBdt();

        /**
         * id3 Face SDK Java objects hold native memory and must manually released
         */
        faceMatcher.close();
        faceTemplate1.close();
        faceTemplate2.close();
        faceEncoder.close();
        faceDetector.close();

        /**
         * Unload models
         */
        FaceLibrary.unloadModel(FaceModel.FACE_ENCODER_9A, ProcessingUnit.CPU);
        FaceLibrary.unloadModel(FaceModel.FACE_DETECTOR_4B, ProcessingUnit.CPU);

        System.out.println("Sample terminated successfully.");
    }
}
