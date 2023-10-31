using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Threading;
using System.Windows.Forms;

using OpenCvSharp;
using OpenCvSharp.Extensions;

namespace id3.Face.Samples.RecognitionWF
{
    using System.Runtime.InteropServices;
    using id3.Face;

    public partial class Form1 : Form
    {
        class WorkerProgress
        {
            public long TrackTime;
            public long IndexToDraw;
        }

        Bitmap[] bitmapBuffer;
        BackgroundWorker camera;
        VideoCapture capture;
        bool isCameraRunning = false;

        /*
         * id3Face SDK objects.
        */
        Image image;
        DetectedFaceList detectedFaceList;
        FaceAnalyser faceAnalyser;
        FaceDetector faceDetector;
        FaceEncoder faceEncoder;
        FaceMatcher faceMatcher;
        FaceTemplate enrollee;
        bool isTemplateEnrolled = false;

        public Form1()
        {
            InitializeComponent();

            FormClosed += Form1_FormClosed;

            // UI elements
            buttonStartCapture.Click += ButtonStartCapture_Click;
            buttonEnroll.Click += ButtonEnroll_Click;
            buttonMatch.Click += ButtonMatch_Click;
            buttonEnroll.Enabled = false;
            buttonMatch.Enabled = false;
            buttonStartCapture.Enabled = IsCameraPlugged();

            if (!buttonStartCapture.Enabled)
            {
                MessageBox.Show("Please plug-in a webcam before to use this sample.");
                Environment.Exit(-1);
            }

            // Camera background worker
            camera = new BackgroundWorker()
            {
                WorkerReportsProgress = true,
                WorkerSupportsCancellation = true
            };
            camera.DoWork += Camera_DoWork;
            camera.ProgressChanged += Camera_ProgressChanged;
            camera.RunWorkerCompleted += Camera_RunWorkerCompleted;

            // Bitmap buffer for UI/process
            bitmapBuffer = new Bitmap[2];

            // id3 Face SDK objects
            try
            {
                /*
                 * Before calling any function of the SDK you must first check a valid license file.
                 * To get such a file please use the provided activation tool.
                 */
                string licensePath = Environment.GetEnvironmentVariable("ID3_LICENSE_PATH");
                FaceLicense.CheckLicense(licensePath);
            }
            catch (FaceException ex)
            {
                MessageBox.Show("Error during license check: " + ex.Message);
                Environment.Exit(-1);
            }

            /*
             * The Face SDK heavily relies on deep learning technics and hence requires trained models to run.
             * Fill in the correct path to the downloaded models.
             */
            string modelPath = "..\\..\\..\\..\\..\\sdk\\models";

            try
            {
                /*
                * Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
                */
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector4B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceEncoder9B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FacePoseEstimator1A, ProcessingUnit.Cpu);

                /*
                 * Init objects.
                 */
                detectedFaceList = new DetectedFaceList();
                faceAnalyser = new FaceAnalyser();
                faceDetector = new FaceDetector()
                {
                    ConfidenceThreshold = 50,
                    Model = FaceModel.FaceDetector4B,
                    ProcessingUnit = ProcessingUnit.Cpu,
                    ThreadCount = 4
                };
                faceEncoder = new FaceEncoder()
                {
                    Model = FaceModel.FaceEncoder9B,
                    ProcessingUnit = ProcessingUnit.Cpu,
                    ThreadCount = 4
                };
                faceMatcher = new FaceMatcher();
            }
            catch (FaceException ex)
            {
                MessageBox.Show("Error during face objects initialization: " + ex.Message);
                Environment.Exit(-1);
            }
        }

        private void Form1_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (isCameraRunning)
            {
                StopCaptureCamera();
            }
        }

        // UI events

        private void ButtonStartCapture_Click(object sender, EventArgs e)
        {
            if (buttonStartCapture.Text.Equals("Start capture"))
            {
                StartCaptureCamera();
                buttonStartCapture.Text = "Stop capture";
                buttonEnroll.Enabled = true;
                buttonMatch.Enabled = isTemplateEnrolled;
            }
            else
            {
                StopCaptureCamera();
                buttonStartCapture.Text = "Start capture";
                buttonEnroll.Enabled = false;
                buttonMatch.Enabled = false;
            }
        }

        private void ButtonEnroll_Click(object sender, EventArgs e)
        {
            if (detectedFaceList.GetCount() < 1)
            {
                return;
            }

            enrollee = GetPortraitAndCreateTemplate(pictureBoxEnrollee);

            isTemplateEnrolled = true;
            buttonMatch.Enabled = true;
        }

        private void ButtonMatch_Click(object sender, EventArgs e)
        {
            if (detectedFaceList.GetCount() < 1)
            {
                return;
            }

            FaceTemplate candidate = GetPortraitAndCreateTemplate(pictureBoxCandidate);

            int score = faceMatcher.CompareTemplates(candidate, enrollee);

            labelMatchScore.Text = string.Format("Match score: {0}", score);
        }

        // Camera events

        private void Camera_DoWork(object sender, DoWorkEventArgs e)
        {
            Mat frame = new Mat();
            capture = new VideoCapture(0);
            capture.Open(0);

            int bitmapIndex = 0;

            // For real-time processing, consider downscaling the image if necessary
            int maxSize = 512;

            if (capture.IsOpened())
            {
                while (!camera.CancellationPending)
                {
                    capture.Read(frame);
                    Bitmap bitmap = BitmapConverter.ToBitmap(frame);

                    // Create image from the first frame
                    byte[] pixels = new byte[3 * frame.Width * frame.Height];
                    Marshal.Copy(frame.Data, pixels, 0, 3 * frame.Width * frame.Height);
                    image = Image.FromRawBuffer(pixels, frame.Width, frame.Height, 3 * frame.Width, PixelFormat.Bgr24Bits, PixelFormat.Bgr24Bits);

                    // Resize for real-time capacity
                    float scale = image.Downscale(maxSize);

                    Stopwatch stopWatch = Stopwatch.StartNew();

                    // Track faces
                    faceDetector.TrackFaces(image, detectedFaceList);

                    long trackTime = stopWatch.ElapsedMilliseconds;

                    // For each detected face...
                    for (int i = 0; i < detectedFaceList.GetCount(); i++)
                    {
                        // 'Get' does not perform a deep copy of the native handle
                        // Hence we clone the 'DetectedFace' to have a deep copy
                        DetectedFace detectedFace = (DetectedFace)detectedFaceList.Get(i).Clone();

                        // ...draw results
                        using (Graphics gr = Graphics.FromImage(bitmap))
                        {
                            // Rescale the copy of the detected to draw in fullscale
                            detectedFace.Rescale(1.0F / scale);

                            gr.DrawRectangle(new Pen(Color.Green, 2), ConvertRectangle(detectedFace.Bounds));
                        }
                    }

                    bitmapBuffer[bitmapIndex] = bitmap;

                    WorkerProgress workerProgress = new WorkerProgress()
                    {
                        TrackTime = trackTime,
                        IndexToDraw = bitmapIndex
                    };

                    camera.ReportProgress(0, workerProgress);
                }
            }
        }

        private void Camera_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            WorkerProgress workerProgress = (WorkerProgress)e.UserState;
            pictureBoxPreview.Image = bitmapBuffer[workerProgress.IndexToDraw];
            labelDetectionTime.Text = string.Format("Detection time: {0} ms", workerProgress.TrackTime);
        }

        private void Camera_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            isCameraRunning = false;
        }

        // Utils

        private bool IsCameraPlugged()
        {
            capture = new VideoCapture(0);
            bool ret = capture.Open(0);
            if (ret)
            {
                capture.Release();
            }
            return ret;
        }

        private void StartCaptureCamera()
        {
            camera.RunWorkerAsync();
            isCameraRunning = true;
        }

        private void StopCaptureCamera()
        {
            camera.CancelAsync();
            while (isCameraRunning)
            {
                Application.DoEvents();
                Thread.Sleep(100);
            }
        }

        private FaceTemplate GetPortraitAndCreateTemplate(PictureBox pictureBox)
        {
            /*
             * Selects the largerst face in case there are several persons on the picture.
             */
            DetectedFace detectedFace = detectedFaceList.GetLargestFace();

            /*
             * Gets aligned portrait of the face.
             */
            Rectangle portraitBounds = detectedFace.GetPortraitBounds(0.25f, 0.45f, 1.33f);
            Image enrolleePortrait = image.ExtractRoi(portraitBounds);

            using (MemoryStream memStream = new MemoryStream(enrolleePortrait.ToBuffer(ImageFormat.Jpeg, 0)))
            {
                pictureBox.Image = System.Drawing.Image.FromStream(memStream);
            }

            /*
             * Extract unique features of the face.
             */
            return faceEncoder.CreateTemplate(image, detectedFace);
        }

        private System.Drawing.Rectangle ConvertRectangle(Rectangle rectangle)
        {
            return new System.Drawing.Rectangle(rectangle.TopLeft.X,
                rectangle.TopLeft.Y,
                rectangle.TopRight.X - rectangle.TopLeft.X,
                rectangle.BottomLeft.Y - rectangle.TopLeft.Y);
        }
    }
}
