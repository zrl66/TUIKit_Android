package com.trtc.uikit.livekit.component.barrage.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.core.graphics.withSave

class EmojiSpan @JvmOverloads constructor(
    drawable: Drawable,
    private val emojiTranslateY: Int = 0
) : ImageSpan(drawable) {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ) = drawable.bounds.right.also {
        fm?.run {
            val pfm = paint.fontMetricsInt
            val drHeight = drawable.bounds.height()
            val fontHeight = pfm.descent - pfm.ascent
            val centerOffset = (drHeight - fontHeight) / 2
            ascent = pfm.ascent - centerOffset
            top = pfm.top - centerOffset
            bottom = pfm.descent + centerOffset
            descent = pfm.descent + centerOffset
        }
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
        canvas.withSave {
            val pfm = paint.fontMetricsInt
            val fontCenter = y + (pfm.descent + pfm.ascent) / 2
            val translateY = fontCenter - drawable.bounds.height() / 2 - emojiTranslateY
            translate(x, translateY.toFloat())
            drawable.draw(this)
        }
    }
}
