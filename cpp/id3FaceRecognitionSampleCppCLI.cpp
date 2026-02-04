/**
 * This sample shows how to perform basic face recognition tasks using id3 Face SDK.
 */

#include <iostream>
#include "id3FaceLib.h"

using namespace id3FaceCppWrapper;
int main(int argc, char **argv)
{
	try
	{
		std::string data_dir = "../../../data/";
		/**
         * Fill in the correct path to the license.
         */
		std::string license_path = "../../../id3Face.lic";
		/**
         * Fill in the correct path to the downloaded models.
         */
		std::string models_dir = "../../../models/";
		/**
         * All functions of the API return an error code.
         */
		int err = id3FaceError_Success;
		/**
         * The id3 Face SDK needs a valid license to work.
         * It is required to provide a path to this license file.
         * It is required to call the id3FaceLicense_CheckLicense() function before calling any other function of the SDK.
         */
		std::cout << "Checking license" << std::endl;
		FaceLicense::checkLicense(license_path.c_str());
		/**
         * Load two pictures from files.
         */
		std::string image1_path = data_dir + "image1.jpg";
		std::cout << "Loading reference image: " << image1_path.c_str() << std::endl;
		Image reference_image;
		reference_image.fromFile(image1_path.c_str(), id3FacePixelFormat_Bgr24Bits);
		// Resize to 512 because of detector limit.
		reference_image.resize(512, 0);
		//
		std::string image2_path = data_dir + "image2.jpg";
		std::cout << "Loading probe image: " << image2_path.c_str() << std::endl;
		Image probe_image;
		probe_image.fromFile(image2_path.c_str(), id3FacePixelFormat_Bgr24Bits);

		/**
         * To use the face detector it is required to first load the model files into the RAM of the desired processing unit.
         * It only has to be called once and then multiple instances of ID3_FACE_DETECTOR can be created.
         */
		std::cout << "Loading face detector 3B model" << std::endl;
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
		/**
         * Once the model is loaded, it is now possible to instantiate an ID3_FACE_DETECTOR object.
         */
		FaceDetector detector;
		/**
         * Once the instance is initialized, it is now possible to set its parameters.
         * - ConfidenceThreshold: default value is 50, a smaller value will increase the number of detections (true ones and
         * false ones), a greater value will decrease it.
         * - Model: default value is FaceDetector4A which is the best accuracy one, for greater speed use FaceDetector4B - see documentation
         * to get the exact performance trade-offs.
         * - ThreadCount: allocating more than 1 thread here can increase the speed of the process.
         */
		detector.setConfidenceThreshold(50);
		detector.setModel(id3FaceModel_FaceDetector4B);
		detector.setThreadCount(4);
		/**
         * At this point, the detector instance is ready.
         */
		/**
         * To use the face encoder it is required to first load the model files in the memory of the desired processing unit.
         * It only has to be called once and then multiple instances of ID3_FACE_ENCODER can be created.
         * It is possible to choose which encoder you want to load:
         * - Processing unit: choose if you want to use either the CPU or GPU to extract templates.
         */
		std::cout << "Loading face encoder 9A model" << std::endl;
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceEncoder9A, id3FaceProcessingUnit_Cpu);
		/**
         * Once the model is loaded, it is now possible to instantiate an ID3_FACE_ENCODER object.
         * Note: parameters must match the ones used in the id3FaceLibrary_LoadModel() function.
         */
		FaceEncoder encoder;
		/**
         * Once the instance is initialized, it is now possible to set its parameters.
         * - Model: default value is 9A which is the best accuracy one, for greater speed use 9B (or 8A/B for legacy) - see documentation
         * to get the exact performance trade-offs. Beware that the models are NOT interoperable (generated templates cannot be cross-matched).
         * - ThreadCount: allocating more than 1 thread here can increase the speed of the process.
         */
		encoder.setModel(id3FaceModel_FaceEncoder9A);
		encoder.setThreadCount(4);
		/*
         * At this point, the encoder instance is ready.
         */
		/**
         * Detect faces in the images.
         */
		std::cout << "Detecting faces" << std::endl;
		auto reference_list = detector.detectFaces(reference_image);
		auto probe_list     = detector.detectFaces(probe_image);

		/**
         * Get detected faces from the lists.
         */
		int reference_count = reference_list.getCount();
		if (reference_count != 1) {
			std::cout << "Reference image does not contain 1 face" << std::endl;
			exit(1);
		}
		auto reference_item = reference_list.get(0);
		int probe_count = reference_list.getCount();
		if (reference_count != 1) {
			std::cout << "Probe image does not contain 1 face" << std::endl;
			exit(1);
		}

		auto probe_item = probe_list.get(0);
		/*
         * A face template object contains the facial unique features that are used to recognize a user.
         */
		/*
         * Create the templates from the detected items.
         */
		std::cout << "Creating reference template" << std::endl;
		auto reference_template = encoder.createTemplate(reference_image, reference_item);

		std::cout << "Creating probe template" << std::endl;
		auto probe_template = encoder.createTemplate(probe_image, probe_item);

		/**
         * Initialize a face matcher.
         */
		FaceMatcher matcher;
		/**
         * The matching process returns a score between 0 and 65535. To take a decision this score
         * must be compared to a defined threshold.
         * For 1 to 1 authentication, it is recommended to select a threshold of at least 4000 (FMR=1:10K).
         * Please see documentation to get more information on how to choose the threshold.
         */
		std::cout << std::endl
				  << "Comparing detected faces" << std::endl;
		int score = matcher.compareTemplates(probe_template, reference_template);
		std::cout << "   Score: " << score << std::endl;
		if (score > id3FaceMatcherThreshold_Fmr10000) {
			std::cout << "   Result: MATCH " << std::endl;
		}
		else {
			std::cout << "   Result: NO MATCH " << std::endl;
		}

		/**
         * Face templates can be exported directly into a file.
         * When using the SDK face matcher the id3FaceTemplateBufferType_Normal must be used.
         */
		std::cout << "Export reference template as file" << std::endl;
		std::string reference_template_path = data_dir + "reference_template.bin";
		reference_template.toFile(reference_template_path.c_str());
		/**
         * Face templates can also directly be exported into a buffer.
         * Please notice how two calls are performed:
         * - the first one allow to receive the required buffer size
         * - the second one fill the allocated buffer with the template data
         * For a given encoder the size of a template buffer will always stay the same.
         */
		std::cout << "Export probe template as buffer" << std::endl;
		auto probe_template_buffer = probe_template.toBuffer();

		// std::cout << std::endl
		//	 << "Press any key..." << std::endl;
		// std::cin.get();
		FaceLibrary::unloadModel(id3FaceModel_FaceEncoder9A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);

		std::cout << "Sample terminated successfully." << std::endl;
	}
	catch (FaceException &ex) {
		std::cout << ex.what() << std::endl;
	}
}
