package com.wheelpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class WheelPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var lastEmitTime = 0L
    private var onValueChanging: ((Int) -> Unit)? = null

    private var items: List<String> = emptyList()
    private var selectedIndex: Int = 0
    private var lastSelectedIndex: Int = 0

    private val itemHeight = 48 * resources.displayMetrics.density
    private val visibleItems = 5
    private val centerY get() = height / 2f

    private var scrollOffset = 0f
    private val scroller = OverScroller(context)
    private var isFling = false

    private var fontFamily: String? = null

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24 * resources.displayMetrics.scaledDensity
        color = Color.parseColor("#1C1C1C")
        textAlign = Paint.Align.CENTER
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24 * resources.displayMetrics.scaledDensity
        color = Color.parseColor("#1C1C1C")
        textAlign = Paint.Align.LEFT
    }

    private val selectionPaint = Paint().apply {
        color = Color.parseColor("#F7F9FF")
        style = Paint.Style.FILL
    }

    private var unit: String? = null
    private var onValueChanged: ((Int) -> Unit)? = null

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (!scroller.isFinished) {
                scroller.abortAnimation()
                isFling = false
            }
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scrollOffset += distanceY
            clampScrollOffset()
            checkAndTriggerHaptic()
            emitValueChanging()
            invalidate()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            isFling = true
            scroller.fling(
                0, scrollOffset.toInt(),
                0, -velocityY.toInt(),
                0, 0,
                Int.MIN_VALUE, Int.MAX_VALUE
            )
            postInvalidateOnAnimation()
            return true
        }
    })

    private fun loadTypeface(fontName: String): Typeface? {
        return try {
            val otfPath = "fonts/$fontName.otf"
            val ttfPath = "fonts/$fontName.ttf"
            try {
                Typeface.createFromAsset(context.assets, otfPath)
            } catch (e: Exception) {
                Typeface.createFromAsset(context.assets, ttfPath)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun applyTypeface() {
        val typeface = fontFamily?.let { loadTypeface(it) }
        textPaint.typeface = typeface
        unitPaint.typeface = typeface
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (itemHeight * visibleItems).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cornerRadius = 16 * resources.displayMetrics.density
        val selectionTop = centerY - itemHeight / 2
        val selectionRect = RectF(
            28 * resources.displayMetrics.density,
            selectionTop,
            width - 28 * resources.displayMetrics.density,
            selectionTop + itemHeight
        )
        canvas.drawRoundRect(selectionRect, cornerRadius, cornerRadius, selectionPaint)

        val centerIndex = (scrollOffset / itemHeight).roundToInt()
        val startIndex = max(0, centerIndex - visibleItems)
        val endIndex = min(items.size - 1, centerIndex + visibleItems)

        for (i in startIndex..endIndex) {
            val itemCenterY = centerY - scrollOffset + i * itemHeight
            val distanceFromCenter = abs(itemCenterY - centerY) / itemHeight

            val opacity = when {
                distanceFromCenter < 0.5f -> 1f
                distanceFromCenter < 1.5f -> 0.4f
                else -> 0.2f
            }
            val scale = 1f - (distanceFromCenter * 0.05f).coerceIn(0f, 0.1f)

            textPaint.alpha = (opacity * 255).toInt()
            unitPaint.alpha = (opacity * 255).toInt()

            val textY = itemCenterY + (textPaint.textSize / 3)

            canvas.save()
            canvas.scale(scale, scale, width / 2f, itemCenterY)

            val valueX = if (unit != null) width / 2f - 20 * resources.displayMetrics.density else width / 2f
            canvas.drawText(items[i], valueX, textY, textPaint)

            unit?.let {
                val unitX = width / 2f + 8 * resources.displayMetrics.density
                canvas.drawText(it, unitX, textY, unitPaint)
            }

            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (!isFling) {
                snapToNearestItem()
            }
        }

        return handled || super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffset = scroller.currY.toFloat()
            clampScrollOffset()
            checkAndTriggerHaptic()
            emitValueChanging()
            postInvalidateOnAnimation()

            if (scroller.isFinished) {
                isFling = false
                snapToNearestItem()
            }
        }
    }

    private fun clampScrollOffset() {
        val maxScroll = (items.size - 1) * itemHeight
        scrollOffset = scrollOffset.coerceIn(0f, maxScroll)
    }

    private fun checkAndTriggerHaptic() {
        if (items.isEmpty()) return
        val currentIndex = (scrollOffset / itemHeight).roundToInt().coerceIn(0, items.size - 1)
        if (currentIndex != lastSelectedIndex) {
            lastSelectedIndex = currentIndex
            triggerHaptic()
        }
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    private fun snapToNearestItem() {
        if (items.isEmpty()) return
        val targetIndex = (scrollOffset / itemHeight).roundToInt().coerceIn(0, items.size - 1)
        val targetOffset = targetIndex * itemHeight

        scroller.startScroll(
            0, scrollOffset.toInt(),
            0, (targetOffset - scrollOffset).toInt(),
            200
        )

        if (targetIndex != selectedIndex) {
            selectedIndex = targetIndex
            onValueChanged?.invoke(selectedIndex)
            onValueChanging?.invoke(targetIndex)
        }

        invalidate()
    }

    private fun emitValueChanging() {
        if (items.isEmpty()) return
        val currentIndex = (scrollOffset / itemHeight).roundToInt().coerceIn(0, items.size - 1)

        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= 50) {           // 50ms 限频
            lastEmitTime = now
            onValueChanging?.invoke(currentIndex)
        }
    }

    fun setItems(newItems: List<String>) {
        items = newItems
        invalidate()
    }

    fun setUnit(newUnit: String?) {
        unit = newUnit
        invalidate()
    }

    fun setSelectedIndex(index: Int) {
        if (index in items.indices) {
            selectedIndex = index
            lastSelectedIndex = index
            scrollOffset = index * itemHeight
            invalidate()
        }
    }

    fun setOnValueChangedListener(listener: (Int) -> Unit) {
        onValueChanged = listener
    }

    fun setOnValueChangingListener(listener: (Int) -> Unit) {
        onValueChanging = listener
    }

    fun setFontFamily(family: String?) {
        fontFamily = family
        applyTypeface()
        invalidate()
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        unitPaint.color = color
        invalidate()
    }

    fun setSelectionBackgroundColor(color: Int) {
        selectionPaint.color = color
        invalidate()
    }
}
