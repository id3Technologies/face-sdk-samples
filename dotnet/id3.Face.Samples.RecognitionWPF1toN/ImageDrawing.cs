using System;
using System.Collections.Generic;
using System.Linq;

namespace id3.Face.Sample.WPF
{
    using System.Runtime.InteropServices;
    using System.Windows;
	using System.Windows.Media;
	using System.Windows.Media.Imaging;
    
    public static class ImageDrawing
	{
        [StructLayout(LayoutKind.Sequential)]
        private struct BGR
        {
            public byte b;
            public byte g;
            public byte r;
        }

        private static void FastConvertGrayscale16bits(id3.Face.Image source, ref WriteableBitmap bmp)
        {
            // Simple GrayScale 16 bits to RGB conversion
            int width = source.Width;
            int height = source.Height;
            var rect = new Int32Rect(0, 0, width, height);
            if (bmp == null)
                bmp = new WriteableBitmap(width, height, 96, 96, PixelFormats.Gray8, null);
            unsafe
            {
                UInt16* pixels = (UInt16*)source.GetPixels();
                byte* newPixel = (byte*)bmp.BackBuffer;
                int offset = bmp.BackBufferStride - (width * 1);
                for (int y = 0; y < height; y++)
                {
                    for (int x = 0; x < width; x++)
                    {
                        UInt16 value = *pixels++;
                        if (value > 2500)
                            *newPixel = 255;
                        else if (value > 1000)
                            *newPixel = (byte)(192 + (63 * (value - 1000)) / 1500);
                        else
                            *newPixel = (byte)((192 * value) / 1000);
                        newPixel++;
                    }
                    newPixel += offset;
                }
            }
            bmp.Lock();
            bmp.AddDirtyRect(rect); // update
            bmp.Unlock();
        }

        public static BitmapSource ToBitmapSource(id3.Face.Image image)
		{
			int w = image.Width;
			int h = image.Height;
			int bufferSize = image.Height * image.Stride;

			WriteableBitmap wbm = new WriteableBitmap(w, h, 96, 96, PixelFormats.Bgr24, null);
			wbm.WritePixels(new Int32Rect(0, 0, w, h), image.GetPixels(), bufferSize, image.Stride);

			return wbm;
		}

		public static BitmapSource ToBitmapSource(id3.Face.Image image, ref WriteableBitmap wbm)
		{
            if (image.PixelDepth == 1)
            {
                FastConvertGrayscale16bits(image, ref wbm);
            }
            else
            {
                int w = image.Width;
                int h = image.Height;
                int bufferSize = image.Height * image.Stride;

                if (wbm == null)
                    wbm = new WriteableBitmap(w, h, 96, 96, PixelFormats.Bgr24, null);

                if ((h <= wbm.Height) && (w <= wbm.Width))
                {
                    wbm.WritePixels(new Int32Rect(0, 0, w, h), image.GetPixels(), bufferSize, image.Stride);
                }
            }
            return wbm;
        }
    }
}
