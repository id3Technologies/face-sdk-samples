/**
 * This sample shows how to perform basic face recognition tasks using id3 Face SDK.
 */

#include <iostream>
#include "id3FaceLib.h"

void check(int err, const std::string &func_name)
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
	 * Load models necessary for Portrait Processor.
	 * It only has to be called once and then multiple instances of ID3_FACE_DETECTOR can be created.
	 */
	std::cout << "Loading models" << std::endl;
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceDetector4B");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceEncoder10B, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceEncoder10B");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceLandmarksEstimator2A, id3FaceProcessingUnit_Cpu);
    check(err, "id3FaceLibrary_LoadModel FaceLandmarksEstimator2A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FacePoseEstimator1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FacePoseEstimator1A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceColorBasedPad4A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceColorBasedPad4A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceAgeEstimator1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceAgeEstimator1A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceExpressionClassifier1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceExpressionClassifier1A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceAttributesClassifier2A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceAttributesClassifier2A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceOcclusionDetector2A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceOcclusionDetector2A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_EyeGazeEstimator2A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel EyeGazeEstimator2A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_EyeOpennessDetector1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel EyeOpennessDetector1A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_EyeRednessDetector1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel EyeRednessDetector1A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceMaskClassifier2A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceMaskClassifier2A");
	err = id3FaceLibrary_LoadModel(models_dir.c_str(), id3FaceModel_FaceBackgroundUniformity1A, id3FaceProcessingUnit_Cpu);
	check(err, "id3FaceLibrary_LoadModel FaceBackgroundUniformity1A");
	/**
	 * Load picture from files.
	 */
	std::string image_path = data_dir + "image1.jpg";
	std::cout << "Loading reference image: " << image_path.c_str() << std::endl;
	ID3_FACE_IMAGE image;
	err = id3FaceImage_Initialize(&image);
	err = id3FaceImage_FromFile(image, image_path.c_str(), id3FacePixelFormat_Bgr24Bits);
	check(err, "id3FaceImage_FromFile");
	// Resize to 512 because of detector limit.
	id3FaceImage_Resize(image, 512, 0);
	/**
	 * Initialize portrait pocessor.
	 */
	std::cout << "Initializing portrait processor " << std::endl;
	ID3_FACE_PORTRAIT_PROCESSOR processor;
	err = id3FacePortraitProcessor_Initialize(&processor);
	check(err, "id3FacePortraitProcessor_Initialize");
	/**
	 * Initialize portrait.
	 */
	std::cout << "Initializing portrait from image " << std::endl;
	ID3_FACE_PORTRAIT portrait;
	err = id3FacePortrait_Initialize(&portrait);
	check(err, "id3FacePortrait_Initialize");
	err = id3FacePortraitProcessor_CreatePortrait(processor, image, portrait);
	check(err, "id3FacePortraitProcessor_CreatePortrait");
	/**
	 * Get age estimation.
	 */
	std::cout << "Estimating age... ";
	err = id3FacePortraitProcessor_EstimateAge(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimateAge");
	int age;
	err = id3FacePortrait_GetAge(portrait, &age);
	check(err, "id3FacePortrait_GetAge");
	std::cout << "\t\t\t" << age << " years" << std::endl;
	/**
	 * Get expression estimation.
	 */
	std::cout << "Estimating expression... ";
	err = id3FacePortraitProcessor_EstimateExpression(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimateExpression");
	id3FaceExpression expression;
	err = id3FacePortrait_GetExpression(portrait, &expression);
	check(err, "id3FacePortrait_GetExpression");
	std::cout << "\t\t" << expression << std::endl;
	/**
	 * Get background uniformity estimation.
	 */
	std::cout << "Estimating background uniformity... " << std::endl;
	err = id3FacePortraitProcessor_EstimateBackgroundUniformity(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimateBackgroundUniformity");
	id3FaceBackgroundUniformity bgUniformity;
	err = id3FacePortrait_GetBackgroundUniformity(portrait, &bgUniformity);
	check(err, "id3FacePortrait_GetBackgroundUniformity");
	std::cout << "\tColor uniformity:         \t" << bgUniformity.ColorUniformity << std::endl;
	std::cout << "\tStructure uniformity:     \t" << bgUniformity.StructureUniformity << std::endl;
	/**
	 * Get ICAO geometric attributes.
	 */
	std::cout << "Computing ICAO geometric attributes... " << std::endl;
	err = id3FacePortraitProcessor_EstimateGeometryQuality(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimateGeometryQuality");
	id3FaceGeometricAttributes geomAttributes;
	err = id3FacePortrait_GetGeometricAttributes(portrait, &geomAttributes);
	check(err, "id3FacePortrait_GetGeometricAttributes");
	std::cout << "\tHead image height ratio:  \t" << geomAttributes.HeadImageHeightRatio << std::endl;
	std::cout << "\tHead image width ratio:   \t" << geomAttributes.HeadImageWidthRatio << std::endl;
	std::cout << "\tHorizontal position:      \t" << geomAttributes.HorizontalPosition << std::endl;
	std::cout << "\tVertical position:        \t" << geomAttributes.VerticalPosition << std::endl;
	std::cout << "\tResolution:               \t" << geomAttributes.Resolution << std::endl;
	/**
	 * Get face landmarks.
	 */
	ID3_FACE_POINT_LIST landmarks;
	err = id3FacePointList_Initialize(&landmarks);
	check(err, "id3FacePointList_Initialize");
	err = id3FacePortrait_GetLandmarks(portrait, landmarks);
	check(err, "id3FacePortrait_GetLandmarks");

	/**
	 * Get face attributes.
	 */

	std::cout << "Detecting occlusions" << std::endl;
	err = id3FacePortraitProcessor_DetectOcclusions(processor, portrait);
	check(err, "id3FacePortraitProcessor_DetectOcclusions");

	std::cout << "Estimating face attributes" << std::endl;
	err = id3FacePortraitProcessor_EstimateFaceAttributes(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimateFaceAttributes");

	id3FaceEyeGaze eyeGaze;
	err = id3FacePortrait_GetEyeGaze(portrait, &eyeGaze);
	check(err, "id3FacePortrait_GetEyeGaze");
	std::cout << "\tLeft eye x gaze:          \t" << eyeGaze.LeftEyeXGaze << " degrees" << std::endl;
	std::cout << "\tLeft eye y gaze:          \t" << eyeGaze.LeftEyeYGaze << " degrees" << std::endl;
	std::cout << "\tRight eye x gaze:         \t" << eyeGaze.RightEyeXGaze << " degrees" << std::endl;
	std::cout << "\tRight eye y gaze:         \t" << eyeGaze.RightEyeYGaze << " degrees" << std::endl;

	int leftEyeVisibilityScore;
	err = id3FacePortrait_GetLeftEyeVisibility(portrait, &leftEyeVisibilityScore);
	check(err, "id3FacePortrait_GetLeftEyeVisibility");
	int leftEyeOpeningScore;
	err = id3FacePortrait_GetLeftEyeOpening(portrait, &leftEyeOpeningScore);
	check(err, "id3FacePortrait_GetRightEyeVisibility");
	int rightEyeVisibilityScore;
	err = id3FacePortrait_GetRightEyeVisibility(portrait, &rightEyeVisibilityScore);
	check(err, "id3FacePortrait_GetRightEyeVisibility");
	int rightEyeOpeningScore;
	err = id3FacePortrait_GetRightEyeOpening(portrait, &rightEyeOpeningScore);
	check(err, "id3FacePortrait_GetRightEyeOpening");
	std::cout << "\tLeft eye visibility score:    \t" << leftEyeVisibilityScore << std::endl;
	std::cout << "\tLeft eye opening score:       \t" << leftEyeOpeningScore << std::endl;
	std::cout << "\tRight eye visibility score:   \t" << rightEyeVisibilityScore << std::endl;
	std::cout << "\tRight eye opening score:      \t" << rightEyeOpeningScore << std::endl;

	int genderMaleScore;
	err = id3FacePortrait_GetGenderMale(portrait, &genderMaleScore);
	check(err, "id3FacePortrait_GetGenderMale");
	int glassesScore;
	err = id3FacePortrait_GetGlasses(portrait, &glassesScore);
	check(err, "id3FacePortrait_GetGlasses");
	int hatScore;
	err = id3FacePortrait_GetHat(portrait, &hatScore);
	check(err, "id3FacePortrait_GetHat");
	int lookStraightScore;
	err = id3FacePortrait_GetLookStraightScore(portrait, &lookStraightScore);
	check(err, "id3FacePortrait_GetLookStraightScore");
	int makeupScore;
	err = id3FacePortrait_GetMakeup(portrait, &makeupScore);
	check(err, "id3FacePortrait_GetMakeup");
	int mouthOpeningScore;
	err = id3FacePortrait_GetMouthOpening(portrait, &mouthOpeningScore);
	check(err, "id3FacePortrait_GetMouthOpening");
	int mouthVisibilityScore;
	err = id3FacePortrait_GetMouthVisibility(portrait, &mouthVisibilityScore);
	check(err, "id3FacePortrait_GetMouthVisibility");
	int noseVisibilityScore;
	err = id3FacePortrait_GetNoseVisibility(portrait, &noseVisibilityScore);
	check(err, "id3FacePortrait_GetNoseVisibility");
	int smileScore;
	err = id3FacePortrait_GetSmile(portrait, &smileScore);
	check(err, "id3FacePortrait_GetSmile");
	int faceMaskScore;
	err = id3FacePortrait_GetFaceMask(portrait, &faceMaskScore);
	check(err, "id3FacePortrait_GetFaceMask");
	std::cout << "\tGender male score:            \t" << genderMaleScore << std::endl;
	std::cout << "\tGlasses score:                \t" << glassesScore << std::endl;
	std::cout << "\tHat score:                    \t" << hatScore << std::endl;
	std::cout << "\tLook Straight score:          \t" << lookStraightScore << std::endl;
	std::cout << "\tMakeup score:                 \t" << makeupScore << std::endl;
	std::cout << "\tMouth opening score:          \t" << mouthOpeningScore << std::endl;
	std::cout << "\tMouth visibility score:       \t" << mouthVisibilityScore << std::endl;
	std::cout << "\tNose visibility score:        \t" << noseVisibilityScore << std::endl;
	std::cout << "\tSmile score:                  \t" << smileScore << std::endl;
	std::cout << "\tFace mask score:              \t" << faceMaskScore << std::endl;

	/**
	 * get ICAO criterias statuses
	 * NOTE: must be called after all estimations
	 */
	err = id3FacePortraitProcessor_EstimatePhotographicQuality(processor, portrait);
	check(err, "id3FacePortraitProcessor_EstimatePhotographicQuality");
	id3FacePortraitQualityCheckpoints icaoCheckpoints;
	err = id3FacePortrait_GetQualityCheckpoints(portrait, &icaoCheckpoints);
	check(err, "id3FacePortrait_GetQualityCheckpoints");

	/**
	 * get global quality score
	 * NOTE: must be called after all estimations
	 */
	int qualityScore;
	err = id3FacePortrait_GetQualityScore(portrait, &qualityScore);
	check(err, "id3FacePortrait_GetQualityScore");
	std::cout << "Global quality score:           \t" << qualityScore << std::endl;

	// std::cout << std::endl
	//	 << "Press any key..." << std::endl;
	// std::cin.get();
	/**
	 * Dispose of all objects and unload models.
	 */
	err = id3FacePointList_Dispose(&landmarks);
	err = id3FacePortrait_Dispose(&portrait);
	err = id3FacePortraitProcessor_Dispose(&processor);
	err = id3FaceImage_Dispose(&image);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceEncoder9B, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceLandmarksEstimator2A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FacePoseEstimator1A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceColorBasedPad3A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceAgeEstimator1A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceExpressionClassifier1A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceAttributesClassifier2A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceOcclusionDetector2A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_EyeGazeEstimator2A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_EyeOpennessDetector1A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_EyeRednessDetector1A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceMaskClassifier2A, id3FaceProcessingUnit_Cpu);
	err = id3FaceLibrary_UnloadModel(id3FaceModel_FaceBackgroundUniformity1A, id3FaceProcessingUnit_Cpu);

	std::cout << "Sample terminated successfully." << std::endl;
}
