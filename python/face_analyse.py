import id3face
import os

# This basic sample shows how to detect and analyze a face.

# Before calling any function of the SDK you must first check a valid license file.
# To get such a file please use the provided activation tool.
license_path = os.getenv("ID3_LICENSE_PATH")
id3face.FaceLicense.check_license(license_path)

"""
The Face SDK heavily relies on deep learning and hence requires trained models to run.
Fill in the correct path to the downloaded models.
"""
modelPath = "../sdk/models"

"""
Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
For instance in this sample, we load a detector and an encoder.
"""
print("Loading models... ")
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_DETECTOR_4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)
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
Detect faces in the images, and upscale the result to the original image coordinates
"""
print("Detecting face... ")
detectedFaceList1 = faceDetector.detect_faces(image1_downscaled)
assert detectedFaceList1.count > 0, "No face detected in the image"
detectedFace1 = detectedFaceList1.get_largest_face()
detectedFace1.rescale(1./image1_ratio)
print("Done.\n")


"""
Initialize an instance of face analyser that will run on the CPU using the model 9A (the one previously loaded by FaceLibrary.LoadModel()).
This instance has several parameters that can be set:
- ThreadCount : allocating more than 1 thread here can increase the speed of the process.
"""
faceAnalyser = id3face.FaceAnalyser()
landmarks = faceAnalyser.compute_landmarks(image1, detectedFace1)


"""
Draw the landmarks on the image for visualisation purpose
"""
try:
    import cv2
    print("Iterating the results and drawing on the image")
    image_cv = cv2.imread("../data/image1.jpg")
    
    # Draw Face BoundingBox
    bounds = detectedFace1.bounds
    cv2.rectangle(image_cv, (bounds.top_left.x, bounds.top_left.y), (bounds.bottom_right.x, bounds.bottom_right.y), (0,0,255), 2)
    
    #Draw landmarks
    for i in range(landmarks.count):
        ldm = landmarks.get(i)
        cv2.circle(image_cv, (ldm.X, ldm.Y), 3, (0,255,0), 2)
    
    cv2.imwrite("../data/image1_landmarks.png", image_cv)

except ImportError:
    print("Install opencv-python to plot and visualize the landmarks on the image")


"""
Unload models
"""
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_DETECTOR_4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_LANDMARKS_ESTIMATOR_2A, id3face.ProcessingUnit.CPU)

print("Sample terminated successfully.")