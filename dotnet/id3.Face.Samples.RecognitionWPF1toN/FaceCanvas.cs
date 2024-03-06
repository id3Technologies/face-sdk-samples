using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;

namespace id3.Face.Sample.WPF
{
	/// <summary>
	/// This class represents a canvas used to draw around a detected face.
	/// </summary>
	public class FaceCanvas : Canvas
	{
		private byte _boundsBackOpacity = 30;
		private TextBlock _faceText = null;
		private Canvas _faceBoundsCanvas = null;
		private Color _boundsColor = FaceColors.Blue;

		#region ctor
		public FaceCanvas() : base()
		{
			_faceBoundsCanvas = new Canvas()
			{
				Visibility = Visibility.Hidden
			};

			// Background
			_faceBoundsCanvas.Children.Add(new System.Windows.Shapes.Rectangle()
			{
				Fill = new SolidColorBrush(Color.FromArgb(_boundsBackOpacity, BoundsColor.R, BoundsColor.G, BoundsColor.B)),
				StrokeThickness = 0,
			});

			// corners
			_faceBoundsCanvas.Children.Add(new Path()
			{
				Stroke = new SolidColorBrush(BoundsColor),
				StrokeEndLineCap = PenLineCap.Round,
				StrokeStartLineCap = PenLineCap.Round,
				Data = new PathGeometry(),
			});

			_faceText = new TextBlock()
			{
				Foreground = new SolidColorBrush(FaceColors.TextForeground),
				Background = new SolidColorBrush(BoundsColor),
				TextAlignment = TextAlignment.Center,
				TextTrimming = TextTrimming.CharacterEllipsis,
				TextWrapping = TextWrapping.Wrap,
				FontFamily = new FontFamily("Helvetica"),
				Visibility = Visibility.Hidden
			};

			this.Children.Add(_faceText);
			this.Children.Add(_faceBoundsCanvas);
		}
		#endregion

		#region accessors
		/// <summary>
		/// Gets or sets the bounds color.
		/// </summary>
		public Color BoundsColor
		{
			get => _boundsColor;
			set
			{
				_boundsColor = value;

				Path path = _faceBoundsCanvas.Children[1] as Path;
				((SolidColorBrush)path.Stroke).Color = _boundsColor;

				System.Windows.Shapes.Rectangle rectangle = _faceBoundsCanvas.Children[0] as System.Windows.Shapes.Rectangle;
				((SolidColorBrush)rectangle.Fill).Color = Color.FromArgb(_boundsBackOpacity, _boundsColor.R, _boundsColor.G, _boundsColor.B);

				_faceText.Background = new SolidColorBrush(_boundsColor);
			}
		}

		/// <summary>
		/// Gets or sets a value indicating whether the bounds of the detected facial images are displayed.
		/// </summary>
		public bool ShowsFaceBounds { get; set; } = false;

		/// <summary>
		/// Enable drawing Text (use message).
		/// </summary>
		public bool ShowsText
		{
			get
			{
				return _faceText.Visibility == Visibility.Visible;
			}
			set
			{
				_faceText.Visibility = value ? Visibility.Visible : Visibility.Hidden;
			}
		}

		/// <summary>
		/// Text Fore Color
		/// </summary>
		public Color TextForeColor
		{
			get { return ((SolidColorBrush)_faceText.Foreground).Color; }
			set { _faceText.Foreground = new SolidColorBrush(value); }
		}

		/// <summary>
		/// Message to draw when ShowsText is true
		/// </summary>
		public string Text { get; set; } = string.Empty;
		#endregion

		public void Update(FaceData facedata)
		{
			DrawBounds(facedata.FaceBounds);
			DrawText(facedata.FaceBounds);
		}

		private void DrawBounds(id3.Face.Rectangle bounds)
		{
			_faceBoundsCanvas.BeginInit();

			// set canvas position
			double w = bounds.TopRight.X - bounds.TopLeft.X;
			double h = bounds.BottomLeft.Y - bounds.TopLeft.Y;
			Canvas.SetLeft(_faceBoundsCanvas, bounds.TopLeft.X);
			Canvas.SetTop(_faceBoundsCanvas, bounds.TopLeft.Y);

			// draw corners
			Path newPath = _faceBoundsCanvas.Children[1] as Path;
			PathGeometry g = newPath.Data as PathGeometry;

			((SolidColorBrush)newPath.Stroke).Color = BoundsColor;

			// create bounds rect
			g.Clear();
			double radius = 10;
			Rect boundsRect = new Rect(0, 0, w, h);
			g.AddGeometry(new RectangleGeometry(boundsRect, radius, radius));

			newPath.StrokeThickness = w / 40f;

			// update background
			System.Windows.Shapes.Rectangle rectangle = _faceBoundsCanvas.Children[0] as System.Windows.Shapes.Rectangle;
			rectangle.Width = w;
			rectangle.Height = h;
			((SolidColorBrush)rectangle.Fill).Color = Color.FromArgb(_boundsBackOpacity, BoundsColor.R, BoundsColor.G, BoundsColor.B);

			_faceBoundsCanvas.EndInit();
			_faceBoundsCanvas.Visibility = Visibility.Visible;
		}

		private TextBlock DrawText(id3.Face.Rectangle bounds)
		{
			double w = bounds.TopRight.X - bounds.TopLeft.X;

			// update text
			_faceText.Text = Text;
			_faceText.Width = w;
			_faceText.Margin = new Thickness(bounds.TopLeft.X, bounds.BottomLeft.Y + w / 20, 0, 0);
			_faceText.Padding = new Thickness(4, 4, 4, 4);
			_faceText.FontSize = w / 6;
			_faceText.FontFamily = new FontFamily("Arial");
			((SolidColorBrush)_faceText.Background).Color = BoundsColor;
			
			return _faceText;
		}
	}
}
