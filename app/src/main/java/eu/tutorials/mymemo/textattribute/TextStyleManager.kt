package eu.tutorials.mymemo.textattribute

import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import eu.tutorials.mymemo.R

class TextStyleManager(
    private val editContent: EditText,
    private val activity: Activity,
    private val textDialog: Dialog
) {
    // textStyle을 변경하기 위해 띄워주는 Dialog
    fun showTextStyleChooserDialog(linearLayoutStyleButtons: LinearLayout) {
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        val (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }

        val isBoldFullyApplied = isStyleSpanApplied(Typeface.BOLD, start, end)
        val isItalicFullyApplied = isStyleSpanApplied(Typeface.ITALIC, start, end)
        val isUnderlineFullyApplied = isCharacterStyleApplied(UnderlineSpan::class.java, start, end)
        val isStrikethroughFullyApplied =
            isCharacterStyleApplied(StrikethroughSpan::class.java, start, end)

        // 각 스타일에 맞는 버튼의 상태를 설정
        updateButtonState(linearLayoutStyleButtons, "bold", isBoldFullyApplied)
        updateButtonState(linearLayoutStyleButtons, "italic", isItalicFullyApplied)
        updateButtonState(linearLayoutStyleButtons, "underline", isUnderlineFullyApplied)
        updateButtonState(linearLayoutStyleButtons, "strikethrough", isStrikethroughFullyApplied)

        textDialog.setTitle("Text Style")
        textDialog.show()
    }

    private fun isStyleSpanApplied(style: Int, start: Int, end: Int): Boolean {
        val spans = editContent.text?.getSpans(start, end, StyleSpan::class.java)

        if (spans.isNullOrEmpty()) return false

        return spans.all { span ->
            val spanStart = editContent.text?.getSpanStart(span) ?: -1
            val spanEnd = editContent.text?.getSpanEnd(span) ?: -1
            span.style == style && spanStart <= start && spanEnd >= end
        }
    }

    private fun isCharacterStyleApplied(
        styleClass: Class<out CharacterStyle>,
        start: Int,
        end: Int
    ): Boolean {
        val spans = editContent.text?.getSpans(start, end, styleClass)

        if (spans.isNullOrEmpty()) return false

        return spans.all { span ->
            val spanStart = editContent.text?.getSpanStart(span) ?: -1
            val spanEnd = editContent.text?.getSpanEnd(span) ?: -1
            spanStart <= start && spanEnd >= end
        }
    }

    // 각 스타일에 따라 버튼 상태를 갱신하는 함수
    private fun updateButtonState(
        linearLayout: LinearLayout,
        styleTag: String,
        isApplied: Boolean
    ) {
        for (i in 0 until linearLayout.childCount) {
            val button = linearLayout.getChildAt(i) as ImageButton
            if (button.tag == styleTag) {
                val drawableId =
                    if (isApplied) R.drawable.pallet_pressed else R.drawable.pallet_normal
                button.setImageDrawable(ContextCompat.getDrawable(activity, drawableId))
                break
            }
        }
    }

    // dialog에서 각 이미지의 view 설정
    fun updateStyleImage(button: ImageButton, isStyleApplied: Boolean) {
        if (isStyleApplied) {
            button.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.pallet_pressed))
        } else {
            button.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.pallet_normal))
        }
    }

    // styleSpan에 따라 변경
    fun updateStyleSpan(
        style: Int,
        isApplied: Boolean
    ) {
        val spannable = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        val (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }
        val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        existingSpans.forEach {
            if (it.style == style) {
                val spanStart = spannable.getSpanStart(it)
                val spanEnd = spannable.getSpanEnd(it)
                if (spanStart < start) {
                    // 범위 앞부분에만 스팬 적용
                    spannable.setSpan(
                        StyleSpan(style),
                        spanStart,
                        start,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }

                if (spanEnd > end) {
                    // 범위 뒷부분에만 스팬 적용
                    spannable.setSpan(
                        StyleSpan(style),
                        end,
                        spanEnd,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                spannable.removeSpan(it)
            }
        }

        if (isApplied) {
            spannable.setSpan(StyleSpan(style), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        editContent.setText(spannable)
        editContent.setSelection(start, end)
    }

    // CharacterStyle에 따라 변경
    fun <T : CharacterStyle> updateSpan(
        span: T,
        isApplied: Boolean
    ) {
        val spannable = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        val (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }
        val existingSpans = spannable.getSpans(start, end, span::class.java)
        existingSpans.forEach {
            val spanStart = spannable.getSpanStart(it)
            val spanEnd = spannable.getSpanEnd(it)
            if (spanStart < start) {
                // 범위 앞부분에만 스팬 적용
                spannable.setSpan(span, spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            if (spanEnd > end) {
                // 범위 뒷부분에만 스팬 적용
                spannable.setSpan(span, end, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            spannable.removeSpan(it)
        }
        if (isApplied) {
            spannable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        editContent.setText(spannable)
        editContent.setSelection(start, end)
    }
}