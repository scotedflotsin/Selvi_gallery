package com.example.selvigallery.Adapter

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    private var mode = Mode.NONE

    private var startX = 0f
    private var startY = 0f

    private var scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private var minScale = 1f
    private var maxScale = 3f

    private var currentScale = 1f

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = matrix
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                startX = event.x
                startY = event.y
                mode = Mode.DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                savedMatrix.set(matrix)
                mode = Mode.ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG) {
                    val dx = event.x - startX
                    val dy = event.y - startY
                    matrix.set(savedMatrix)
                    matrix.postTranslate(dx, dy)
                    fixTranslation()
                    imageMatrix = matrix
                }
                // Zoom handled by ScaleGestureDetector below
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = Mode.NONE
            }
        }

        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val prevScale = currentScale
            currentScale *= scaleFactor
            currentScale = max(minScale, min(currentScale, maxScale))

            val scaleChange = currentScale / prevScale

            matrix.postScale(scaleChange, scaleChange, detector.focusX, detector.focusY)
            fixTranslation()
            imageMatrix = matrix
            return true
        }
    }

    private fun fixTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]

        val drawable = drawable ?: return

        val imageWidth = drawable.intrinsicWidth * scaleX
        val imageHeight = drawable.intrinsicHeight * scaleY

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        var newTransX = transX
        var newTransY = transY

        // Limit translation so image won't move out of bounds horizontally
        if (imageWidth < viewWidth) {
            newTransX = (viewWidth - imageWidth) / 2f
        } else {
            newTransX = min(0f, max(transX, viewWidth - imageWidth))
        }

        // Limit translation so image won't move out of bounds vertically
        if (imageHeight < viewHeight) {
            newTransY = (viewHeight - imageHeight) / 2f
        } else {
            newTransY = min(0f, max(transY, viewHeight - imageHeight))
        }

        matrix.postTranslate(newTransX - transX, newTransY - transY)
    }
}
