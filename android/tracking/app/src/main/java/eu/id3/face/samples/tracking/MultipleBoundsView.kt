package eu.id3.face.samples.tracking

import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.util.AttributeSet
import android.view.View
import eu.id3.face.TrackedFaceList
import eu.id3.face.TrackingStatus

/**
 * View class in which we will draw the bounds of a detected object.
 */
class MultipleBoundsView : View {
    private var isCapturing = false
    private var isDrawing = false
    private var trackedFaceList: TrackedFaceList? = null
    private var rect: Rect? = null

    private var rectPaint: Paint? = null
    private var textPaint: Paint? = null
    private val textSize = 70

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
            for (i in 0 until trackedFaceList!!.count) {
                val tf = trackedFaceList!![i]
                if (tf.trackingStatus == TrackingStatus.CONFIRMED) {
                    val bounds = tf.predictedBounds
                    rect?.set(bounds.getTopLeft().x,
                        bounds.getTopLeft().y,
                        bounds.getBottomRight().x,
                        bounds.getBottomRight().y)
                    scaleRect(rect!!)
                    canvas.drawRect(rect!!, rectPaint!!)
                    val textx = rect!!.exactCenterX()
                    val texty = rect!!.exactCenterY() + 0.5f * rect!!.height() + textSize
                    canvas.drawText(tf.id.toString(), textx, texty, textPaint!!)
                }
                tf.close()
            }
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        isDrawing = false
    }

    /**
     * Updates the bounds this view needs.
     */
    fun update(trackedFaceList: TrackedFaceList?, processingWidth: Int, processingHeight: Int) {
        if (!isDrawing) {
            if (trackedFaceList == null) {
                isCapturing = false
            } else {
                isCapturing = true
                this.processingWidth = processingWidth
                this.processingHeight = processingHeight
            }
            this.trackedFaceList = trackedFaceList
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

    fun setPaints() {
        rect = Rect()
        textPaint = Paint()
        textPaint!!.setColor(Color.GREEN)
        textPaint!!.setStyle(Paint.Style.FILL)
        textPaint!!.setTextSize(textSize.toFloat())
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint!!.setColor(Color.GREEN)
        rectPaint!!.setStyle(Paint.Style.STROKE)
        rectPaint!!.setStrokeWidth(3.0f)
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
