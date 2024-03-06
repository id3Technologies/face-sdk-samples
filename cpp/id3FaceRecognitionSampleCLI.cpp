/**
 * This sample shows how to perform basic face recognition tasks using id3 Face SDK.
 */

#include <iostream>
#include "id3FaceLib.h"

void check(int err, const std::string& func_name)
{
	if (err != id3FaceError_Success)
	{
		std::cout << "Error " << err << " in " << func_name.c_str() << std::endl;
		exit(1);
	}
}

int main(int argc, char **argv)
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
	err = id3FaceLicense_CheckLicense(license_path.c_str(), nullptr);
	check(err, "id3FaceLicense_CheckLicense");
	/**
   	 * Load two pictures from files.
   	 */
	std::string image1_path = data_dir + "image1.jpg";
	std::cout << "Loading reference image: " << image1_path.c_str() << std::endl;
	ID3_FACE_IMAGE reference_image;
	err = id3FaceImage_Initialize(&reference_image);
	err = id3FaceImage_FromFile(reference_image, image1_path.c_str(), id3FacePixelFormat_Bgr24Bits);
	check(err, "id3FaceImage_FromFile");
    // Resize to 512 because of detector limit.
    id3FaceImage_Resize(reference_image, 512, 0);
    //
	std::string image2_path = data_dir + "image2.jpg";
	std::cout << "Loading probe image: " << image2_path.c_str() << std::endl;
	ID3_FACE_IMAGE probe_image;
	err = id3FaceImage_Initialize(&probe_image);
	err = id3FaceImage_FromFile(probe_image, image2_path.c_str(), id3FacePixelFormat_Bgr24Bits);
	check(err, "id3FaceImage_FromFile");

	ID3_FACE_DETECTOR detector;
	/**
	 * To use the face detector it is required to first load the model files into the RAM of the desired processing unit.
	 * It only has to be called once and then multiple instances of ID3_FACE_DETECTOR can be created.
     */
	std::cout << "Loading face detector 3B model" << std::endl;
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel");
	/**
   	 * Once the model is loaded, it is now possible to instantiate an ID3_FACE_DETECTOR object.
   	 */
	err = id3FaceDetector_Initialize(&detector);
	check(err, "id3FaceDetector_Initialize");
	/**
   	 * Once the instance is initialized, it is now possible to set its parameters.
   	 * - ConfidenceThreshold: default value is 50, a smaller value will increase the number of detections (true ones and
   	 * false ones), a greater value will decrease it.
	 * - Model: default value is FaceDetector4A which is the best accuracy one, for greater speed use FaceDetector4B - see documentation 
	 * to get the exact performance trade-offs.
   	 * - ThreadCount: allocating more than 1 thread here can increase the speed of the process.
   	 */
	err = id3FaceDetector_SetConfidenceThreshold(detector, 50);
	check(err, "id3FaceDetector_SetConfidenceThreshold");
	err = id3FaceDetector_SetModel(detector, id3FaceModel_FaceDetector4B);
	check(err, "id3FaceDetector_SetModel");
	err = id3FaceDetector_SetThreadCount(detector, 4);
	check(err, "id3FaceDetector_SetThreadCount");
	/**
   	 * At this point, the detector instance is ready.
   	 */
	ID3_FACE_ENCODER encoder;
	/**
     * To use the face encoder it is required to first load the model files in the memory of the desired processing unit.
     * It only has to be called once and then multiple instances of ID3_FACE_ENCODER can be created.
     * It is possible to choose which encoder you want to load:
     * - Processing unit: choose if you want to use either the CPU or GPU to extract templates.
     */
	std::cout << "Loading face encoder 9A model" << std::endl;
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceEncoder9A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel");
	/**
   	 * Once the model is loaded, it is now possible to instantiate an ID3_FACE_ENCODER object.
   	 * Note: parameters must match the ones used in the id3FaceLibrary_LoadModel() function.
   	 */
	err = id3FaceEncoder_Initialize(&encoder);
	check(err, "id3FaceEncoder_Initialize");
	/**
   	 * Once the instance is initialized, it is now possible to set its parameters.
     * - Model: default value is 9A which is the best accuracy one, for greater speed use 9B (or 8A/B for legacy) - see documentation 
	 * to get the exact performance trade-offs. Beware that the models are NOT interoperable (generated templates cannot be cross-matched).
   	 * - ThreadCount: allocating more than 1 thread here can increase the speed of the process.
   	 */
	err = id3FaceEncoder_SetModel(encoder, id3FaceModel_FaceEncoder9A);
	check(err, "id3FaceDetector_SetModel");
	err = id3FaceEncoder_SetThreadCount(encoder, 4);
	check(err, "id3FaceEncoder_SetThreadCount");
	/*
   	 * At this point, the encoder instance is ready.
   	 */
	ID3_DETECTED_FACE_LIST reference_list;
	ID3_DETECTED_FACE_LIST probe_list;
	/**
   	 * A DetectedFaceList is a list of detected detected faces.
   	 * It needs to be initialized before to be filled by the function id3FaceDetector_DetectFaces().
   	 */
	err = id3DetectedFaceList_Initialize(&reference_list);
	check(err, "id3DetectedFaceList_Initialize");
	err = id3DetectedFaceList_Initialize(&probe_list);
	check(err, "id3DetectedFaceList_Initialize");
    /**
   	 * Detect faces in the images.
   	 */
	std::cout << "Detecting faces" << std::endl;
	err = id3FaceDetector_DetectFaces(detector, reference_image, reference_list);
	check(err, "id3FaceDetector_DetectFaces");
	err = id3FaceDetector_DetectFaces(detector, probe_image, probe_list);
	check(err, "id3FaceDetector_DetectFaces");

	ID3_DETECTED_FACE reference_item;
	ID3_DETECTED_FACE probe_item;
	/**
   	 * A detected face object contains the detection information of a face.
   	 * It needs to be initialized before to be got from a list of items.
   	 */
	err = id3DetectedFace_Initialize(&reference_item);
	check(err, "id3DetectedFace_Initialize");
	err = id3DetectedFace_Initialize(&probe_item);
	check(err, "id3DetectedFace_Initialize");
	/**
   	 * Get detected faces from the lists.
   	 */
	int reference_count = 0;
	err = id3DetectedFaceList_GetCount(reference_list, &reference_count);
	check(err, "id3DetectedFaceList_GetCount");
	if (reference_count != 1)
	{
		std::cout << "Reference image does not contain 1 face" << std::endl;
		exit(1);
	}

	err = id3DetectedFaceList_Get(reference_list, 0, reference_item);
	check(err, "id3DetectedFaceList_Get");

	int probe_count = 0;
	err = id3DetectedFaceList_GetCount(reference_list, &probe_count);
	check(err, "id3DetectedFaceList_GetCount");
	if (reference_count != 1)
	{
		std::cout << "Probe image does not contain 1 face" << std::endl;
		exit(1);
	}

	err = id3DetectedFaceList_Get(probe_list, 0, probe_item);
	check(err, "id3DetectedFaceList_Get");

	ID3_FACE_TEMPLATE reference_template;
	ID3_FACE_TEMPLATE probe_template;
	/*
   	 * A face template object contains the facial unique features that are used to recognize a user.
   	 * It needs to be initialized before to be filled by the function id3FaceEncoder_CreateTemplate().
   	 */
	err = id3FaceTemplate_Initialize(&reference_template);
	check(err, "id3DetectedFaceList_Initialize");
	err = id3FaceTemplate_Initialize(&probe_template);
	check(err, "id3DetectedFaceList_Initialize");
	/*
   	 * Create the templates from the detected items.
   	 */
	std::cout << "Creating reference template" << std::endl;
	err = id3FaceEncoder_CreateTemplate(encoder, reference_image, reference_item, reference_template);
	check(err, "id3FaceEncoder_CreateTemplate");

	std::cout << "Creating probe template" << std::endl;
	err = id3FaceEncoder_CreateTemplate(encoder, probe_image, probe_item, probe_template);
	check(err, "id3FaceEncoder_CreateTemplate");

	/**
   	 * Initialize a face matcher.
   	 */
	ID3_FACE_MATCHER matcher;
	err = id3FaceMatcher_Initialize(&matcher);
	check(err, "id3FaceMatcher_Initialize");
	/**
	 * The matching process returns a score between 0 and 65535. To take a decision this score
	 * must be compared to a defined threshold.
	 * For 1 to 1 authentication, it is recommended to select a threshold of at least 4000 (FMR=1:10K).
	 * Please see documentation to get more information on how to choose the threshold.
	 */
	std::cout << std::endl
		 << "Comparing detected faces" << std::endl;
	int score = 0;
	err = id3FaceMatcher_CompareTemplates(matcher, probe_template, reference_template, &score);
	check(err, "id3FaceMatcher_CompareTemplates");
	std::cout << "   Score: " << score << std::endl;
	if (score > id3FaceMatcherThreshold_Fmr10000)
	{
		std::cout << "   Result: MATCH " << std::endl;
	}
	else
	{
		std::cout << "   Result: NO MATCH " << std::endl;
	}

	/**
	 * Face templates can be exported directly into a file.
	 * When using the SDK face matcher the id3FaceTemplateBufferType_Normal must be used.
	 */
	std::cout << "Export reference template as file" << std::endl;
	std::string reference_template_path = data_dir + "reference_template.bin";
	err = id3FaceTemplate_ToFile(reference_template, reference_template_path.c_str());
	check(err, "id3FaceTemplate_Save");
	/**
	 * Face templates can also directly be exported into a buffer.
	 * Please notice how two calls are performed:
	 * - the first one allow to receive the required buffer size
	 * - the second one fill the allocated buffer with the template data
	 * For a given encoder the size of a template buffer will always stay the same.
	 */
	std::cout << "Export probe template as buffer" << std::endl;
	int probe_template_buffer_size = 0;
	unsigned char* probe_template_buffer = nullptr;
	err = id3FaceTemplate_ToBuffer(probe_template, probe_template_buffer, &probe_template_buffer_size);
	if(err == id3FaceError_InsufficientBuffer) // expected error as an empty buffer has been provided
	{
		probe_template_buffer = (unsigned char*)malloc(probe_template_buffer_size);
		err = id3FaceTemplate_ToBuffer(probe_template, probe_template_buffer, &probe_template_buffer_size);
		check(err, "id3FaceTemplate_ToBuffer with allocated buffer");
		// probe_template_buffer now contains the exported template and could be stored, etc
		free(probe_template_buffer);
	}else 
	{
		check(err, "id3FaceTemplate_ToBuffer with empty buffer");
	}

	//std::cout << std::endl
	//	 << "Press any key..." << std::endl;
	//std::cin.get();
	/**
	 * Dispose of all objects and unload models.
	 */
	err = id3FaceMatcher_Dispose(&matcher);
	err = id3FaceTemplate_Dispose(&probe_template);
	err = id3FaceTemplate_Dispose(&reference_template);
	err = id3FaceEncoder_Dispose(&encoder);
	err = id3DetectedFace_Dispose(&probe_item);
	err = id3DetectedFace_Dispose(&reference_item);
	err = id3DetectedFaceList_Dispose(&probe_list);
	err = id3DetectedFaceList_Dispose(&reference_list);
	err = id3FaceDetector_Dispose(&detector);
	err = id3FaceImage_Dispose(&probe_image);
	err = id3FaceImage_Dispose(&reference_image);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceEncoder9A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
}
