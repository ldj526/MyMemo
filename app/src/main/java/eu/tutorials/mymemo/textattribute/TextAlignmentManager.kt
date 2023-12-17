package eu.tutorials.mymemo.textattribute

import android.app.Activity
import android.app.Dialog
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import eu.tutorials.mymemo.R

class TextAlignmentManager(
    private val editContent: EditText,
    private var currentAlignButton: ImageButton,
    private val activity: Activity,
    private var textAlignDialog: Dialog
) {
    // 정렬을 위한 Dialog 띄우기
    fun showTextAlignChooserDialog(linearLayoutTextAlign: LinearLayout) {
        // 적용된 정렬을 가져와 image에 표시해주기 위함
        val spannableString = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        val (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }

        // Align은 선택된 줄 전체가 정렬되어야 하므로 줄의 처음과 끝을 설정해준다.
        val layout = editContent.layout
        val lineStart = layout.getLineForOffset(start)
        val lineEnd = layout.getLineForOffset(end)

        // 선택된 모든 줄에 대해 일관된 정렬이 있는지 확인
        var consistentAlignment: Layout.Alignment? = null
        var isConsistent = true
        for (i in lineStart..lineEnd) {
            val alignmentSpan =
                spannableString.getSpans(layout.getLineStart(i), layout.getLineEnd(i), AlignmentSpan::class.java).firstOrNull()?.alignment
            Log.d("AlignTest", "layout.getLineStart(i): ${layout.getLineStart(i)}, layout.getLineEnd(i): ${layout.getLineEnd(i)}")
            if (i == lineStart) {
                consistentAlignment = alignmentSpan
            } else if (alignmentSpan != consistentAlignment) {
                isConsistent = false
                break
            }
        }

        val alignButtonIndex = if (isConsistent && consistentAlignment != null) {
            when (consistentAlignment) {
                Layout.Alignment.ALIGN_NORMAL -> 0 // 왼쪽 정렬
                Layout.Alignment.ALIGN_CENTER -> 1 // 가운데 정렬
                Layout.Alignment.ALIGN_OPPOSITE -> 2 // 오른쪽 정렬
                else -> 0 // 기본 정렬
            }
        } else {
            0 // 기본 정렬 (왼쪽 정렬) 표시
        }
        Log.d("AlignTest", "alignButtonIndex: $alignButtonIndex")

        // 이전에 선택된 버튼의 상태를 초기화
        for (i in 0 until linearLayoutTextAlign.childCount) {
            val button = linearLayoutTextAlign.getChildAt(i) as ImageButton
            button.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.pallet_normal))
        }

        // 새로운 정렬 상태에 따른 버튼 설정
        currentAlignButton = linearLayoutTextAlign[alignButtonIndex] as ImageButton
        currentAlignButton.setImageDrawable(
            ContextCompat.getDrawable(activity, R.drawable.pallet_pressed)
        )
        textAlignDialog.setTitle("Text Align: ")
        textAlignDialog.show()
    }

    // 정렬 Tag를 받아 Layout.Alignment로 변경
    fun setAlign(alignTag: String) {
        val alignment = when (alignTag) {
            "start" -> Layout.Alignment.ALIGN_NORMAL    // 왼쪽 정렬
            "center" -> Layout.Alignment.ALIGN_CENTER   // 가운데 정렬
            "end" -> Layout.Alignment.ALIGN_OPPOSITE    // 오른쪽 정렬
            else -> Layout.Alignment.ALIGN_NORMAL   // 기본 정렬
        }
        applyTextAlignment(alignment)
    }

    // 선택된 곳 정렬 해주기
    private fun applyTextAlignment(alignment: Layout.Alignment) {
        val spannableString = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        var (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }

        // Align은 선택된 줄 전체가 정렬되어야 하므로 줄의 처음과 끝을 설정해준다.
        val layout = editContent.layout
        val lineStart = layout.getLineForOffset(start)
        val lineEnd = layout.getLineForOffset(end)
        start = layout.getLineStart(lineStart)
        end = layout.getLineEnd(lineEnd)

        // 해당 범위에 적용된 기존 정렬 스팬 제거
        val alignmentSpans = spannableString.getSpans(start, end, AlignmentSpan::class.java)
        for (span in alignmentSpans) {
            spannableString.removeSpan(span)
        }

        // 해당 범위에 정렬 스팬 적용
        spannableString.setSpan(
            AlignmentSpan.Standard(alignment),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editContent.setText(spannableString)
        editContent.setSelection(selectionStart, selectionEnd)
    }
}