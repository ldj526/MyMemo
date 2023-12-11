package eu.tutorials.mymemo.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.bottomappbar.BottomAppBar
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.draw.DrawingView
import eu.tutorials.mymemo.model.Memo

class NewMemoActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private var isDrawingEnabled = false
    private lateinit var imageButtonCurrentPaint: ImageButton
    private lateinit var linearLayoutPaintColors: LinearLayout
    private lateinit var brushDialog: Dialog

    private lateinit var imageButtonCurrentAlign: ImageButton
    private lateinit var linearLayoutTextAlign: LinearLayout
    private lateinit var textAlignDialog: Dialog

    var isUnderlineApplied = false
    var isStrikethroughApplied = false
    var isBoldApplied = false
    var isItalicApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(findViewById(R.id.drawBottomAppbar))

        // MainActivity로부터 folderId 읽기
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        val folderId = sharedPref.getInt("lastSelectedFolderId", -1)
        val drawBottomAppbar = findViewById<BottomAppBar>(R.id.drawBottomAppbar)

        brushDialog = Dialog(this)
        textAlignDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        textAlignDialog.setContentView(R.layout.dialog_text_align)
        linearLayoutPaintColors = brushDialog.findViewById(R.id.ll_paint_colors)
        linearLayoutTextAlign = textAlignDialog.findViewById(R.id.ll_text_align)
        editTitle = findViewById(R.id.et_title)
        editContent = findViewById(R.id.et_content)
        drawingView = findViewById(R.id.drawingView)

        val button = findViewById<ImageView>(R.id.btn_save)
        button.setOnClickListener {
            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(applicationContext, "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                val timeStamp = System.currentTimeMillis()
                val memo = Memo(null, title, content, timeStamp, false, folderId)
                memoViewModel.insert(memo)
                finish()
            }
        }
        imageButtonCurrentAlign = linearLayoutTextAlign[0] as ImageButton
        imageButtonCurrentAlign.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        drawBottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawMode -> {
                    // 버튼을 클릭했을 때 그림 그리는 기능을 활성화/비활성화

                    isDrawingEnabled = !isDrawingEnabled
                    if (isDrawingEnabled) {
                        drawingView.enableDrawing()
                    } else {
                        drawingView.disableDrawing()
                    }
                    true
                }

                R.id.brushSize -> {
                    showBrushSizeChooserDialog()
                    true
                }

                R.id.undo -> {
                    drawingView.onClickUndo()
                    true
                }

                R.id.textStyle -> {
                    showTextStyleChooserDialog()
                    true
                }

                R.id.textAlign -> {
                    showTextAlignChooserDialog()
                    true
                }

                else -> false
            }
        }

    }

    // 정렬을 위한 Dialog 띄우기
    private fun showTextAlignChooserDialog() {
        // 적용된 정렬을 가져와 image에 표시해주기 위함
        val spannableString = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd
        val alignmentSpan =
            spannableString.getSpans(selectionStart, selectionEnd, AlignmentSpan::class.java)
                .firstOrNull()
        val alignButtonIndex = when (alignmentSpan?.alignment) {
            Layout.Alignment.ALIGN_NORMAL -> 0  // 왼쪽 정렬
            Layout.Alignment.ALIGN_CENTER -> 1 // 가운데 정렬
            Layout.Alignment.ALIGN_OPPOSITE -> 2 // 오른쪽 정렬
            else -> 0 // 기본 정렬
        }

        // 이전에 선택된 버튼의 상태를 초기화
        imageButtonCurrentAlign.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_normal)
        )

        // 새로운 정렬 상태에 따른 버튼 설정
        imageButtonCurrentAlign = linearLayoutTextAlign[alignButtonIndex] as ImageButton
        imageButtonCurrentAlign.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
        textAlignDialog.setTitle("Text Align: ")
        textAlignDialog.show()
    }

    // 정렬 이미지를 클릭했을 때
    fun textAlignClicked(view: View) {
        if (view !== imageButtonCurrentAlign) {
            // 정렬 이미지 업데이트
            val imageButton = view as ImageButton
            // tag는 현재 정렬을 이전 정렬로 바꾸는 데 사용된다.
            val alignTag = imageButton.tag.toString()
            setAlign(alignTag)
            // 마지막 활성 이미지 버튼과 현재 활성 이미지 버튼의 배경을 바꿉니다.
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            imageButtonCurrentAlign.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            // 현재 뷰는 ImageButton 형태로 선택된 뷰로 업데이트됩니다.
            imageButtonCurrentAlign = view
        }
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
    fun applyTextAlignment(alignment: Layout.Alignment) {
        val spannableString = SpannableString(editContent.text)
        val selectionStart = editContent.selectionStart
        val selectionEnd = editContent.selectionEnd

        val start: Int
        val end: Int

        if (selectionStart < selectionEnd) {
            // 텍스트가 선택된 경우
            start = selectionStart
            end = selectionEnd
        } else {
            // 텍스트가 선택되지 않은 경우 커서 위치의 줄 찾기
            val layout = editContent.layout
            val line = layout.getLineForOffset(selectionStart)
            start = layout.getLineStart(line)
            end = layout.getLineEnd(line)
        }

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

    // textStyle을 변경하기 위해 띄워주는 Dialog
    private fun showTextStyleChooserDialog() {
        val textDialog = Dialog(this)
        textDialog.setContentView(R.layout.dialog_text_style)
        textDialog.setTitle("Text Style")
        textDialog.show()
    }

    fun styleClicked(view: View) {
        // 스타일 업데이트
        val styleImageButton = view as ImageButton
        val styleTag = styleImageButton.tag.toString()
        when (styleTag) {
            "bold" -> {
                isBoldApplied = !isBoldApplied
                updateStyleImage(styleImageButton, isBoldApplied)
            }

            "italic" -> {
                isItalicApplied = !isItalicApplied
                updateStyleImage(styleImageButton, isItalicApplied)
            }

            "underline" -> {
                isUnderlineApplied = !isUnderlineApplied
                updateStyleImage(styleImageButton, isUnderlineApplied)
            }

            "strikethrough" -> {
                isStrikethroughApplied = !isStrikethroughApplied
                updateStyleImage(styleImageButton, isStrikethroughApplied)
            }
        }
        applyTextStyle()
    }

    // dialog에서 각 이미지의 view 설정
    fun updateStyleImage(button: ImageButton, isStyleApplied: Boolean) {
        if (isStyleApplied) {
            button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
        } else {
            button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))
        }
    }

    // 텍스트 스타일 적용 함수
    fun applyTextStyle() {
        val spannableString = SpannableString(editContent.text)
        val start = editContent.selectionStart
        val end = editContent.selectionEnd
        // 굵게 스타일 적용 또는 해제
        updateStyleSpan(spannableString, Typeface.BOLD, isBoldApplied, start, end)

        // 기울임꼴 스타일 적용 또는 해제
        updateStyleSpan(spannableString, Typeface.ITALIC, isItalicApplied, start, end)

        // 밑줄 스타일 적용 또는 해제
        updateSpan(spannableString, UnderlineSpan(), isUnderlineApplied, start, end)

        // 취소선 스타일 적용 또는 해제
        updateSpan(spannableString, StrikethroughSpan(), isStrikethroughApplied, start, end)

        editContent.setText(spannableString)
        editContent.setSelection(start, end)
    }

    // styleSpan에 따라 변경
    fun updateStyleSpan(
        spannable: SpannableString,
        style: Int,
        isApplied: Boolean,
        start: Int,
        end: Int
    ) {
        val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        existingSpans.forEach {
            if (it.style == style) {
                spannable.removeSpan(it)
            }
        }

        if (isApplied) {
            spannable.setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    // CharacterStyle에 따라 변경
    fun <T : CharacterStyle> updateSpan(
        spannable: SpannableString,
        span: T,
        isApplied: Boolean,
        start: Int,
        end: Int
    ) {
        spannable.getSpans(start, end, span::class.java).forEach {
            spannable.removeSpan(it)
        }

        if (isApplied) {
            spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    // Brush 크기를 조절하는 Dialog
    private fun showBrushSizeChooserDialog() {
        val brushSeekBar = brushDialog.findViewById<SeekBar>(R.id.brushSeekBar)
        val brushSizeText = brushDialog.findViewById<TextView>(R.id.brushSizeText)
        imageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        imageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        val currentBrushSize = drawingView.brushSize // null 이면 초기값 3으로 설정
        brushSeekBar.progress = currentBrushSize.toInt()    // SeekBar의 progress
        brushSizeText.text = brushSeekBar.progress.toString()   // SeekBar의 progress 표현

        brushSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView.setSizeForBrush(progress.toFloat())
                brushSizeText.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        brushDialog.setTitle("Brush size: ")
        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== imageButtonCurrentPaint) {
            // 색 업데이트
            val imageButton = view as ImageButton
            // tag는 현재 색상을 이전 색상으로 바꾸는 데 사용된다.
            val colorTag = imageButton.tag.toString()
            drawingView.setColor(colorTag)
            // 마지막 활성 이미지 버튼과 현재 활성 이미지 버튼의 배경을 바꿉니다.
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            imageButtonCurrentPaint.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            // 현재 뷰는 ImageButton 형태로 선택된 뷰로 업데이트됩니다.
            imageButtonCurrentPaint = view
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawing_bottom_app_bar, menu)

        val textSizeSpinnerItem = menu?.findItem(R.id.textSize)
        val textSizeSpinner = textSizeSpinnerItem?.actionView as Spinner
        val textSizeOptions = arrayOf("12", "13", "14", "15")

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, textSizeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textSizeSpinner.adapter = adapter


        textSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedSize = textSizeOptions[position].toInt()
                // 글자 크기 바꿔주는 기능
                val spannable = SpannableString(editContent.text)
                spannable.setSpan(
                    AbsoluteSizeSpan(selectedSize, true),
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                editContent.setText(spannable, TextView.BufferType.SPANNABLE)
                editContent.setSelection(editContent.selectionStart, editContent.selectionEnd)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        return true
    }
}