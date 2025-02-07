package eu.id3.face.samples.portraitprocessorjava;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.view.View;

public class BoundsView extends View {
    private boolean isCapturing = false;
    private boolean isDrawing = false;
    private Rect bounds = null;
    private Paint boundsPaint = null;

    private int processingWidth = 0;
    private int processingHeight = 0;
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public BoundsView(Context context) {
        super(context);
    }

    public BoundsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoundsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * This function is called every time the bounds to draw are modified.
     * If the capture is ongoing and an object of interest is detected, we draw the detected bounds
     * in the view. Otherwise, we erase the previous bounds.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isCapturing) {
            scaleRect(bounds);
            canvas.drawRect(bounds, boundsPaint);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        isDrawing = false;
    }

    /**
     * Updates the bounds this view needs.
     */
    public void update(Rect bounds, int processingWidth, int processingHeight) {
        if (!isDrawing) {
            if (bounds == null) {
                isCapturing = false;
            } else {
                isCapturing = true;
                this.processingWidth = processingWidth;
                this.processingHeight = processingHeight;
            }
            this.bounds = bounds;
            isDrawing = true;
            postInvalidate();
        }
    }

    /**
     * Updates the ratio of the view in order to draw the bounds correctly.
     */
    public void setAspectRatio(int width, int height) {
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    public void setPaint(Paint paint) {
        boundsPaint = paint;
    }

    /**
     * Gets the right measure of the view in order to draw the detected bounds correctly.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
    }

    /**
     * The camera in "selfie" mode acts like a mirror in the preview, but the real output
     * is flipped compared to this preview so we need to mirror coordinates.
     */
    private void scaleRect(Rect rect) {
        if (eu.id3.face.samples.portraitprocessorjava.Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
            rect.left = this.getMeasuredWidth() - rect.left * this.getMeasuredWidth() / processingWidth;
            rect.right = this.getMeasuredWidth() - rect.right * this.getMeasuredWidth() / processingWidth;
        } else {
            rect.left = rect.left * this.getMeasuredWidth() / processingWidth;
            rect.right = rect.right * this.getMeasuredWidth() / processingWidth;
        }
        rect.top = rect.top * this.getMeasuredHeight() / processingHeight;
        rect.bottom = rect.bottom * this.getMeasuredHeight() / processingHeight;
    }
}
