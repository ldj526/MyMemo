package eu.tutorials.mymemo.textattribute

import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.widget.Spinner

class TextSizeManager(private val editContent: CustomEditText) {
    fun updateSpinnerSelectionBasedOnTextSize(spinner: Spinner, textSizeOptions: Array<String>) {
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        // 선택 시작 및 끝 인덱스 순서 확인 및 조정
        val (start, end) = if (selectionStart <= selectionEnd) {
            selectionStart to selectionEnd
        } else {
            selectionEnd to selectionStart
        }

        val spans = if (start != end) {
            // 텍스트가 선택된 경우
            editContent.text?.getSpans(start, end, AbsoluteSizeSpan::class.java)
                ?: emptyArray()
        } else {
            // 텍스트가 선택되지 않은 경우 (커서 위치에서 스팬 찾기)
            val layout = editContent.layout
            val line = layout.getLineForOffset(selectionStart)
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)
            editContent.text?.getSpans(lineStart, lineEnd, AbsoluteSizeSpan::class.java)
                ?: emptyArray()
        }

        var currentSizeSp = 0
        if (spans.isNotEmpty()) {
            currentSizeSp = spans.first().size
        } else {
            // 기본 텍스트 크기를 사용하거나 기본값 설정
            val defaultSizePx = editContent.textSize
            currentSizeSp =
                (defaultSizePx / editContent.context.resources.displayMetrics.scaledDensity).toInt()
        }
        Log.d("textSizeTest", "currentSizeSp : $currentSizeSp")
        val spinnerPosition = if(spans.isEmpty()){
            textSizeOptions.indexOf(15.toString())
        } else if (spans.distinct().size == 1) {
            // 모든 스팬 크기가 동일한 경우 해당 크기 사용
            textSizeOptions.indexOf(currentSizeSp.toString())
        } else {
            // 서로 다른 크기의 스팬이 있는 경우 스피너 업데이트하지 않음
            -1
        }

        if (spinnerPosition >= 0) {
            spinner.setSelection(spinnerPosition, false)
        }
    }
}