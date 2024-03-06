using id3.Face;
using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace id3FaceSearchSampleWPF
{
    using id3.Face.Sample;
    using id3.Face.Sample.WPF;
    using System.Diagnostics;

    /// <summary>
    /// Interaction logic for FaceDatabasePage.xaml
    /// </summary>
    public partial class PageDatabase : Page, INotifyPropertyChanged
    {
        // Background worker for database enroll process
        BackgroundWorker _worker;

		// Event handler for the database enroll process
		public event EventHandler FaceProcessingStarted;
		public event EventHandler FaceProcessingCompleted;
        public event PropertyChangedEventHandler PropertyChanged;

        public int ColumnCount { get; private set; } = 8;
        public int ThumbnailWidth { get; set; } = 120;


        public PageDatabase()
		{
			InitializeComponent();

            // DataBinding source
            DataContext = this;
            panelProgress.Visibility = Visibility.Hidden;

            _worker = new BackgroundWorker()
			{
				WorkerReportsProgress = true,
				WorkerSupportsCancellation = true,
			};

			_worker.DoWork += Worker_DoWork;
			_worker.ProgressChanged += Worker_ProgressChanged;
			_worker.RunWorkerCompleted += Worker_RunWorkerCompleted;

			MatchPairList.ItemsSource = FaceDatabase.ReferenceDataList;
		}

        public void Close()
        {
        }

		private void ButtonLoadImg_Click(object sender, RoutedEventArgs e)
		{
			var dialog = new OpenFileDialog()
			{
				Filter = "All supported image files|*.bmp;*.tif;*.tiff;*.jpg;*.jpeg;*.png;*.jpg;*.jp2;*.gif"
						+ "|BMP - Windows Bitmap (.bmp)|*.bmp"
						+ "|PNG - Portable Network Graphics (.png)|*.png"
						+ "|TIF - Tagged Image File Format (.tif)|*.tif;*.tiff"
						+ "|JPG - JPG/JPEG Format (.jpg)|*.jpg;*.jpeg"
						+ "|JP2 - JPEG 2000 Format (.jp2)|*.jp2"
						+ "|GIF - Graphics Interchange Format (.gif)|*.gif",
				Multiselect = true
			};

			if (dialog.ShowDialog() == true)
			{
				progressBar.Maximum = dialog.FileNames.Count();
				FaceProcessingStarted?.Invoke(null, e);
				buttonLoadImg.Visibility = Visibility.Hidden;
				_worker.RunWorkerAsync(dialog.FileNames);
			}
		}

        private void Worker_DoWork(object sender, DoWorkEventArgs e)
		{
			var worker = sender as BackgroundWorker;
			IEnumerable<string> files = e.Argument as IEnumerable<string>;

            int progressFiles = 0;
			int id = FaceDatabase.Count;

			try
			{
				FaceDetector detector = new FaceDetector()
				{
					ConfidenceThreshold = 60,
					ThreadCount = Environment.ProcessorCount
				};
				FaceAnalyser analyser = new FaceAnalyser();

				// face detection
				foreach (string filename in files)
				{
					using (var img_src = id3.Face.Image.FromFile(filename, id3.Face.PixelFormat.Bgr24Bits))
					{
						int progress = 0;

						// Check if img already in database
						string img_name = System.IO.Path.GetFileNameWithoutExtension(filename);

						var item = FaceDatabase.ReferenceDataList.SingleOrDefault(x => x.ImageName == img_name);
						if (item == null)
						{
							// Width face detection range is 16 to 512.
							float detector_img_ratio;
							var img_detector = FaceTools.ComputeDetectorImage(img_src, 512, out detector_img_ratio);
							var faceList = new List<FaceDatabaseItem>();
							var detectedFaces = detector.DetectFaces(img_detector);
							img_detector.Dispose();
							if (detectedFaces.Count > 0)
							{
								detectedFaces.RescaleAll(detector_img_ratio);

								var face = detectedFaces.GetLargestFace();
								var facedata = new FaceDatabaseItem()
								{
									UserID = ++id,
									Title = System.IO.Path.GetFileNameWithoutExtension(filename),
									ImageName = img_name
								};

								int quality = FaceTools.Encoder.ComputeQuality(img_src, face);
                                var iod = face.GetInterocularDistance();
                                if (iod >= 30)
                                {
                                    //var roi = img_src.ExtractRoi(face.GetPortraitBounds(0.25f, 0.45f, 1.33f));
                                    var roi = img_src.ExtractRoi(face.GetPortraitBounds(0.45f, 0.45f, 1.33334f));
                                    //roi.ToFile(@"C:\Users\gpinel\Downloads\Gal_Gadot_2_roi.webp", 100);
                                    facedata.BitmapSource = ImageDrawing.ToBitmapSource(roi);
                                    facedata.BitmapSource.Freeze();

                                    try
                                    {
                                        // create template
                                        var sw = Stopwatch.StartNew();
                                        var template = FaceTools.Encoder.CreateTemplate(img_src, face);
                                        Console.WriteLine($"{filename}: Q={quality} (compute in {sw.ElapsedMilliseconds} ms)");

                                        // update template list
                                        lock (FaceDatabase.ReferenceListLock)
                                            FaceDatabase.Add(facedata.UserID.ToString(), template);
                                    }
                                    catch (FaceException ex)
                                    {
                                        MessageBox.Show($"PageDatabase.Worker_DoWork.CreateTemplate FaceException: {ex.Message}");
                                    }
                                    catch (Exception ex)
                                    {
                                        MessageBox.Show($"PageDatabase.Worker_DoWork.CreateTemplate exception: {ex.Message}");
                                    }

                                    faceList.Add(facedata);
                                    FaceDatabase.ReferenceDataList.Add(facedata);
                                }
                            }
							worker.ReportProgress(progress++, faceList);
						}
						worker.ReportProgress(++progressFiles, null);
					}
				}
				analyser.Dispose();
				detector.Dispose();
			}
			catch (FaceException ex)
			{
				MessageBox.Show($"PageDatabase.Worker_DoWork FaceException: {ex.Message}");
			}
		}

		private void Worker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
		{
			panelProgress.Visibility = Visibility.Hidden;
			buttonLoadImg.Visibility = Visibility.Visible;
			FaceProcessingCompleted?.Invoke(null, e);
		}

		private void Worker_ProgressChanged(object sender, ProgressChangedEventArgs e)
		{
			if (e.UserState is List<FaceDatabaseItem>)
			{
				var faces = e.UserState as List<FaceDatabaseItem>;
				panelProgress.Visibility = Visibility.Visible;
			}
			else
			{
				progressBar.Value = e.ProgressPercentage;
			}
		}

        private void MatchPairList_PreviewMouseWheel(object sender, MouseWheelEventArgs e)
        {
            ScrollViewer scv = (ScrollViewer)sender;
            scv.ScrollToVerticalOffset(scv.VerticalOffset - e.Delta);
            e.Handled = true;

        }

        private void ScrollViewer_SizeChanged(object sender, SizeChangedEventArgs e)
        {
            ScrollViewer scv = (ScrollViewer)sender;
            ColumnCount = (int)(scv.ActualWidth / (ThumbnailWidth + 8));
            NotifyPropertyChanged("ColumnCount");
        }

        private void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
