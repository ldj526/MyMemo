package eu.tutorials.mymemo.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.Log
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
import eu.tutorials.mymemo.textattribute.CustomEditText
import eu.tutorials.mymemo.textattribute.TextAlignmentManager
import eu.tutorials.mymemo.textattribute.TextSizeManager
import eu.tutorials.mymemo.textattribute.TextStyleManager
import java.lang.reflect.Type

class NewMemoActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView

    private lateinit var editTitle: EditText
    private lateinit var editContent: CustomEditText
    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private lateinit var imageButtonCurrentPaint: ImageButton
    private lateinit var linearLayoutPaintColors: LinearLayout
    private lateinit var brushDialog: Dialog

    private lateinit var imageButtonCurrentAlign: ImageButton
    private lateinit var linearLayoutTextAlign: LinearLayout
    private lateinit var textAlignDialog: Dialog

    private lateinit var textAlignmentManager: TextAlignmentManager
    private lateinit var textStyleManager: TextStyleManager
    private lateinit var textSizeManager: TextSizeManager

    private lateinit var linearLayoutTextStyle: LinearLayout
    private lateinit var textStyleDialog: Dialog

    private var isSpinnerSelectionChanged = false
    var isUnderlineApplied = false
    var isStrikethroughApplied = false
    var isBoldApplied = false
    var isItalicApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(findViewById(R.id.drawBottomAppbar))
        setSupportActionBar(findViewById(R.id.textBottomAppbar))

        // MainActivity로부터 folderId 읽기
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        val folderId = sharedPref.getInt("lastSelectedFolderId", -1)
        val drawBottomAppbar = findViewById<BottomAppBar>(R.id.drawBottomAppbar)
        val textBottomAppbar = findViewById<BottomAppBar>(R.id.textBottomAppbar)

        editContent = findViewById(R.id.et_content)
        brushDialog = Dialog(this)
        textAlignDialog = Dialog(this)
        textStyleDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        textAlignDialog.setContentView(R.layout.dialog_text_align)
        textStyleDialog.setContentView(R.layout.dialog_text_style)
        linearLayoutPaintColors = brushDialog.findViewById(R.id.ll_paint_colors)
        linearLayoutTextAlign = textAlignDialog.findViewById(R.id.ll_text_align)
        linearLayoutTextStyle = textStyleDialog.findViewById(R.id.ll_text_style)
        editTitle = findViewById(R.id.et_title)
        drawingView = findViewById(R.id.drawingView)

        val saveButton = findViewById<ImageView>(R.id.btn_save)
        saveButton.setOnClickListener {
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
        textAlignmentManager =
            TextAlignmentManager(editContent, imageButtonCurrentAlign, this, textAlignDialog)
        textStyleManager = TextStyleManager(editContent, this, textStyleDialog)
        textSizeManager = TextSizeManager(editContent)

        drawBottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {

                R.id.brushSize -> {
                    showBrushSizeChooserDialog()
                    true
                }

                R.id.undo -> {
                    drawingView.onClickUndo()
                    true
                }

                R.id.textMode -> {
                    textBottomAppbar.visibility = View.VISIBLE
                    drawBottomAppbar.visibility = View.GONE
                    drawingView.disableDrawing()
                    true
                }

                else -> false
            }
        }

        textBottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawMode -> {
                    drawingView.enableDrawing()
                    drawBottomAppbar.visibility = View.VISIBLE
                    textBottomAppbar.visibility = View.GONE
                    true
                }

                R.id.textStyle -> {
                    textStyleManager.showTextStyleChooserDialog(linearLayoutTextStyle)
                    true
                }

                R.id.textAlign -> {
                    textAlignmentManager.showTextAlignChooserDialog(linearLayoutTextAlign)
                    true
                }

                else -> false
            }
        }

    }

    // 정렬 이미지를 클릭했을 때
    fun textAlignClicked(view: View) {
        if (view !== imageButtonCurrentAlign) {
            // 정렬 이미지 업데이트
            val imageButton = view as ImageButton
            // tag는 현재 정렬을 이전 정렬로 바꾸는 데 사용된다.
            val alignTag = imageButton.tag.toString()
            Log.d("AlignTest", "alignTag: $alignTag")
            textAlignmentManager.setAlign(alignTag)
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

    fun styleClicked(view: View) {
        // 스타일 업데이트
        val styleImageButton = view as ImageButton
        val styleTag = styleImageButton.tag.toString()
        when (styleTag) {
            "bold" -> {
                isBoldApplied = !isBoldApplied
                textStyleManager.updateStyleImage(styleImageButton, isBoldApplied)
                textStyleManager.updateStyleSpan(Typeface.BOLD, isBoldApplied)
            }

            "italic" -> {
                isItalicApplied = !isItalicApplied
                textStyleManager.updateStyleImage(styleImageButton, isItalicApplied)
                textStyleManager.updateStyleSpan(Typeface.ITALIC, isItalicApplied)
            }

            "underline" -> {
                isUnderlineApplied = !isUnderlineApplied
                textStyleManager.updateStyleImage(styleImageButton, isUnderlineApplied)
                textStyleManager.updateSpan(UnderlineSpan(), isUnderlineApplied)
            }

            "strikethrough" -> {
                isStrikethroughApplied = !isStrikethroughApplied
                textStyleManager.updateStyleImage(styleImageButton, isStrikethroughApplied)
                textStyleManager.updateSpan(StrikethroughSpan(), isStrikethroughApplied)
            }
        }
//        textStyleManager.applyTextStyle(
//            isBoldApplied,
//            isItalicApplied,
//            isUnderlineApplied,
//            isStrikethroughApplied
//        )
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
        menuInflater.inflate(R.menu.text_bottom_app_bar_menu, menu)

        val textSizeSpinnerItem = menu?.findItem(R.id.textSize)
        val textSizeSpinner = textSizeSpinnerItem?.actionView as Spinner
        val textSizeOptions = arrayOf("12", "13", "14", "15", "16", "17", "18", "19", "20")

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, textSizeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textSizeSpinner.adapter = adapter

        textSizeSpinner.setSelection(3, false)

        editContent.selectionChangedListener = {
            if (!isSpinnerSelectionChanged) {
                textSizeManager.updateSpinnerSelectionBasedOnTextSize(
                    textSizeSpinner,
                    textSizeOptions
                )
            }
        }
        textSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                isSpinnerSelectionChanged = true
                val selectedSize = textSizeOptions[position].toInt()
                val selectionStart = editContent.selectionStart
                val selectionEnd = editContent.selectionEnd

                // 선택 시작 및 끝 인덱스 순서 확인 및 조정
                var (start, end) = if (selectionStart <= selectionEnd) {
                    selectionStart to selectionEnd
                } else {
                    selectionEnd to selectionStart
                }

                if (start == end) {
                    // 텍스트가 선택되지 않은 경우 커서 위치의 줄 찾기
                    val layout = editContent.layout
                    val line = layout.getLineForOffset(selectionStart)
                    start = layout.getLineStart(line)
                    end = layout.getLineEnd(line)
                }

                val spannable = SpannableString(editContent.text)
                // 기존에 있는 span 제거
                spannable.getSpans(start, end, AbsoluteSizeSpan::class.java).forEach {
                    spannable.removeSpan(it)
                }
                // 글자 크기 바꿔주는 기능
                spannable.setSpan(
                    AbsoluteSizeSpan(selectedSize, true),
                    start,
                    end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                editContent.setText(
                    spannable,
                    TextView.BufferType.SPANNABLE
                )   // 이 과정에서 커서 위치가 초기화된다.
                editContent.setSelection(selectionStart, selectionEnd)
                isSpinnerSelectionChanged = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        return true
    }
}