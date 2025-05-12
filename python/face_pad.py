import id3face
import os

# This basic sample shows how to detect a presentation attack on an image.

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
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_COLOR_BASED_PAD_3A, id3face.ProcessingUnit.CPU)
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
Detect largest face in image.
"""
detectedFaceList1 = faceDetector.detect_faces(image1_downscaled)
assert detectedFaceList1.count > 0, "No face detected in the image"
detectedFace1 = detectedFaceList1.get_largest_face()


"""
Detect presentation attack.
"""
facePadInstance = id3face.FacePad()
result = facePadInstance.compute_color_based_score(image1_downscaled, detectedFace1)
print(f"PAD score:\t{result.score}")


"""
Unload models.
"""
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_DETECTOR_4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unload_model(id3face.FaceModel.FACE_COLOR_BASED_PAD_3A, id3face.ProcessingUnit.CPU)

print("Sample terminated successfully.")