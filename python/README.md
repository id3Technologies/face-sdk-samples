# Face SDK Python samples

This repository contains Python samples of **id3 Technologies** Face SDK.

## Installation

First of all you must follow the upper README steps to get a license file. You can skip the SDK installation, as the python wrapper is delivered separately, but you still need to download the models.
Contact id3 to get the `.tar.gz` Python package, and then install it with 
```
pip install .\id3Face-X.X.X.tar.gz
```

### Windows
On Windows, the current wrapper only works with Python 3.7, or you will get the following error : `ImportError: DLL load failed while importing _id3Face:`

### Linux
You may need to add to the path the library needed by the Python wrapper, which is located inside the package installation folder. For example (adjust with your own installation path), use : 
```
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH":/usr/local/lib/python3.8/dist-packages/id3Face/
```

Once everything is setup you can proceed to the following steps.

## Running the solution

### Filling the license path

Before to execute any of the Python samples, you need to fill in the path to your license in the source code. Look for the following line in the `*.py` files and overwrite `license_path` with your correct path.

```
license_path = os.getenv("ID3_LICENSE_PATH")
```

You may also set the environment variable : `export ID3_LICENSE_PATH=<path/to/license.lic>`

### Ensuring models are present

Following models are required to be in the sdk/models/ directory:
- For **face_recognition** sample:
    - face_detector_v4b.id3nn
    - face_encoder_v9a.id3nn
- For **face_analyse** sample:
    - face_detector_v4b.id3nn
    - face_landmarks_estimator_v2a.id3nn

### Run
 ```
 python face_recognition.py
 python face_analyse.py
 ```
