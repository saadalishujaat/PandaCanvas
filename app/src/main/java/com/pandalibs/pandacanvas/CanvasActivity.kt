package com.pandalibs.pandacanvas

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.pandalibs.pandacanvas.databinding.ActivityCanvasBinding
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver


class CanvasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCanvasBinding
    private val hideHandler = Handler(Looper.getMainLooper())

    private var currentColor: Int = Color.BLACK

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            binding.root.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            binding.root.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    override fun onBackPressed() {

        show()
        super.onBackPressed()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityCanvasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        isFullscreen = true


        binding.colorPicker.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(it, object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        currentColor = color
                        binding.colorPicker.setColorFilter(color)
                        binding.strokeSetter.setColorFilter(color)

                        binding.seekBar.thumbTintList = ColorStateList.valueOf(color)
                        binding.seekBar.progressTintList = ColorStateList.valueOf(color)
                        binding.canvas.setColor(color)
                    }

                    fun onColor(color: Int, fromUser: Boolean) {}
                })
        }

        val handler: Handler = Handler(Looper.getMainLooper())

        binding.canvas.setLineThickness(30f)

        binding.strokeSetter.setOnClickListener {
            binding.canvas.setColor(currentColor)

            if(binding.eraser.isSelected){
                binding.eraser.isSelected = false
            }

            if(binding.seekBar.visibility == View.VISIBLE){
                binding.seekBar.visibility = View.GONE
            }else{
                binding.seekBar.visibility = View.VISIBLE
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(Runnable {
                    if(binding.seekBar.visibility == View.VISIBLE){
                        binding.seekBar.visibility = View.GONE
                    }
                },3000)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.canvas.setLineThickness((progress * 10).toFloat())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    if(binding.seekBar.visibility == View.VISIBLE){
                        binding.seekBar.visibility = View.GONE
                    }
                },3000)
            }

        })


        binding.undo.isEnabled = false
        binding.redo.isEnabled = false

        binding.undo.setOnClickListener {
            binding.canvas.onClickUndo()
            binding.undo.isEnabled = binding.canvas.pathSize() > 0
            binding.redo.isEnabled = binding.canvas.undoPathSize() > 0
        }

        binding.redo.setOnClickListener {
            binding.canvas.onClickRedo()
            binding.undo.isEnabled = binding.canvas.pathSize() > 0
            binding.redo.isEnabled = binding.canvas.undoPathSize() > 0
        }

        binding.canvas.setOnClickListener {
            binding.undo.isEnabled = binding.canvas.pathSize() > 0
            binding.redo.isEnabled = binding.canvas.undoPathSize() > 0

        }

        binding.eraser.setOnClickListener {
            if(!binding.eraser.isSelected){
                binding.eraser.isSelected = true
            }
            binding.canvas.setColor(Color.WHITE)
        }

        binding.clearCanvas.setOnClickListener {
            binding.canvas.clearCanvas()
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(100)
    }


    private fun hide() {
        supportActionBar?.hide()
        isFullscreen = false
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        if (Build.VERSION.SDK_INT >= 30) {
            binding.root.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            binding.root.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        private const val AUTO_HIDE = true
        private const val AUTO_HIDE_DELAY_MILLIS = 10
        private const val UI_ANIMATION_DELAY = 10
    }
}