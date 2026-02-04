package eu.id3.face.samples.trackingjava;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.view.View;

import eu.id3.face.Rectangle;
import eu.id3.face.TrackedFace;
import eu.id3.face.TrackedFaceList;
import eu.id3.face.TrackingStatus;

public class MultipleBoundsView extends View {
    private boolean isCapturing = false;
    private boolean isDrawing = false;
    private TrackedFaceList trackedFaceList = null;
    private Rect rect = null;

    private Paint rectPaint = null;
    private Paint textPaint = null;
    private final int textSize = 70;

    private int processingWidth = 0;
    private int processingHeight = 0;
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public MultipleBoundsView(Context context) {
        super(context);
    }

    public MultipleBoundsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultipleBoundsView(Context context, AttributeSet attrs, int defStyle) {
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
            for(int i = 0 ; i < trackedFaceList.getCount() ; i++) {
                TrackedFace tf = trackedFaceList.get(i);
                if(tf.getTrackingStatus() == TrackingStatus.CONFIRMED) {
                    eu.id3.face.Rectangle bounds = tf.getPredictedBounds();
                    rect.set(bounds.getTopLeft().x,
                            bounds.getTopLeft().y,
                            bounds.getBottomRight().x,
                            bounds.getBottomRight().y);
                    scaleRect(rect);
                    canvas.drawRect(rect, rectPaint);

                    float textx = rect.exactCenterX();
                    float texty = rect.exactCenterY() + 0.5f * rect.height() + textSize;
                    canvas.drawText(String.valueOf(tf.getId()), textx, texty, textPaint);
                }
                tf.close();
            }
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        isDrawing = false;
    }

    /**
     * Updates the bounds this view needs.
     */
    public void update(TrackedFaceList trackedFaceList, int processingWidth, int processingHeight) {
        if (!isDrawing) {
            if (trackedFaceList == null) {
                isCapturing = false;
            } else {
                isCapturing = true;
                this.processingWidth = processingWidth;
                this.processingHeight = processingHeight;
            }
            this.trackedFaceList = trackedFaceList;
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

    public void setPaints() {
        rect = new Rect();
        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3.0f);
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
        if(Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
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
