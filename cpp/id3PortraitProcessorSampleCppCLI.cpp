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
         * Load models necessary for Portrait Processor.
         * It only has to be called once and then multiple instances of ID3_FACE_DETECTOR can be created.
         */
		std::cout << "Loading models" << std::endl;
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceEncoder9B, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceLandmarksEstimator2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FacePoseEstimator1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceColorBasedPad3A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceEncodingQualityEstimator3A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceAgeEstimator1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceExpressionClassifier1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceAttributesClassifier2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceOcclusionDetector2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_EyeGazeEstimator2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_EyeOpennessDetector1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_EyeRednessDetector1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceMaskClassifier2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::loadModel(models_dir.c_str(), id3FaceModel_FaceBackgroundUniformity1A, id3FaceProcessingUnit_Cpu);
		/**
         * Load picture from files.
         */
		std::string image_path = data_dir + "image1.jpg";
		std::cout << "Loading reference image: " << image_path.c_str() << std::endl;
		Image image;
		image.fromFile(image_path.c_str(), id3FacePixelFormat_Bgr24Bits);
		// Resize to 512 because of detector limit.
		image.resize(512, 0);
		/**
         * Initialize portrait pocessor.
         */
		std::cout << "Initializing portrait processor " << std::endl;
		PortraitProcessor processor;
		/**
         * Initialize portrait.
         */
		std::cout << "Initializing portrait from image " << std::endl;
		auto portrait = processor.createPortrait(image);
		/**
         * Get age estimation.
         */
		std::cout << "Estimating age... ";
		processor.estimateAge(portrait);
		auto age = portrait.getAge();
		std::cout << "\t\t\t" << age << " years" << std::endl;
		/**
         * Get expression estimation.
         */
		std::cout << "Estimating expression... ";
		processor.estimateExpression(portrait);
		auto expression = portrait.getExpression();
		std::cout << "\t\t" << expression << std::endl;
		/**
         * Get background uniformity estimation.
         */
		std::cout << "Estimating background uniformity... " << std::endl;
		processor.estimateBackgroundUniformity(portrait);
		auto bgUniformity = portrait.getBackgroundUniformity();
		std::cout << "\tColor uniformity:         \t" << bgUniformity.ColorUniformity << std::endl;
		std::cout << "\tStructure uniformity:     \t" << bgUniformity.StructureUniformity << std::endl;
		/**
         * Get ICAO geometric attributes.
         */
		std::cout << "Computing ICAO geometric attributes... " << std::endl;
		processor.estimateGeometryQuality(portrait);
		auto geomAttributes = portrait.getGeometricAttributes();
		std::cout << "\tHead image height ratio:  \t" << geomAttributes.HeadImageHeightRatio << std::endl;
		std::cout << "\tHead image width ratio:   \t" << geomAttributes.HeadImageWidthRatio << std::endl;
		std::cout << "\tHorizontal position:      \t" << geomAttributes.HorizontalPosition << std::endl;
		std::cout << "\tVertical position:        \t" << geomAttributes.VerticalPosition << std::endl;
		std::cout << "\tResolution:               \t" << geomAttributes.Resolution << std::endl;
		/**
         * Get face landmarks.
         */
		auto landmarks = portrait.getLandmarks();

		/**
         * Get face attributes.
         */

		std::cout << "Detecting occlusions" << std::endl;
		processor.detectOcclusions(portrait);

		std::cout << "Estimating face attributes" << std::endl;
		processor.estimateFaceAttributes(portrait);

		auto eyeGaze = portrait.getEyeGaze();
		std::cout << "\tLeft eye x gaze:          \t" << eyeGaze.LeftEyeXGaze << " degrees" << std::endl;
		std::cout << "\tLeft eye y gaze:          \t" << eyeGaze.LeftEyeYGaze << " degrees" << std::endl;
		std::cout << "\tRight eye x gaze:         \t" << eyeGaze.RightEyeXGaze << " degrees" << std::endl;
		std::cout << "\tRight eye y gaze:         \t" << eyeGaze.RightEyeYGaze << " degrees" << std::endl;

		auto leftEyeVisibilityScore  = portrait.getLeftEyeVisibility();
		auto leftEyeOpeningScore     = portrait.getLeftEyeOpening();
		auto rightEyeVisibilityScore = portrait.getRightEyeVisibility();
		auto rightEyeOpeningScore    = portrait.getRightEyeOpening();
		std::cout << "\tLeft eye visibility score:    \t" << leftEyeVisibilityScore << std::endl;
		std::cout << "\tLeft eye opening score:       \t" << leftEyeOpeningScore << std::endl;
		std::cout << "\tRight eye visibility score:   \t" << rightEyeVisibilityScore << std::endl;
		std::cout << "\tRight eye opening score:      \t" << rightEyeOpeningScore << std::endl;

		auto genderMaleScore      = portrait.getGenderMale();
		auto glassesScore         = portrait.getGlasses();
		auto hatScore             = portrait.getHat();
		auto lookStraightScore    = portrait.getLookStraightScore();
		auto makeupScore          = portrait.getMakeup();
		auto mouthOpeningScore    = portrait.getMouthOpening();
		auto mouthVisibilityScore = portrait.getMouthVisibility();
		auto noseVisibilityScore  = portrait.getNoseVisibility();
		auto smileScore           = portrait.getSmile();
		auto faceMaskScore        = portrait.getFaceMask();
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
		processor.estimatePhotographicQuality(portrait);
		auto icaoCheckpoints = portrait.getQualityCheckpoints();

		/**
         * get global quality score
         * NOTE: must be called after all estimations
         */
		auto qualityScore = portrait.getQualityScore();
		std::cout << "Global quality score:           \t" << qualityScore << std::endl;

		// std::cout << std::endl
		//	 << "Press any key..." << std::endl;
		// std::cin.get();
		/**
         * Dispose of all objects and unload models.
         */
		FaceLibrary::unloadModel(id3FaceModel_FaceDetector4B, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceEncoder9B, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceLandmarksEstimator2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FacePoseEstimator1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceColorBasedPad3A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceEncodingQualityEstimator3A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceAgeEstimator1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceExpressionClassifier1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceAttributesClassifier2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceOcclusionDetector2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_EyeGazeEstimator2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_EyeOpennessDetector1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_EyeRednessDetector1A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceMaskClassifier2A, id3FaceProcessingUnit_Cpu);
		FaceLibrary::unloadModel(id3FaceModel_FaceBackgroundUniformity1A, id3FaceProcessingUnit_Cpu);

		std::cout << "Sample terminated successfully." << std::endl;
	}
	catch (FaceException &ex) {
		std::cout << ex.what() << std::endl;
	}
}
