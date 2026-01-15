package com.trtc.uikit.livekit.component.barrage.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan

class AnchorTagSpan(
    private val text: String,
    private val textColor: Int,
    private val textSize: Float,
    private val background: Drawable,
    private val tagWidth: Int,
    private val tagHeight: Int,
    private val margin: Int,
    private val isRtl: Boolean = false
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        fm?.run {
            val pfm = paint.fontMetricsInt
            val centerOffset = (tagHeight - (pfm.descent - pfm.ascent)) / 2
            ascent = pfm.ascent - centerOffset
            top = pfm.top - centerOffset
            bottom = pfm.descent + centerOffset
            descent = pfm.descent + centerOffset
        }
        return tagWidth + margin
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val pfm = paint.fontMetricsInt
        val fontCenter = y + (pfm.descent + pfm.ascent) / 2
        val tagTop = fontCenter - tagHeight / 2

        val tagX = if (isRtl) x + margin else x

        background.setBounds(tagX.toInt(), tagTop, tagX.toInt() + tagWidth, tagTop + tagHeight)
        background.draw(canvas)

        val originalTextSize = paint.textSize
        val originalColor = paint.color

        paint.textSize = textSize
        paint.color = textColor
        paint.isFakeBoldText = true

        val textWidth = paint.measureText(this.text.uppercase())
        val textX = tagX + (tagWidth - textWidth) / 2
        val textY = tagTop + tagHeight / 2 - (paint.descent() + paint.ascent()) / 2

        canvas.drawText(this.text.uppercase(), textX, textY, paint)

        paint.textSize = originalTextSize
        paint.color = originalColor
        paint.isFakeBoldText = false
    }
}
