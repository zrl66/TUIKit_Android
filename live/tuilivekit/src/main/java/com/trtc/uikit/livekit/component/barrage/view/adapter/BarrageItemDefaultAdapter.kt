package com.trtc.uikit.livekit.component.barrage.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.trtc.uikit.livekit.R
import com.trtc.uikit.livekit.component.barrage.store.model.DefaultEmojiResource
import com.trtc.uikit.livekit.component.barrage.view.AnchorTagSpan
import com.trtc.uikit.livekit.component.barrage.view.EmojiSpan
import io.trtc.tuikit.atomicxcore.api.barrage.Barrage
import kotlin.math.ceil

class BarrageItemDefaultAdapter(
    private val context: Context,
    private val ownerId: String
) : BarrageItemAdapter {

    private val mLayoutInflater = LayoutInflater.from(context)
    private val mEmojiResource = DefaultEmojiResource()
    private val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    private val userNameColor = ContextCompat.getColor(context, R.color.livekit_barrage_user_name_color)
    private val contentColor = ContextCompat.getColor(context, R.color.livekit_barrage_g8)

    private val anchorTagText = context.getString(R.string.live_barrage_anchor)
    private val anchorTagBackground = ContextCompat.getDrawable(context, R.drawable.livekit_barrage_bg_anchor_flag)!!
    private val anchorTagWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44f, context.resources.displayMetrics).toInt()
    private val anchorTagHeight =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
    private val anchorTagMarginEnd =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()
    private val anchorTagTextSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, context.resources.displayMetrics)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, barrage: Barrage) {
        val viewHolder = holder as ViewHolder
        val fontSize = getFontSize(viewHolder.textMsgContent)

        val userName = barrage.sender.userName.takeIf { it.isNotEmpty() } ?: barrage.sender.userID
        val isOwner = ownerId == barrage.sender.userID

        viewHolder.textMsgContent.text = buildMessageContent(userName, barrage.textContent, fontSize, isOwner)
        viewHolder.textMsgContent.movementMethod = if (isOwner) LinkMovementMethod.getInstance() else null
    }

    private fun buildMessageContent(
        userName: String,
        textContent: String,
        fontSize: Int,
        isOwner: Boolean
    ): SpannableStringBuilder {
        val contentPart = getContentWithEmoji(textContent, fontSize)

        return SpannableStringBuilder().apply {
            if (isRtl) {

                val startIndex = 0

                if (isOwner) {
                    append("\u200B")
                    setSpan(
                        AnchorTagSpan(
                            anchorTagText,
                            contentColor,
                            anchorTagTextSize,
                            anchorTagBackground.constantState!!.newDrawable().mutate(),
                            anchorTagWidth,
                            anchorTagHeight,
                            anchorTagMarginEnd,
                            isRtl = true
                        ),
                        startIndex, startIndex + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                val userNameStart = length
                append("$userName\u200F:")
                val userNameEnd = length
                setSpan(
                    ForegroundColorSpan(userNameColor),
                    userNameStart,
                    userNameEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                append("\u2066")
                val contentStart = length
                append(contentPart)
                val contentEnd = length
                append("\u2069")
                setSpan(
                    ForegroundColorSpan(contentColor),
                    contentStart,
                    contentEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                if (isOwner) {
                    append("\u200B")
                    setSpan(
                        AnchorTagSpan(
                            anchorTagText,
                            contentColor,
                            anchorTagTextSize,
                            anchorTagBackground.constantState!!.newDrawable().mutate(),
                            anchorTagWidth,
                            anchorTagHeight,
                            anchorTagMarginEnd,
                            isRtl = false
                        ),
                        0, 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                val anchorLength = length
                append("$userName: ")
                val userNameEndIndex = length
                setSpan(
                    ForegroundColorSpan(userNameColor),
                    anchorLength,
                    userNameEndIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append(contentPart)
                setSpan(
                    ForegroundColorSpan(contentColor),
                    userNameEndIndex,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = mLayoutInflater.inflate(R.layout.livekit_barrage_item_msg, parent, false)
        return ViewHolder(view)
    }

    fun getFontSize(textView: TextView): Int {
        return Paint().apply {
            textSize = textView.textSize
        }.let { paint ->
            val fm = paint.fontMetrics
            ceil(fm.bottom - fm.top).toInt()
        }
    }

    private fun getContentWithEmoji(textContent: String, fontSize: Int): SpannableStringBuilder {
        return SpannableStringBuilder(textContent).apply {
            processEmojiSpan(this, fontSize)
        }
    }

    private fun processEmojiSpan(sb: SpannableStringBuilder, fontSize: Int) {
        val text = sb.toString()
        var startIndex = 0
        var i = 0
        while (i < text.length) {
            if (text[i] == '[') {
                val endIndex = text.indexOf(']', i)
                if (endIndex != -1) {
                    val emojiKey = text.substring(i, endIndex + 1)
                    mEmojiResource.getResId(emojiKey).takeIf { it != 0 }?.let { resId ->
                        mEmojiResource.getDrawable(context, resId, Rect(0, 0, fontSize, fontSize))
                            .apply { setBounds(0, 0, fontSize, fontSize) }
                            .let { drawable ->
                                sb.setSpan(
                                    EmojiSpan(drawable, 0),
                                    startIndex, endIndex + 1,
                                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                    }
                    startIndex = endIndex + 1
                    i = endIndex
                }
            } else {
                startIndex++
            }
            i++
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMsgContent: TextView = itemView.findViewById(R.id.tv_msg_content)
    }
}
