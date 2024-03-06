using System.Windows.Media.Imaging;

namespace id3FaceSearchSampleWPF
{
	using id3.Face;

	public class FaceDatabaseItem
	{
		/// <summary>
		/// Unique ID for users
		/// </summary>
		public int UserID { get; set; }

		/// <summary>
		/// Title for the database gui interface
		/// !! BIND WITH the DataTemplate of PageDatabase.MatchPairList !!
		/// </summary>
		public string Title { get; set; }

		/// <summary>
		/// Bitmap to draw for the database gui interface
		/// !! BIND WITH the DataTemplate of PageDatabase.MatchPairList !!
		/// </summary>
		public BitmapSource BitmapSource { get; set; }

		/// <summary>
		/// Original name of the source image
		/// </summary>
		public string ImageName { get; set; }
	}
}
