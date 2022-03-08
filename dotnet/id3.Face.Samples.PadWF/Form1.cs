using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.Threading;
using System.Windows.Forms;

using OpenCvSharp;
using OpenCvSharp.Extensions;

namespace id3.Face.Samples.PadWF
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
        FaceDetector faceDetector;
        FacePad facePad;

        public Form1()
        {
            InitializeComponent();

            FormClosed += Form1_FormClosed;

            // UI elements
            buttonStartCapture.Click += ButtonStartCapture_Click;
            buttonComputePad.Click += ButtonComputePad_Click;
            buttonComputePad.Enabled = false;
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
                FaceLibrary.CheckLicense(@"your_license_path_here");
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
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector3B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceBlurrinessDetector1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceColorBasedPad1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceMoireDetector1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceAttackSupportDetector1A, ProcessingUnit.Cpu);

                /*
                 * Init objects.
                 */
                detectedFaceList = new DetectedFaceList();
                faceDetector = new FaceDetector()
                {
                    ConfidenceThreshold = 70,
                    Model = FaceModel.FaceDetector3B,
                    ProcessingUnit = ProcessingUnit.Cpu,
                    ThreadCount = 4
                };
                facePad = new FacePad();
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
                buttonComputePad.Enabled = true;
            }
            else
            {
                StopCaptureCamera();
                buttonStartCapture.Text = "Start capture";
                buttonComputePad.Enabled = false;
            }
        }

        private void ButtonComputePad_Click(object sender, EventArgs e)
        {
            if (detectedFaceList.GetCount() != 1)
            {
                return;
            }

            /*
             * Select the largerst face in case there are several persons on the picture.
             */
            DetectedFace detectedFace = detectedFaceList.GetLargestFace();

            /*
             * Compute PAD scores.
             */
            int colorScore = facePad.ComputeColorBasedScore(image, detectedFace);
            int moireScore = facePad.ComputeMoireScore(image, detectedFace);
            int blurrinessScore = facePad.ComputeBlurrinessScore(image, detectedFace);
            DetectedFaceAttackSupport attackSupport = facePad.DetectAttackSupport(image, detectedFace);

            labelColorPadScore.Text = "Color PAD score: " + colorScore;
            labelMoireScore.Text = "Moiré score: " + moireScore;
            labelBlurrinessScore.Text = "Blurriness score: " + blurrinessScore;
            labelAttackSupportScore.Text = "Attack support score: " + attackSupport.Score;
        }

        // Camera events

        private void Camera_DoWork(object sender, DoWorkEventArgs e)
        {
            Mat frame = new Mat();
            capture = new VideoCapture(0);
            capture.Open(0);

            int bitmapIndex = 0;

            // For real-time processing, consider downscaling the image if necessary
            // However, if doing so, keep a full resolution copy of the image for PAD!
            int maxSize = 512;
            float scale = 1F;

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
                    scale = image.Downscale(maxSize);

                    Stopwatch stopWatch = Stopwatch.StartNew();

                    // Track faces
                    faceDetector.TrackFaces(image, detectedFaceList);

                    long trackTime = stopWatch.ElapsedMilliseconds;

                    // For each detected face...
                    for (int i = 0; i < detectedFaceList.GetCount(); i++)
                    {
                        DetectedFace detectedFace = detectedFaceList.Get(i);

                        // ...draw results
                        using (Graphics gr = Graphics.FromImage(bitmap))
                        {
                            detectedFace.Rescale(scale);

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

                    // Release native memory
                    image.Dispose();
                }
            }
        }

        private void Camera_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            WorkerProgress workerProgress = (WorkerProgress)e.UserState;
            pictureBoxPreview.Image = bitmapBuffer[workerProgress.IndexToDraw];
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

        private System.Drawing.Rectangle ConvertRectangle(Rectangle rectangle)
        {
            return new System.Drawing.Rectangle(rectangle.TopLeft.X,
                rectangle.TopLeft.Y,
                rectangle.TopRight.X - rectangle.TopLeft.X,
                rectangle.BottomLeft.Y - rectangle.TopLeft.Y);
        }
    }
}
