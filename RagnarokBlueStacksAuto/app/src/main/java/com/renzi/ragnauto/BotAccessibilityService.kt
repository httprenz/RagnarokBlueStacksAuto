package com.renzi.ragnauto

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import java.util.concurrent.Executor

class BotAccessibilityService : AccessibilityService() {

    companion object {
        var instance: BotAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var overlay: LinearLayout? = null
    private var lastActionAt = 0L

    // Relative points derived from the user's BlueStacks screenshots.
    private val questX = 0.125
    private val questY = 0.355
    private val autoX = 0.925
    private val autoY = 0.525
    private val potionHpX = 0.797
    private val potionHpY = 0.435
    private val potionSpX = 0.840
    private val potionSpY = 0.435
    private val skipX = 0.895
    private val skipY = 0.115
    private val dialogueNextX = 0.50
    private val dialogueNextY = 0.92

    private val tapToSkipTemplate by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.tap_to_skip)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        handler.post(botLoop)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        hideControlOverlay()
        instance = null
        super.onDestroy()
    }

    private val botLoop = object : Runnable {
        override fun run() {
            if (BotState.running && !BotState.paused) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    takeScreenshot(
                        android.view.Display.DEFAULT_DISPLAY,
                        mainExecutorCompat(),
                        object : TakeScreenshotCallback {
                            override fun onSuccess(result: ScreenshotResult) {
                                val hw = result.hardwareBuffer
                                val cs = result.colorSpace
                                val wrapped = Bitmap.wrapHardwareBuffer(hw, cs)
                                val bmp = wrapped?.copy(Bitmap.Config.ARGB_8888, false)
                                hw.close()
                                if (bmp != null) {
                                    processFrame(bmp)
                                    bmp.recycle()
                                }
                            }
                            override fun onFailure(errorCode: Int) {
                                // Coordinate-only fallback.
                                coordinateFallback()
                            }
                        }
                    )
                } else {
                    coordinateFallback()
                }
            }
            handler.postDelayed(this, 1200)
        }
    }

    private fun mainExecutorCompat(): Executor = Executor { command -> handler.post(command) }

    private fun processFrame(screen: Bitmap) {
        val now = System.currentTimeMillis()
        if (now - lastActionAt < 900) return

        // 1) Cutscene/dialog detection: compare top-right ROI against the user's "Tap to Skip".
        if (BotState.skipCutscenes) {
            val roi = ScreenUtil.cropRelative(screen, 0.82, 0.07, 0.97, 0.16)
            val resized = Bitmap.createScaledBitmap(tapToSkipTemplate, roi.width, roi.height, true)
            val sim = ScreenUtil.similarity(roi, resized)
            resized.recycle()
            roi.recycle()

            if (sim > 0.72) {
                tapRelative(skipX, skipY)
                handler.postDelayed({ tapRelative(dialogueNextX, dialogueNextY) }, 900)
                lastActionAt = now
                return
            }
        }

        // 2) HP/SP sampling from the player's bars at top-left.
        val hp = ScreenUtil.barFillPercent(screen, 0.070, 0.128, 0.165, 0.154, "hp")
        val sp = ScreenUtil.barFillPercent(screen, 0.070, 0.155, 0.165, 0.180, "sp")

        if (hp in 1 until BotState.hpThreshold) {
            tapRelative(potionHpX, potionHpY)
            lastActionAt = now
            return
        }
        if (sp in 1 until BotState.spThreshold) {
            tapRelative(potionSpX, potionSpY)
            lastActionAt = now
            return
        }

        // 3) Basic quest/combat heartbeat.
        // The game itself handles pathing/combat after these UI actions.
        if (BotState.autoCombat) {
            tapRelative(autoX, autoY)
            lastActionAt = now
            return
        }

        if (BotState.autoQuest) {
            tapRelative(questX, questY)
            lastActionAt = now
        }
    }

    private fun coordinateFallback() {
        if (!BotState.running || BotState.paused) return
        // On Android versions without AccessibilityService screenshots, keep a conservative loop.
        if (BotState.autoQuest) {
            tapRelative(questX, questY)
            handler.postDelayed({
                if (BotState.running && BotState.autoCombat) tapRelative(autoX, autoY)
            }, 3500)
        } else if (BotState.autoCombat) {
            tapRelative(autoX, autoY)
        }
    }

    private fun tapRelative(rx: Double, ry: Double) {
        val dm = resources.displayMetrics
        val x = (dm.widthPixels * rx).toFloat()
        val y = (dm.heightPixels * ry).toFloat()
        val p = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(p, 0, 70)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun showControlOverlay() {
        if (overlay != null) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.argb(210, 13, 27, 42))
        }

        val status = TextView(this).apply {
            text = "Ragna Auto"
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(8, 0, 12, 0)
        }

        val pause = Button(this).apply {
            text = "Pause"
            setOnClickListener {
                BotState.paused = !BotState.paused
                text = if (BotState.paused) "Resume" else "Pause"
            }
        }

        val stop = Button(this).apply {
            text = "Stop"
            setOnClickListener {
                BotState.running = false
                BotState.paused = false
                hideControlOverlay()
            }
        }

        panel.addView(status)
        panel.addView(pause)
        panel.addView(stop)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 100
        }

        wm.addView(panel, params)
        overlay = panel
    }

    fun hideControlOverlay() {
        val panel = overlay ?: return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        runCatching { wm.removeView(panel) }
        overlay = null
    }
}
