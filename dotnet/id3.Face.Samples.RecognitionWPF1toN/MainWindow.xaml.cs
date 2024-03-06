using System;
using System.Windows;
using System.Windows.Controls;

namespace id3FaceSearchSampleWPF
{
    using System.Windows.Navigation;
	using System.Windows.Controls.Primitives;
    using System.Collections.Generic;
    using id3.Face;
    using id3.Face.Sample;

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
	{
        /// <summary>
        /// List of required models
        /// </summary>
        static readonly List<FaceModel> FaceModels = new List<FaceModel>()
        {
            // Detector model
            FaceModel.FaceDetector3B,
            // Quality model
            FaceModel.FaceEncodingQualityEstimator3A,
        };
        // Encoder model
        static readonly FaceModel FaceTemplateModels = FaceModel.FaceEncoder9A;

        PageDatabase pageDatabase;
		PageSearch pageSearch;

		public MainWindow()
		{
			InitializeComponent();
		}

		private void Window_Loaded(object sender, RoutedEventArgs e)
		{
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
                MessageBox.Show($"Error during license check: {ex.Message}");
                Environment.Exit(-1);
            }

            // Load model in background
            string modelPath = @"..\..\..\..\models";

            FaceDatabase.Initialize(1000, FaceTemplateFormat.V9A);

            FaceTools.LoadingCompleted += FaceEngine_LoadingCompleted;
            FaceTools.Initialize(modelPath, FaceModels, FaceTemplateModels);

			pageDatabase = new PageDatabase() { Tag = buttonWatchList };
			pageSearch = new PageSearch() { Tag = buttonSearch };

			pageDatabase.FaceProcessingStarted += PageDatabase_FaceProcessingStarted;
			pageDatabase.FaceProcessingCompleted += PageDatabase_FaceProcessingCompleted;

			MainFrame.NavigationService.Navigate(pageSearch);
			MainFrame.NavigationService.Navigating += NavigationService_Navigating;
			MainFrame.NavigationService.Navigated += NavigationService_Navigated;

			buttonSearch.IsEnabled = false;
			buttonWatchList.IsEnabled = false;
			pageSearch.IsEnabled = false;
		}

		private void FaceEngine_LoadingCompleted(object sender, EventArgs e)
		{
            pageSearch.InitializeFaceProcess();
            loadingProgessBar.Visibility = Visibility.Hidden;
			buttonSearch.IsEnabled = true;
			buttonWatchList.IsEnabled = true;
			pageSearch.IsEnabled = true;
		}

		private void PageDatabase_FaceProcessingStarted(object sender, EventArgs e)
		{
			buttonSearch.IsEnabled = false;
		}

		private void PageDatabase_FaceProcessingCompleted(object sender, EventArgs e)
		{
			buttonSearch.IsEnabled = true;
		}

		private void NavigationService_Navigating(object sender, NavigatingCancelEventArgs e)
		{		
			var page = MainFrame.NavigationService.Content as Page;
			var button = page.Tag as ToggleButton;
			button.IsChecked = false;
		}

		private void NavigationService_Navigated(object sender, NavigationEventArgs e)
		{
			var page = e.Content as Page;
			var button = page.Tag as ToggleButton;
			button.IsChecked = true;
		}

		private void Button_Search_Click(object sender, RoutedEventArgs e)
		{
			MainFrame.NavigationService.Navigate(pageSearch);
		}

		private void Button_WatchList_Click(object sender, RoutedEventArgs e)
		{
			MainFrame.NavigationService.Navigate(pageDatabase);
		}

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            pageSearch.Close();
            pageDatabase.Close();
        }
    }
}
