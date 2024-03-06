using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace id3FaceSearchSampleWPF
{
	using id3.Face;
	using System.ComponentModel;
    using System.Threading;
    using id3.Face.Sample;
    using id3.Face.Sample.WPF;
    using System.Diagnostics;


    /// <summary>
    /// Interaction logic for FaceSearchPage.xaml
    /// </summary>
    public partial class PageSearch : Page
	{
		// BackgroundWorker for detec and match process
		BackgroundWorker _workerMatch;
		// Local Face detector 
		FaceDetector _faceDetector;
		// Local Face analyser
		FaceAnalyser _faceAnalyser;
		// Local Face matcher
		FaceMatcher _faceMatcher;
        // Face macting threshold
        int _faceMatchingThreshold = 3000;

        public PageSearch()
		{
			InitializeComponent();
            imageSearch.Source = null;
            labelScore.Text = "";
            labelSearch.Text = "";

            // Face macther
            _faceMatcher = new FaceMatcher();

            // Background worker for match process
            _workerMatch = new BackgroundWorker()
			{
				WorkerReportsProgress = true,
				WorkerSupportsCancellation = true
			};
            _workerMatch.DoWork += WorkerMatch_DoWork;
            _workerMatch.ProgressChanged += _workerMatch_ProgressChanged;
            _workerMatch.RunWorkerCompleted += _workerMatch_RunWorkerCompleted;

            lblStatus.Text = "Loading, please wait ...";
        }

        // Initialize face process
        // Must be called when face models are loaded
        public void InitializeFaceProcess()
		{
            int num_thread = ((Environment.ProcessorCount - 1) > 4) ? 4 : (Environment.ProcessorCount - 1);
            try
			{
				_faceDetector = new FaceDetector()
				{
					ThreadCount = num_thread
                };
			}
			catch (FaceException ex)
			{
				MessageBox.Show($"PageSearch.initializeFaceProcess.FaceDetector exception: {ex.Message}");
			}

			try
			{
				_faceAnalyser = new FaceAnalyser();
			}
			catch (FaceException ex)
			{
				MessageBox.Show($"PageSearch.initializeFaceProcess.FaceAnalyser exception: {ex.Message}");
			}

            lblStatus.Text = "Initialization done.";
		}

        public void Close()
        {
            _workerMatch.CancelAsync();
            Thread.Sleep(100);
        }

        #region Match process
		private void matchProcess(id3.Face.Image img, DetectedFace face, FaceData facedata)
		{
            int quality = 0;
            try
            {
                quality = FaceTools.Encoder.ComputeQuality(img, face);
            }
            catch (FaceException ex)
            {
                MessageBox.Show($"WorkerLive_DoWork.ComputeQuality FaceException: {ex.Message}");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"WorkerLive_DoWork.ComputeQuality exception: {ex.Message}");
            }

            facedata.Quality = quality;
            if (FaceDatabase.Count > 0)
			{
				if (quality > 30)
				{
					try
					{
                        var sw = Stopwatch.StartNew();
                        var template = FaceTools.Encoder.CreateTemplate(img, face);
                        Console.WriteLine($"Create tempate : {sw.ElapsedMilliseconds} ms");

                        sw = Stopwatch.StartNew();
                        var candidates = new FaceCandidateList();
                        _faceMatcher.SearchTemplate(FaceDatabase.ReferenceTemplateList, template, 1, candidates);
                        Console.WriteLine($"FaceMatcher.SearchTemplate : {sw.ElapsedMilliseconds} ms");

                        sw = Stopwatch.StartNew();
                        var candidates2 = FaceDatabase.SearchTemplate(template, 1);
                        Console.WriteLine($"FaceIndexer.SearchTemplate : {sw.ElapsedMilliseconds} ms");

                        if (candidates?.Count > 0)
						{
                            var candidate = candidates[0];
                            var candidate2 = candidates2[0];
                            if (candidate.Score >= _faceMatchingThreshold)
							{
								// Find the reference data associated with this candidate
								var referenceData = FaceDatabase.ReferenceDataList.FirstOrDefault(x => x.UserID.ToString() == candidate.Id);
								if (referenceData != null)
								{
                                    facedata.Quality = quality;
                                    facedata.CandidateScore = candidate.Score;
                                    facedata.DatabaseItem = referenceData;
                                }
							}
						}
					}
					catch (FaceException ex)
					{
						MessageBox.Show($"WorkerLive_DoWork.CreateTemplate FaceException: {ex.Message}");
					}
					catch (Exception ex)
					{
						MessageBox.Show($"WorkerLive_DoWork.CreateTemplate exception: {ex.Message}");
					}
				}
			}
        }
        #endregion

        #region BackgroundWorker detect, encode and match process
		private void WorkerMatch_DoWork(object sender, DoWorkEventArgs e)
		{
			var worker = sender as BackgroundWorker;
            try
            {
                var img_src = e.Argument as id3.Face.Image;

                float detector_img_ratio;
                var img_detector = FaceTools.ComputeDetectorImage(img_src, 512, out detector_img_ratio);
                var detectedFaces = _faceDetector.DetectFaces(img_detector);
                img_detector.Dispose();

                if (detectedFaces.Count > 0)
                {
                    var face = detectedFaces.GetLargestFace();
                    face.Rescale(detector_img_ratio);
                    var facedata = new CustomFaceData()
                    {
                        FaceBounds = face.Bounds,
                        Face = face
                    };
                    // Update GUI
                    worker.ReportProgress(0, facedata);
                    // Call the matching process
                    matchProcess(img_src, facedata.Face, facedata);
                    {
                        // Update GUI
                        worker.ReportProgress(0, facedata);
                    }
                }
            }
            catch (Exception ex)
            {
                Console.Write(ex.Message);
            }
        }

        private void _workerMatch_ProgressChanged(object sender, ProgressChangedEventArgs e)
		{
            if (e.UserState != null)
            {
                if (e.UserState is CustomFaceData)
                {
                    var face = e.UserState as CustomFaceData;
                    canvasDrawing.Children.Clear();
                    drawFaceBounds(face);
                    canvasDrawing.Children.Add(face.Canvas);
                }
            }
        }

        private void _workerMatch_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            buttonOpenFile.Visibility = Visibility.Visible;
        }
#endregion

        // Draw only one face
        private void drawFaceBounds(CustomFaceData face)
		{
            if (face != null)
            {
                if (face.Canvas == null)
                {
                    face.CreateCanvas();
                }
                if (face.Quality > 0)
                {
                    if (face.DatabaseItem != null)
                    {
                        face.Canvas.BoundsColor = FaceColors.FromColor(200, FaceColors.Green);
                        imageSearch.BeginInit();
                        imageSearch.Source = face.DatabaseItem.BitmapSource;
                        imageSearch.EndInit();
                        labelSearch.Text = face.DatabaseItem.Title;
                        labelScore.Text = face.CandidateScore.ToString();
                    }
                    else
                    {
                        face.Canvas.BoundsColor = FaceColors.FromColor(200, FaceColors.Blue);
                    }
                    face.Canvas.Text = face.Quality.ToString();
                    face.Canvas.ShowsText = true;
                }
                face.Canvas.Update(face);
            }
		}

        private void ButtonOpenFile_Click(object sender, RoutedEventArgs e)
		{
			var dialog = new OpenFileDialog();
			if (dialog.ShowDialog() == true)
			{
                imageSearch.Source = null;
                labelSearch.Text = "";
                labelScore.Text = "";
                // Load image
                var img_src = id3.Face.Image.FromFile(dialog.FileName, id3.Face.PixelFormat.Bgr24Bits);
                // Draw Image
                imagePreview.BeginInit();
                imagePreview.Source = ImageDrawing.ToBitmapSource(img_src);
                imagePreview.EndInit();
                canvasDrawing.Children.Clear();
                // Start match process
                _workerMatch.RunWorkerAsync(img_src.Clone());
            }
        }
	}

    public class CustomFaceData : FaceData
    {
        /// <summary>
        /// Drawing process
        /// </summary>
        public FaceCanvas Canvas { get; set; }

        public void CreateCanvas()
        {
            Canvas = new FaceCanvas()
            {
                ShowsText = false,
                BoundsColor = FaceColors.FromColor(200, FaceColors.Blue)
            };
        }
    }
}
