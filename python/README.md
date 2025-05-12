# Face SDK Python samples

This repository contains Python samples of **id3 Technologies** Face SDK.

## Installation

First of all you must follow the upper README steps to get a license file. You can skip the SDK installation, as the python wrapper is delivered separately, but you still need to download the models.
Locate the .whl packages in the /python directory, and then install the package corresponding to your Python version

```sh
python -m pip install .\id3face-X.X.X-cpYY-cpYY-PLATFORM.whl
```

Where YY is your version of Python.

### Windows

On Windows, the current wrapper only works from Python 3.9 to 3.11, or you will get the following error : `ImportError: DLL load failed while importing _id3Face:`

### Linux

You may need to add to the path the library needed by the Python wrapper, which is located inside the package installation folder. For example (adjust with your own installation path), use :

```sh
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH":/usr/local/lib/pythonY.Y/dist-packages/id3Face/
```

Where Y.Y is your version of Python.

Once everything is setup you can proceed to the following steps.

## Running the solution

### Filling the license path

Before to execute any of the Python samples, you need to fill in the path to your license in the source code. Look for the following line in the `*.py` files and overwrite `license_path` with your correct path.

```sh
license_path = "../id3Face.lic"
```

### Ensuring models are present

Following models are required to be in the sdk/models/ directory:

- For **face_recognition** sample:
  - face_detector_v4b.id3nn
  - face_encoder_v9a.id3nn
- For **face_analyse** sample:
  - face_detector_v4b.id3nn
  - face_landmarks_estimator_v2a.id3nn
- For **portrait_processor** sample:
  - cf. portrait_processor.py

### Run

 ```sh
 python face_recognition.py
 python face_analyse.py
 python portrait_processor.py
 ```
