import id3face
import os

# This basic sample shows how to encode two faces and compare them.

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
id3face.FaceLibrary.load_model(modelPath, id3face.FaceModel.FACE_ENCODER_9A, id3face.ProcessingUnit.CPU)
print("Done.\n")

"""
Load sample images from files.
"""
print("Loading images from files... ")
image1 = id3face.Image.from_file("../data/image1.jpg", id3face.PixelFormat.BGR_24_BITS)
image2 = id3face.Image.from_file("../data/image2.jpg", id3face.PixelFormat.BGR_24_BITS)
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
Detect faces in the images.
"""
print("Detecting faces... ")
detectedFaceList1 = faceDetector.detect_faces(image1_downscaled)
detectedFaceList2 = faceDetector.detect_faces(image2)
print("Done.\n")


"""
Initialize an instance of face encoder that will run on the CPU using the model 9A (the one previously loaded by FaceLibrary.LoadModel()).
This instance has several parameters that can be set:
- ThreadCount : allocating more than 1 thread here can increase the speed of the process.
"""
faceEncoder = id3face.FaceEncoder(
    thread_count=4
)

"""
Create the template from the largest detected faces in each image.
For the downscaled image, we rescale the detected to the original image, to enroll the template on the full resolution image.
"""
print("Creating templates... ")
assert detectedFaceList1.count > 0, "No face detected in the image"
detectedFace1 = detectedFaceList1.get_largest_face()
detectedFace1.rescale(1./image1_ratio)
faceTemplate1 = faceEncoder.create_template(image1, detectedFace1)

assert detectedFaceList2.count > 0, "No face detected in the image"
detectedFace2 = detectedFaceList2.get_largest_face()
faceTemplate2 = faceEncoder.create_template(image2, detectedFace2)
print("Done.\n")

"""
Initialize a face matcher instance.
"""
faceMatcher = id3face.FaceMatcher()
"""
Compare the two templates.
The matching process returns a score between 0 and 65535. To take a decision this score must be compared to a defined threshold.
It is recommended to select a threshold associated to at least an FMR of 1:10000.
Please see documentation to get more information on how to choose the threshold.
"""
print("Comparing templates... ")
score = faceMatcher.compare_templates(faceTemplate2, faceTemplate1)
print("Done.\n")
if score > id3face.FaceMatcherThreshold.FMR10000:
    print(f"Match: {score}")
else:
    print(f"No match: {score}")

"""
Face templates can be exported directly into a file or a buffer.
When using the SDK face matcher the id3FaceTemplateBufferType_Normal must be used.
"""
print("Export template 1 as file...")
faceTemplate1.to_file("../data/template1.bin")

"""
Unload models
"""
id3face.FaceLibrary.unload_model(id3face.FaceModel_FaceDetector4B, id3face.ProcessingUnit.CPU)
id3face.FaceLibrary.unloadModel(id3face.FaceModel.FACE_ENCODER_9A, id3face.ProcessingUnit.CPU)

print("Sample terminated successfully.")