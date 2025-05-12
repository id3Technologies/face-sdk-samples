using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.Threading;
using System.Windows.Forms;
using OpenCvSharp;
using OpenCvSharp.Extensions;
using System.Collections.Concurrent;
using System.Runtime.InteropServices;
using id3.Face;

namespace id3.Face.Samples.PadWF
{
    public partial class Form1 : Form
    {
        /*
         * Put here the webcam index to use.
         */
        private int _cameraIndex = 0;

        private PortraitProcessor _processor;
        private Portrait _portrait;
        private bool _portraitCreated;

        class WorkerProgress
        {
            public long IndexToDraw;
        }

        class QueueData
        {
            public Image image;
        }

        class FacePadResult
        {
            public FacePadResult() {}
            public FacePadResult(string message) { this.message = message; }
            public string message;
            public PortraitInstruction Instruction;
            public PadStatus Status;
            public int Score;
        }

        Bitmap[] bitmapBuffer;
        BackgroundWorker camera;
        BackgroundWorker pad;
        ConcurrentQueue<QueueData> queue;
        VideoCapture capture;
        bool isCameraRunning = false;

        // id3Face SDK objects.
        public Form1()
        {
            InitializeComponent();

            FormClosed += Form1_FormClosed;

            // UI elements
            buttonStartCapture.Click += ButtonStartCapture_Click;
            buttonStartCapture.Enabled = IsCameraPlugged();

            if (!buttonStartCapture.Enabled)
            {
                MessageBox.Show("Please plug-in a webcam before to use this sample.");
                Environment.Exit(-1);
            }

            // id3 Face SDK objects
            try
            {
                /*
                 * Before calling any function of the SDK you must first check a valid license file.
                 * To get such a file please use the provided activation tool.
                 */
                FaceLicense.CheckLicense(@"..\..\..\..\id3Face.lic");
            }
            catch (FaceException ex)
            {
                MessageBox.Show("Error during license check: " + ex.Message);
                Environment.Exit(-1);
            }

            // Bitmap buffer for UI/process
            bitmapBuffer = new Bitmap[2];

            /*
             * The Face SDK heavily relies on deep learning technics and hence requires trained models to run.
             * Fill in the correct path to the downloaded models.
             */
            string modelPath = @"..\..\..\..\models";

            try
            {
                // Once a model is loaded in the desired processing unit (CPU or GPU) several instances of the associated processor can be created.
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceDetector4B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceEncoder9B, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceLandmarksEstimator2A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FacePoseEstimator1A, ProcessingUnit.Cpu);
                FaceLibrary.LoadModel(modelPath, FaceModel.FaceColorBasedPad3A, ProcessingUnit.Cpu);
            }
            catch (FaceException ex)
            {
                MessageBox.Show("Error during face objects initialization: " + ex.Message);
                Environment.Exit(-1);
            }

            queue = new ConcurrentQueue<QueueData>();

            _processor = new PortraitProcessor();
            _portraitCreated = false;

            // PAD background worker
            pad = new BackgroundWorker()
            {
                WorkerReportsProgress = true,
                WorkerSupportsCancellation = true
            };
            pad.DoWork += Pad_DoWork;
            pad.ProgressChanged += Pad_ProgressChanged;
            pad.RunWorkerAsync();

            // Camera background worker
            camera = new BackgroundWorker()
            {
                WorkerReportsProgress = true,
                WorkerSupportsCancellation = true
            };
            camera.DoWork += Camera_DoWork;
            camera.ProgressChanged += Camera_ProgressChanged;
            camera.RunWorkerCompleted += Camera_RunWorkerCompleted;
        }

        private void Form1_FormClosed(object sender, FormClosedEventArgs e)
        {
            pad.CancelAsync();
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
            }
            else
            {
                StopCaptureCamera();
                buttonStartCapture.Text = "Start capture";
            }
        }

        private void Pad_DoWork(object sender, DoWorkEventArgs e)
        {
            while (!pad.CancellationPending)
            {
                if (queue.TryDequeue(out QueueData queueData))
                {
                    // Compute PAD properties.
                    try
                    {
                        if (!_portraitCreated)
                        {
                            _portrait = new Portrait();
                            _portraitCreated = true;
                        }                     
                        _processor.UpdatePortrait(_portrait, queueData.image);
                        _processor.EstimatePhotographicQuality(_portrait);
                        _processor.DetectPresentationAttack(_portrait);

                        FacePadResult facePadResult = new FacePadResult()
                        {
                            Instruction = _portrait.Instruction,
                            Status = _portrait.PadStatus,
                            Score = _portrait.PadScore,
                        };
                        pad.ReportProgress(0, facePadResult);
                    }
                    catch (FaceException ex)
                    {
                        pad.ReportProgress(0, new FacePadResult(ex.Message));
                    }
                    catch (Exception ex)
                    {
                        Trace.WriteLine(ex.Message);
                    }
                }
            }
        }

        private void Pad_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            FacePadResult facePadResult = (FacePadResult)e.UserState;
            if (string.IsNullOrEmpty(facePadResult.message))
            {
                labelInstruction.Text = $"Instruction: {facePadResult.Instruction}";
                labelColorPadScore.Text = $"Color PAD score: {facePadResult.Score}";
                labelPadStatus.Text = $"PAD Status: {facePadResult.Status}";
            }
            else
            {
                labelInstruction.Text = "Instruction:";
                labelColorPadScore.Text = "Color PAD score:";
                labelPadStatus.Text = $"PAD Status: {facePadResult.message}";
            }
        }

        // Camera events

        private void Camera_DoWork(object sender, DoWorkEventArgs e)
        {
            var frame = new Mat();
            capture = new VideoCapture(0);
            capture.Open(_cameraIndex);

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
                    var image = Image.FromRawBuffer(pixels, frame.Width, frame.Height, 3 * frame.Width, PixelFormat.Bgr24Bits, PixelFormat.Bgr24Bits);

                    // Resize for real-time capacity
                    scale = image.Downscale(maxSize);

                    // Draw tracked face bounds
                    if (_portrait != null && _portrait.TrackedFace != null)
                    {
                        using (Graphics gr = Graphics.FromImage(bitmap))
                        {
                            TrackedFace trackedFace = (TrackedFace)_portrait.TrackedFace.Clone();
                            trackedFace.Rescale(1 / scale);
                            gr.DrawRectangle(new Pen(Color.Green, 2), ConvertRectangle(trackedFace.Bounds));
                        }
                    }

                    // Add image for processing
                    {
                        if (queue.Count == 0)
                        {
                            var queueData = new QueueData()
                            {
                                image = (Image)image.Clone()
                            };
                            queue.Enqueue(queueData);
                        }
                    }

                    bitmapBuffer[bitmapIndex] = bitmap;

                    var workerProgress = new WorkerProgress()
                    {
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
            var workerProgress = (WorkerProgress)e.UserState;
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
            bool ret = capture.Open(_cameraIndex);
            if (ret)
            {
                capture.Release();
            }
            return ret;
        }

        private void StartCaptureCamera()
        {
            _portraitCreated = false;
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
