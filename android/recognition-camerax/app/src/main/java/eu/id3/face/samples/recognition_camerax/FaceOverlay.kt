package eu.id3.face.samples.recognition_camerax

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector

/**
 * This class is a transparent overlay which will draw Facebounds over the camera preview.
 */

class FaceOverlay : View {
    private val drawLock = Object()
    private var srcWidth = 0
    private var srcHeight = 0
    private var cameraType = CameraSelector.DEFAULT_FRONT_CAMERA
    private var isCapturing = false
    private var faceBounds: Rect = Rect(0, 0, 0, 0)
    private var boundsPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GREEN
        it.strokeWidth = 3.0f
        it.style = Paint.Style.STROKE
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setFaceBounds(
        bounds: eu.id3.face.Rectangle?,
        imageWidth: Int,
        imageHeight: Int,
        cameraType: CameraSelector
    ) {
        /**
         * Drawing and update of the face bounds are synchronized to avoid concurrent access
         */
        synchronized(drawLock) {
            this.srcWidth = imageWidth
            this.srcHeight = imageHeight
            this.cameraType = cameraType
            if (bounds != null) {
                // Converting id3Face.Rectangle to android Rect
                this.faceBounds = Rect(
                    bounds.getTopLeft().x,
                    bounds.getTopLeft().y,
                    bounds.getBottomRight().x,
                    bounds.getBottomRight().y
                )
                isCapturing = true
            } else {
                this.faceBounds = Rect(0, 0, 0, 0)
                isCapturing = false;
            }
        }
        // Request redrawing of the view
        postInvalidate()
    }

    /**
     * This function is called every time the bounds to draw are modified.
     * If the capture is ongoing and an object of interest is detected, we draw the detected bounds
     * in the view. Otherwise, we erase the previous bounds.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(drawLock) {
            if (isCapturing) {
                // adapt face bounds to the preview size
                scaleRect(faceBounds)
                canvas.drawRect(faceBounds, boundsPaint)
            } else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
        }
    }

    /**
     * Adapt the face bounds to the preview view
     *
     * The face bounds have been detected on a fixed resolution given by the ImageAnalyser settings
     * The camera preview size is not fixed as it can be high resolution, of a different aspect ratio, etc
     *
     * We have to do three things:
     *  - rescale to fit the new resolution
     *  - horizontally and/or vertically offset to mitigate aspect ratio differences
     *  - if using frontal camera, flip the bounds as the preview is flipped
     */
    private fun scaleRect(rect: Rect) {
        val viewAspectRatio = this.width.toFloat() / this.height
        val imageAspectRatio = this.srcWidth.toFloat() / this.srcHeight
        var widthOffset = 0
        var heightOffset = 0
        val scaleFactor: Float

        if (viewAspectRatio > imageAspectRatio) {
            // need to vertically crop
            scaleFactor = this.width.toFloat() / srcWidth
            heightOffset =
                ((this.width / imageAspectRatio - this.height) / 2).toInt()
        } else {
            // need to vertically crop
            scaleFactor = this.height.toFloat() / srcHeight
            widthOffset =
                ((this.height / imageAspectRatio - this.width) / 2).toInt()
        }

        rect.left = (rect.left * scaleFactor).toInt()
        rect.right = (rect.right * scaleFactor).toInt()
        rect.top = (rect.top * scaleFactor).toInt()
        rect.bottom = (rect.bottom * scaleFactor).toInt()

        rect.offset(-widthOffset, -heightOffset)

        if (cameraType == CameraSelector.DEFAULT_FRONT_CAMERA) {
            rect.left = this.width - rect.left
            rect.right = this.width - rect.right
        }
    }

}