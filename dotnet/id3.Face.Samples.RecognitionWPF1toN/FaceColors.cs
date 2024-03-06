using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media;

namespace id3.Face.Sample.WPF
{
	public static class FaceColors
	{
		private static readonly Color DefaultBorder = Blue;

		/// <summary>
		/// Gets the default text fore color.
		/// </summary>
		public static Color TextForeground { get { return Color.FromRgb(255, 255, 255); } }

        /// <summary>
        /// Gets a default face bounds border color that has a red dominant.
        /// </summary>
        public static Color Red { get { return Color.FromRgb(255, 45, 45); } }

        /// <summary>
        /// Gets a default face bounds border color that has a blue dominant.
        /// </summary>
        public static Color Blue { get { return Color.FromRgb(29, 183, 239); } }

		/// <summary>
		/// Gets a default face bounds color that has a green dominant.
		/// </summary>
		public static Color Green { get { return Color.FromRgb(0, 221, 52); } }

		public static Color FromColor(byte a, Color color)
		{
			return Color.FromArgb(a, color.R, color.G, color.B);
		}
}
}
