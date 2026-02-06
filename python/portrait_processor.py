import id3face
import os

# This basic sample shows how to detect and analyze a face via PortraitProcessor.

# Before calling any function of the SDK you must first check a valid license file.
# To get such a file please use the provided activation tool.
id3face.FaceLicense.check_license("../id3Face.lic")

"""
The Face SDK heavily relies on deep learning and hence requires trained models to run.
Fill in the correct path to the downloaded models.
"""
modelPath = "../models"

"""
Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
For instance in this sample, we load a detector and the models used by PortraitProcesor.
"""
print("Loading models... ")
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_DETECTOR_4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_ENCODER_10B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_POSE_ESTIMATOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_COLOR_BASED_PAD_4A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_AGE_ESTIMATOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_EXPRESSION_CLASSIFIER_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_OCCLUSION_DETECTOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.EYE_GAZE_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.EYE_OPENNESS_DETECTOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.EYE_REDNESS_DETECTOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_MASK_CLASSIFIER_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_BACKGROUND_UNIFORMITY_1A, id3face.ProcessingUnit.CPU)
print("Done.\n")

"""
Load sample images from files.
"""
print("Loading image from files... ")
image1 = id3face.Image.from_file("../data/image1.jpg", id3face.PixelFormat.BGR_24_BITS)
print("Done.\n")

"""
Initialize an instance of face detector that will run on the CPU.
This instance has several parameters that can be set:
- ConfidenceThreshold: the detection score above which proposals will be considered as detected faces. For model 4B, the recommanded value is 50. In the range [0:100].
- ThreadCount : allocating more than 1 thread here can increase the speed of the process.
"""
faceDetector = id3face.FaceDetector(
    thread_count=4
)

"""
Downscale the image to a maximum dimension of 512px. The detectors might miss big faces in high resolution images. Keep the ratio to upscale the results later.
"""
image1_downscaled = image1.clone()
image1_ratio = image1_downscaled.downscale(512)

"""
Initialize portait processor and portrait.
"""
print("Initializing portrait from image... ")
processor = id3face.PortraitProcessor()
portrait = processor.create_portrait(image1_downscaled)
print("Done.\n")

"""
Get age estimation.
"""
print("Estimating age... ", end='')
processor.estimate_age(portrait)
age = portrait.age
print(f"\t\t\t{age} years\n")

"""
Get expression estimation.
"""
print("Estimating expression... ", end='')
processor.estimate_expression(portrait)
expression = portrait.expression
print(f"\t\t{expression.name}\n")

"""
Get background uniformity.
"""
print("Estimating background uniformity... ")
processor.estimate_background_uniformity(portrait)
bgUniformity = portrait.background_uniformity
print(f"\tColor uniformity:         \t{bgUniformity.color_uniformity}")
print(f"\tStructure uniformity:     \t{bgUniformity.structure_uniformity}\n")

"""
Get ICAO geometric attributes.
"""
print("Computing ICAO geometric attributes... ")
processor.estimate_geometry_quality(portrait)
geomAttributes = portrait.geometric_attributes
print(f"\tHead image height ratio:  \t{geomAttributes.head_image_height_ratio}")
print(f"\tHead image width ratio:   \t{geomAttributes.head_image_width_ratio}")
print(f"\tHorizontal position:      \t{geomAttributes.horizontal_position}")
print(f"\tVertical position:        \t{geomAttributes.vertical_position}")
print(f"\tResolution:               \t{geomAttributes.resolution}\n")

"""
Get face attributes.
"""
print("Detecting occlusions... ")
processor.detect_occlusions(portrait)
print("Done.\n")

print("Estimating face attributes... \n")
processor.estimate_face_attributes(portrait)
eyeGaze = portrait.eye_gaze
print(f"\tLeft eye x gaze:          \t{eyeGaze.left_eye_x_gaze}°")
print(f"\tLeft eye y gaze:          \t{eyeGaze.left_eye_y_gaze}°")
print(f"\tRight eye x gaze:         \t{eyeGaze.right_eye_x_gaze}°")
print(f"\tRight eye y gaze:         \t{eyeGaze.right_eye_y_gaze}°\n")

leftEyeVisibilityScore = portrait.left_eye_visibility
leftEyeOpeningScore = portrait.left_eye_opening
rightEyeVisibilityScore = portrait.right_eye_visibility
rightEyeOpeningScore = portrait.right_eye_opening
print(f"\tLeft eye visibility score:    \t{leftEyeVisibilityScore}")
print(f"\tLeft eye opening score:       \t{leftEyeOpeningScore}")
print(f"\tRight eye visibility score:   \t{rightEyeVisibilityScore}")
print(f"\tRight eye opening score:      \t{rightEyeOpeningScore}\n")

genderMaleScore = portrait.gender_male
glassesScore = portrait.glasses
hatScore = portrait.hat
lookStraightScore = portrait.look_straight_score
makeupScore = portrait.makeup
mouthOpeningScore = portrait.mouth_opening
mouthVisibilityScore = portrait.mouth_visibility
noseVisibilityScore = portrait.nose_visibility
smileScore = portrait.smile
faceMaskScore = portrait.face_mask
print(f"\tGender male score:            \t{genderMaleScore}")
print(f"\tGlasses score:                \t{glassesScore}")
print(f"\tHat score:                    \t{hatScore}")
print(f"\tLook Straight score:          \t{lookStraightScore}")
print(f"\tMakeup score:                 \t{makeupScore}")
print(f"\tMouth opening score:          \t{mouthOpeningScore}")
print(f"\tMouth visibility score:       \t{mouthVisibilityScore}")
print(f"\tNose visibility score:        \t{noseVisibilityScore}")
print(f"\tSmile score:                  \t{smileScore}")
print(f"\tFace mask score:              \t{faceMaskScore}\n")

"""
Get ICAO criterias statuses
NOTE: must be called after all estimations
"""
processor.estimate_photographic_quality(portrait)
icaoCheckpoints = portrait.quality_checkpoints

"""
Get global quality score
NOTE: must be called after all estimations
"""
qualityScore = portrait.quality_score
print(f"Global quality score:           \t{qualityScore}\n")


"""
Draw the landmarks on the image for visualisation purpose
"""
try:
    import cv2
    print("Iterating the results and drawing on the image")
    image_cv = cv2.imread("../data/image1.jpg")  

    #Detect faces in the images, and upscale the result to the original image coordinates
    print("Estimating face landmarks... ")
    detectedFaceList1 = faceDetector.detect_faces(image1_downscaled)
    assert detectedFaceList1.count > 0, "No face detected in the image"
    detectedFace1 = detectedFaceList1.get_largest_face()
    detectedFace1.rescale(1./image1_ratio)

    # Draw Face BoundingBox
    bounds = detectedFace1.bounds
    cv2.rectangle(image_cv, (bounds.top_left.x, bounds.top_left.y), (bounds.bottom_right.x, bounds.bottom_right.y), (0,0,255), 2)
    
    #Get face landmarks
    portraitOriginal = processor.create_portrait(image1)
    landmarks = portraitOriginal.landmarks
    print("Done.\n")

    #Draw landmarks
    for i in range(landmarks.count):
        ldm = landmarks.get(i)
        cv2.circle(image_cv, (ldm.x, ldm.y), 3, (0,255,0), 2)
    
    imageLandmarksPath = "../data/image1_landmarks.png"
    cv2.imwrite(imageLandmarksPath, image_cv)
    print(f"Landmarks image saved to {imageLandmarksPath}.\n")

    #cv2.imshow('image1 landmarks', image_cv)
    #cv2.waitKey(0)

except ImportError:
    print("Install opencv-python to plot and visualize the landmarks on the image")


"""
Unload models
"""
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_DETECTOR_4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_POSE_ESTIMATOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_COLOR_BASED_PAD_4A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_AGE_ESTIMATOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_EXPRESSION_CLASSIFIER_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_ATTRIBUTES_CLASSIFIER_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_OCCLUSION_DETECTOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.EYE_GAZE_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.EYE_OPENNESS_DETECTOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.EYE_REDNESS_DETECTOR_1A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_MASK_CLASSIFIER_2A, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_BACKGROUND_UNIFORMITY_1A, id3face.ProcessingUnit.CPU)

print("Sample terminated successfully.")