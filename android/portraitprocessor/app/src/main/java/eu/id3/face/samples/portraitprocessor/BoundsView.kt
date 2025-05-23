package eu.id3.face.samples.portraitprocessor

import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.util.AttributeSet
import android.view.View

/**
 * View class in which we will draw the bounds of a detected object.
 */
class BoundsView : View {
    private var isCapturing = false
    private var isDrawing = false
    private var bounds: Rect? = null
    private var boundsPaint: Paint? = null

    private var processingWidth: Int = 0
    private var processingHeight: Int = 0
    private var ratioWidth = 0
    private var ratioHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * This function is called every time the bounds to draw are modified.
     * If the capture is ongoing and an object of interest is detected, we draw the detected bounds
     * in the view. Otherwise, we erase the previous bounds.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isCapturing) {
            scaleRect(bounds!!)
            canvas.drawRect(bounds!!, boundsPaint!!)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        isDrawing = false
    }

    /**
     * Updates the bounds this view needs.
     */
    fun update(bounds: Rect?, processingWidth: Int, processingHeight: Int) {
        if (!isDrawing) {
            if (bounds == null) {
                isCapturing = false
            } else {
                isCapturing = true
                this.processingWidth = processingWidth
                this.processingHeight = processingHeight
            }
            this.bounds = bounds
            isDrawing = true
            postInvalidate()
        }
    }

    /**
     * Updates the ratio of the view in order to draw the bounds correctly.
     */
    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    fun setPaint(paint: Paint) {
        boundsPaint = paint
    }

    /**
     * Gets the right measure of the view in order to draw the detected bounds correctly.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }
    }

    /**
     * The camera in "selfie" mode acts like a mirror in the preview, but the real output
     * is flipped compared to this preview so we need to mirror coordinates.
     */
    private fun scaleRect(rect: Rect) {
        if (Parameters.cameraType == CameraCharacteristics.LENS_FACING_FRONT) {
            rect.left = this.measuredWidth - rect.left * this.measuredWidth / processingWidth
            rect.right = this.measuredWidth - rect.right * this.measuredWidth / processingWidth
        } else {
            rect.left = rect.left * this.measuredWidth / processingWidth
            rect.right = rect.right * this.measuredWidth / processingWidth
        }
        rect.top = rect.top * this.measuredHeight / processingHeight
        rect.bottom = rect.bottom * this.measuredHeight / processingHeight
    }
}
