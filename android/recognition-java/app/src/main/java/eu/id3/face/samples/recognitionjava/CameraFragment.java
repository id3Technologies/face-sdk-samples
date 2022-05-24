package eu.id3.face.samples.recognitionjava;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.id3.face.DetectedFace;
import eu.id3.face.FaceTemplate;
import eu.id3.face.PixelFormat;

public class CameraFragment extends Fragment {
    private final static int MAX_PREVIEW_WIDTH = 1920;
    private final static int MAX_PREVIEW_HEIGHT = 1080;
    private final static int REQUEST_CAMERA_PERMISSION = 200;

    private final static String LOG_TAG = "CameraFragment";
    private FaceCapturePreviewView cameraPreview;
    private BoundsView boundsView;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private String cameraId = "";
    CameraDevice cameraDevice_ = null;
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraDevice_ = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
        }
    };

    private CameraCaptureSession cameraCaptureSessions = null;
    private CaptureRequest.Builder captureRequestBuilder = null;
    private Size imageDimension = null;
    private ImageReader imageReader = null;
    private Handler backgroundHandler = null;
    private HandlerThread backgroundThread = null;
    private int sensorOrientation = 0;
    private int displayOrientation = 0;
    private boolean isCapturing = false;
    private boolean needsToEnrollTemplate = false;
    private boolean needsToVerifyTemplate = false;
    private FaceProcessor faceProcessor = null;
    private FaceProcessorListener faceProcessorListener = null;

    /**
     * Links the MainActivity to this fragment so they can communicate.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (faceProcessorListener == null) {
                faceProcessorListener = (FaceProcessorListener) getParentFragment();
                if (faceProcessorListener == null)
                    faceProcessorListener = (FaceProcessorListener) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement FaceProcessorListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    /**
     * Sets up the view when it is created.
     */
    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraPreview = view.findViewById(R.id.cameraPreview);
        boundsView = view.findViewById(R.id.boundsView);
        Paint boundsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundsPaint.setColor(Color.GREEN);
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(3.0f);
        boundsView.setPaint(boundsPaint);
        boundsView.setWillNotDraw(false);
        boundsView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * Sets the paint for bounds drawing.
     */
    void setBoundsPaint(Paint boundsPaint) {
        boundsView.setPaint(boundsPaint);
    }

    /**
     * Sets and configures the face processor for stream processing.
     */
    void setProcessor(FaceProcessor processor) {
        faceProcessor = processor;
    }

    /**
     * Requires the camera background thread to enroll a face template.
     * It triggers the enrolment operation later in the 'onImageAvailableListener'.
     */
    void requestTemplateEnrolment() {
        needsToEnrollTemplate = true;
    }

    /**
     * Requires the camera background thread to verify a face template.
     * It triggers the verification operation later in the 'onImageAvailableListener'.
     */
    void requestTemplateVerification() {
        needsToVerifyTemplate = true;
    }

    /**
     * Starts the capture upon request from UI. It allows the processing to occur in the
     * 'onImageAvailableListener'.
     */
    void startCapture() {
        isCapturing = true;
    }

    /**
     * Stops the capture upon request from UI. It clears the bounds view and stops the processing
     * from occurring in the 'onImageAvailableListener'.
     */
    void stopCapture() {
        boundsView.update(null, 0, 0);
        isCapturing = false;
    }

    /**
     * Starts the background camera thread.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background camera thread.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resumes the camera preview.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
        startBackgroundThread();
        if (cameraPreview.isAvailable()) {
            openCamera(cameraPreview.getWidth(), cameraPreview.getHeight());
        } else {
            cameraPreview.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * Pauses the camera preview.
     */
    @Override
    public void onPause() {
        Log.v(LOG_TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Creates the camera preview.
     */
    private void createCameraPreview() {
        try {
            /* Select the view that will display the camera preview and the image reader that will analyze the output */
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice_.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            CameraCaptureSession.StateCallback cameraStateCallBack = new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if (null == cameraDevice_) {
                        Log.e(LOG_TAG, "updatePreview error, return");
                        return;
                    }
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            };

            /* Create the capture session (2 ways to do this depending on the Android version installed on the device) */
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                /* Configure the session */
                SessionConfiguration sessionConfiguration = new SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        Arrays.asList(
                                new OutputConfiguration(surface),
                                new OutputConfiguration(imageReader.getSurface())
                        ),
                        this.requireActivity().getMainExecutor(), cameraStateCallBack
                );
                cameraDevice_.createCaptureSession(sessionConfiguration);
            } else {
                cameraDevice_.createCaptureSession(
                        Arrays.asList(surface, imageReader.getSurface()),
                        cameraStateCallBack, null
                );
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera in order to display its output.
     */
    private void openCamera(int width, int height) {
        Log.v(LOG_TAG, "open camera");
        CameraManager manager = (CameraManager) this.requireContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            /* Select the cameraId of the front camera */
            for (String camId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == Parameters.cameraType) {
                    cameraId = camId;
                    break;
                }
            }

            /* Get characteristics we need */
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            /* For still image captures, we use the largest available size. */
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());

            /* Get the display rotation of the device (2 ways to do this depending on the Android version installed on the device) */
            int displayRotation;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                displayRotation = requireActivity().getDisplay().getRotation();
            } else {
                displayRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
            }

            /* Compare the sensor orientation of the camera to the display rotation to know if we need to swap dimensions. */
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                    displayOrientation = 0;
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_180:
                    displayOrientation = 180;
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                    displayOrientation = 90;
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                case Surface.ROTATION_270:
                    displayOrientation = 270;
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(LOG_TAG, "Display rotation is invalid: " + displayRotation);
            }

            /* Get the size of the preview window */
            Size previewSize;
            if (swappedDimensions) {
                previewSize = chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture.class),
                        height, width, MAX_PREVIEW_HEIGHT, MAX_PREVIEW_WIDTH, largest
                );
            } else {
                previewSize = chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture.class),
                        width, height, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, largest
                );
            }

            imageDimension = previewSize;

            /* Create the image reader object that will analyze the camera output. */
            imageReader = ImageReader.newInstance(
                    previewSize.getWidth(), previewSize.getHeight(),
                    ImageFormat.YUV_420_888, 10
            );
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

            /* Fit the aspect ratio of TextureView to the size of preview we picked. */
            int orientation = getActivity().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                cameraPreview.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                boundsView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                cameraPreview.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                boundsView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }

            /* Add permission for camera and let user grant the permission. */
            if (ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        this.requireActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION
                );
                return;
            }

            /* Finally open the camera */
            manager.openCamera(cameraId, mStateCallback, backgroundHandler);
            Log.v(LOG_TAG, "Camera opened successfully");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the camera.
     */
    private void closeCamera() {
        if(cameraDevice_ != null)
            cameraDevice_.close();
        if(imageReader != null)
            imageReader.close();
    }

    /**
     * Starts the camera capture session.
     */
    private void updatePreview() {
        if (null == cameraDevice_) {
            Log.e(LOG_TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException cameraAccessException) {
            cameraAccessException.printStackTrace();
        }
    }

    /**
     * Processes the image once it is available from the camera reader.
     */
    private final ImageReader.OnImageAvailableListener onImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            /* Get the image from the reader. */
            Image image = reader.acquireLatestImage();
            if (image == null)
                return;

            if (isCapturing) {
                /* Convert the image from input format to BGR for id3 Face SDK processing functions. */
                /* Get the YUV planes of the image. */
                android.media.Image.Plane[] planes = image.getPlanes();
                int w = image.getWidth();
                int h = image.getHeight();
                int uvPixelStride = planes[1].getPixelStride();
                int uvRowStride = planes[1].getRowStride();
                byte[] plane0 = new byte[planes[0].getBuffer().remaining()];
                planes[0].getBuffer().get(plane0);
                byte[] plane1 = new byte[planes[1].getBuffer().remaining()];
                planes[1].getBuffer().get(plane1);
                byte[] plane2 = new byte[planes[2].getBuffer().remaining()];
                planes[2].getBuffer().get(plane2);

                /* Convert this image to an id3.face Image. */
                eu.id3.face.Image processingImage = eu.id3.face.Image.fromYuvPlanes(
                        plane0,
                        plane1,
                        plane2,
                        w,
                        h,
                        uvPixelStride,
                        uvRowStride,
                        PixelFormat.BGR_24BITS
                );

                /* Rotate image if necessary. */
                int rotationDegrees = displayOrientation - sensorOrientation;
                if (rotationDegrees < 0) {
                    rotationDegrees += 360;
                }
                processingImage.rotate(rotationDegrees);

                /*
                 * Resize image if larger or higher than 'maxProcessingImageSize'.
                 * This operation allows to speed up the face detection process.
                 */
                processingImage.downscale(Parameters.maxProcessingImageSize);

                /* Track faces. */
                DetectedFace detectedFace = faceProcessor.detectLargestFace(processingImage);
                if (detectedFace != null) {
                    eu.id3.face.Rectangle bounds = detectedFace.getBounds();
                    /*
                     * Convert id3.face Rectangle object to android.graphics Rect object and update
                     * bounds view.
                     */
                    Rect rect = new Rect(
                            bounds.topLeft.x,
                            bounds.topLeft.y,
                            bounds.bottomRight.x,
                            bounds.bottomRight.y
                    );
                    boundsView.update(rect, processingImage.getWidth(), processingImage.getHeight());

                    /* Enroll template if requested by UI. */
                    if (needsToEnrollTemplate) {
                        FaceProcessor.EnrollLargestFaceResult enrollLargestFaceResult = faceProcessor.enrollLargestFace(processingImage, detectedFace);
                        faceProcessorListener.onLargestFaceEnrolled(enrollLargestFaceResult);
                        needsToEnrollTemplate = false;
                    }

                    /* Verify template if requested by UI. */
                    if (needsToVerifyTemplate) {
                        FaceProcessor.VerifyLargestFaceResult verifyLargestFaceResult = faceProcessor.verifyLargestFace(processingImage, detectedFace);
                        faceProcessorListener.onLargestFaceVerified(verifyLargestFaceResult);
                        needsToVerifyTemplate = false;
                    }
                } else {
                    boundsView.update(null, 0, 0);
                }
            } else {
                boundsView.update(null, 0, 0);
            }
            image.close();
        }
    };

    /**
     * Chooses the optimal size for the camera preview.
     */
    public Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                  int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(LOG_TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Internal class that implements a comparator for Size objects, by comparing their area.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}

/**
 * Interface that allows a communication between the main activity and this processor.
 */
interface FaceProcessorListener {
    void onLargestFaceEnrolled(FaceProcessor.EnrollLargestFaceResult enrollLargestFaceResult);

    void onLargestFaceVerified(FaceProcessor.VerifyLargestFaceResult verifyLargestFaceResult);
}
