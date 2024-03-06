namespace id3.Face.Sample
{
	using id3.Face;
    using id3FaceSearchSampleWPF;

    public class FaceData
	{
		/// <summary>
		/// Face bounds
		/// </summary>
		public Rectangle FaceBounds { get; set; }

		/// <summary>
		/// Face quality
		/// </summary>
		public int Quality { get; set; }

        /// <summary>
        /// Face candiate score
        /// </summary>
        public int CandidateScore { get; set; }

        /// <summary>
        /// Face
        /// </summary>
        public DetectedFace Face { get; set; }

        /// <summary>
        /// Database Item
        /// </summary>
        public FaceDatabaseItem DatabaseItem { get; set; }
}
}
