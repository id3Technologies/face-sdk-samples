using System;

namespace id3.Face.Sample
{
	using id3.Face;
    using System.Collections.Generic;
    using System.ComponentModel;
	using System.Windows;

	public static class FaceTools
	{
        public static string modelPath;
        // Global encoder
        public static FaceEncoder Encoder { get; private set; }

		// Event handler for the model loading process
		public static event EventHandler LoadingCompleted;

		public static void Initialize(string modelPath, List<FaceModel> faceModels, FaceModel faceEncoderModels)
		{
            FaceTools.modelPath = modelPath;
            faceModels.Add(faceEncoderModels);

            // id3 Face SDK objects
            var worker = new BackgroundWorker()
            {
                WorkerReportsProgress = true
            };
			worker.DoWork += Worker_DoWork;
			worker.RunWorkerCompleted += Worker_RunWorkerCompleted;
			worker.RunWorkerAsync(faceModels);
		}

		private static void Worker_DoWork(object sender, DoWorkEventArgs e)
		{
			BackgroundWorker worker = (BackgroundWorker)sender;
			try
			{
                // Load models
                List<FaceModel> faceModels = (List<FaceModel>)e.Argument;
                foreach (FaceModel model in faceModels)
                {
                    FaceLibrary.LoadModel(modelPath, model, ProcessingUnit.Cpu);
                }
                FaceModel encoderModel = faceModels[faceModels.Count - 1];
                Encoder = new FaceEncoder() 
                {
                    Model = encoderModel,
                    ThreadCount = Environment.ProcessorCount,
                };
			}
			catch (FaceException ex)
			{
				MessageBox.Show(string.Format("FaceEngine.Worker_DoWork FaceException: {0}", ex.Message));
			}
		}

		private static void Worker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
		{
			LoadingCompleted?.Invoke(null, e);
		}

		// Image ratio for the detection is very important for performance issue
		// Width face detection range is 16 to 512.
		// We fixed detector image width to 512
		public static id3.Face.Image ComputeDetectorImage(id3.Face.Image img_src, int detector_img_w, out float detector_img_ratio)
		{
			int src_w = img_src.Width;
			int src_h = img_src.Height;
			float src_ar = (float)(src_w) / src_h; // compute input aspect ratio
			int detector_img_h = (int)(detector_img_w / src_ar); // compute height
			detector_img_ratio = (float)(src_w) / detector_img_w; // compute detector image ratio
            var result = new Image();
            img_src.ResizeTo(detector_img_w, detector_img_h, result);
            return result;
		}
	}
}
