package com.pandalibs.pandacanvaslib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import java.util.*
import kotlin.math.abs

class CanvasPad(context: Context?, attr: AttributeSet?) : View(context, attr) {
    private var pandaCanvas: Canvas
    private var pandaPath: Path
    private val _paint: Paint
    private var pointerId = 0
    private var thickness = 0f
    private var shadowRadius = 0f
    private var shadowX = 0f
    private var shadowY = 0f
    private var _mX = 0f
    private var _mY = 0f
    private val paths = ArrayList<CanvasPojo>()
    private val newPaths = ArrayList<CanvasPojo>()
    private val undonePaths = ArrayList<CanvasPojo>()
    private val touchTolerance = 4f
    private val lineThickness = 5f

    //for weird pen
    private var mVelocityTracker: VelocityTracker? = null
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (paths.size > 0) {
            for (i in paths.indices) {
                _paint.color = paths[i].paintColor
                _paint.strokeWidth = paths[i].paintWidth
                _paint.setShadowLayer(
                    paths[i].shadowRadius,
                    paths[i].shadowX,
                    paths[i].shadowY,
                    Color.LTGRAY
                )
                paths[i].getPath()?.let {
                    canvas.drawPath(paths[i].getPath()!!, _paint)
                }
            }
        }
        canvas.drawPath(pandaPath, _paint)
    }

    private fun TouchStart(x: Float, y: Float) {
        pandaPath.reset()
        pandaPath.moveTo(x, y)
        _mX = x
        _mY = y
    }

    fun setLineThickness(lineThickness: Float) {
        thickness = lineThickness
        _paint.strokeWidth = lineThickness
        val cpath = CanvasPojo(_paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
        paths.add(cpath)
    }

    fun setShadowLayer(on: Boolean) {
        if (on) {
            shadowRadius = 8f
            shadowX = 8f
            shadowY = 8f
            _paint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.LTGRAY)
            val cpath = CanvasPojo(_paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
            paths.add(cpath)
        } else {
            shadowRadius = 0f
            shadowX = 0f
            shadowY = 0f
            _paint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.LTGRAY)
            val cpath = CanvasPojo(_paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
            paths.add(cpath)
        }
    }

    fun setAlpha(alpha: Int) {
        _paint.alpha = alpha
        val cpath = CanvasPojo(_paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
        paths.add(cpath)
    }

    fun setColor(color: Int) {
        _paint.color = color
        val cpath = CanvasPojo(_paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
        paths.add(cpath)
    }

    private fun touchMove(x: Float, y: Float, pointerId: Int) {
        val dx = abs(x - _mX)
        val dy = abs(y - _mY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            pandaPath.quadTo(_mX, _mY, (x + _mX) / 2, (y + _mY) / 2)
            _mX = x
            _mY = y
        }
    }

    private fun touchUp() {
        if (!pandaPath.isEmpty) {
            pandaPath.lineTo(_mX, _mY)
            val cpath =
                CanvasPojo(pandaPath, _paint.color, _paint.strokeWidth, shadowRadius, shadowX, shadowY)
            paths.add(cpath)
            newPaths.add(paths[paths.size - 1])
            undonePaths.clear()
            pandaPath = Path()
        } else {
            pandaCanvas.drawPoint(_mX, _mY, _paint)
        }
        pandaPath.reset()
    }

    private var isDrawn = false

    override fun onTouchEvent(e: MotionEvent): Boolean {
        super.onTouchEvent(e)
        val index = e.actionIndex
        val action = e.actionMasked
        pointerId = e.getPointerId(index)
        val x = e.x
        val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the
                    // velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain()
                } else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker!!.clear()
                }
                // Add a user's movement to the tracker.
                mVelocityTracker!!.addMovement(e)
                TouchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker!!.addMovement(e)
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker!!.computeCurrentVelocity(1000)
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                touchMove(x, y, pointerId)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        isDrawn = true
        return true
    }

    fun clearCanvas() {
        pandaPath.reset()
        pandaCanvas = Canvas()
        paths.clear()
        newPaths.clear()
        invalidate()
    }

    fun setEraser() {
        setColor(Color.WHITE)
        invalidate()
    }

    fun setHighlighter() {
        _paint.strokeJoin = Paint.Join.BEVEL
        _paint.strokeCap = Paint.Cap.BUTT
        invalidate()
    }

    fun setNormal() {
        _paint.strokeJoin = Paint.Join.ROUND
        _paint.strokeCap = Paint.Cap.ROUND
        invalidate()
    }

    fun setTransparent() {
        pandaCanvas.drawColor(Color.TRANSPARENT)
        invalidate()
    }

/*    @get:SuppressLint("WrongThread")
    val bytes: ByteArray
        get() {
            val b = bitmap
            val baos = ByteArrayOutputStream()
            b.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return baos.toByteArray()
        }
    val bitmap: Bitmap
        get() {
            val v = this.parent as View
            val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(v.left, v.top, v.right, v.bottom)
            v.draw(c)
            return b
        }*/

    fun onClickUndo() {
        pandaPath.reset()
        if (paths.size > 0 && newPaths.size > 0) {
//            undonePaths.add(paths.remove(paths.size() - 1));
            paths.remove(newPaths[newPaths.size - 1])
            undonePaths.add(newPaths[newPaths.size - 1])
            newPaths.remove(newPaths[newPaths.size - 1])
            Log.d("PATH_TAG", "onClickUndo: " + paths.size)
            Log.i("PATH_TAG", "UNDONE: " + undonePaths.size)
            invalidate()
        }

    }

    fun onClickRedo() {
        if (undonePaths.size > 0) {
            newPaths.add(undonePaths[undonePaths.size - 1])
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    fun pathSize(): Int {
        return newPaths.size
    }

    fun undoPathSize(): Int {
        return undonePaths.size
    }

    init {
        pandaPath = Path()
        _paint = Paint()
        _paint.isAntiAlias = true
        _paint.isDither = true
        _paint.color = Color.BLACK
        _paint.style = Paint.Style.STROKE
        _paint.strokeJoin = Paint.Join.ROUND
        _paint.strokeCap = Paint.Cap.ROUND
        _paint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.LTGRAY)
        _paint.strokeWidth = lineThickness
        pandaCanvas = Canvas()
        isDrawn = false
        setWillNotDraw(false)
    }
}