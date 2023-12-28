package eu.tutorials.mymemo.fragment

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.core.content.ContextCompat
import androidx.core.view.ContentInfoCompat.Flags
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomappbar.BottomAppBar
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.activity.MainActivity
import eu.tutorials.mymemo.draw.DrawingView
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.textattribute.CustomEditText
import eu.tutorials.mymemo.textattribute.TextAlignmentManager
import eu.tutorials.mymemo.textattribute.TextSizeManager
import eu.tutorials.mymemo.textattribute.TextStyleManager
import eu.tutorials.mymemo.utils.BottomAppBarHost
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MemoFragment : Fragment() {

    private lateinit var drawingView: DrawingView

    private lateinit var editTitle: EditText
    private lateinit var editContent: CustomEditText
    private val memoViewModel: MemoViewModel by activityViewModels() {
        MemoViewModelFactory((activity?.application as MemosApplication).repository)
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

    private lateinit var drawBottomAppBar: BottomAppBar
    private lateinit var textBottomAppBar: BottomAppBar

    private var isSpinnerSelectionChanged = false
    private var isUnderlineApplied = false
    private var isStrikethroughApplied = false
    private var isBoldApplied = false
    private var isItalicApplied = false

    private var folderId: Int? = null
    private var isEditMode: Boolean = false

    private var currentMemo: Memo? = null
    private lateinit var currentTitle: String
    private lateinit var currentContent: String
    private var currentId: Int? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            isEditMode = it.getBoolean("isEditMode", false)
            currentMemo = it.getParcelable("currentMemo")
            Log.d("MemoFragment", "MemoFragment onCreate: $isEditMode")
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memo, container, false)

        editContent = view.findViewById(R.id.et_content)
        editTitle = view.findViewById(R.id.et_title)
        drawingView = view.findViewById(R.id.drawingView)

        // MainActivity로부터 folderId 읽기
        val sharedPref = activity?.getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        folderId = sharedPref?.getInt("lastSelectedFolderId", -1)

        brushDialog = Dialog(requireContext())
        textAlignDialog = Dialog(requireContext())
        textStyleDialog = Dialog(requireContext())

        brushDialog.setContentView(R.layout.dialog_brush_size)
        textAlignDialog.setContentView(R.layout.dialog_text_align)
        textStyleDialog.setContentView(R.layout.dialog_text_style)

        linearLayoutPaintColors = brushDialog.findViewById(R.id.ll_paint_colors)
        linearLayoutTextAlign = textAlignDialog.findViewById(R.id.ll_text_align)
        linearLayoutTextStyle = textStyleDialog.findViewById(R.id.ll_text_style)

        imageButtonCurrentAlign = linearLayoutTextAlign[0] as ImageButton
        imageButtonCurrentAlign.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.pallet_pressed)
        )
        textAlignmentManager =
            TextAlignmentManager(
                editContent,
                imageButtonCurrentAlign,
                requireActivity(),
                textAlignDialog
            )
        textStyleManager = TextStyleManager(editContent, requireActivity(), textStyleDialog)
        textSizeManager = TextSizeManager(editContent)

        val saveButton = view.findViewById<ImageView>(R.id.btn_save)
        saveButton.setOnClickListener {
            saveMemo()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val currentMemo = it.getParcelable<Memo>("currentMemo")
            currentMemo?.let { memo ->
                getMemoInfo(memo)
            }
        }

        drawBottomAppBar = view.findViewById(R.id.drawBottomAppbar)
        textBottomAppBar = view.findViewById(R.id.textBottomAppbar)

        (activity as? BottomAppBarHost)?.let { host ->
            host.setBottomAppBar(drawBottomAppBar)
            host.setBottomAppBar(textBottomAppBar)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(createMenuProvider(), viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun createMenuProvider(): MenuProvider {
        Log.d("MemoFragment", "createMenuProvider() 실행")
        return object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                Log.d("MemoFragment", "createMenuProvider() onCreateMenu")
                menuInflater.inflate(R.menu.text_bottom_app_bar_menu, menu)

                val textSizeSpinnerItem = menu.findItem(R.id.textSize)
                val textSizeSpinner = textSizeSpinnerItem?.actionView as Spinner
                val textSizeOptions = arrayOf("12", "13", "14", "15", "16", "17", "18", "19", "20")
                Log.d("MemoFragment", "createMenuProvider() object")
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, textSizeOptions)
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
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {

                    R.id.brushSize -> {
                        showBrushSizeChooserDialog()
                        return true
                    }

                    R.id.undo -> {
                        drawingView.onClickUndo()
                        return true
                    }

                    R.id.textMode -> {
                        textBottomAppBar.visibility = View.VISIBLE
                        drawBottomAppBar.visibility = View.GONE
                        drawingView.disableDrawing()
                        return true
                    }

                    R.id.drawMode -> {
                        drawingView.enableDrawing()
                        drawBottomAppBar.visibility = View.VISIBLE
                        textBottomAppBar.visibility = View.GONE
                        Log.d("MemoFragment", "drawMode 작동")
                        Log.d("MemoFragment", "${drawBottomAppBar.visibility} 작동")
                        Log.d("MemoFragment", "${textBottomAppBar.visibility} 작동")
                        return true
                    }

                    R.id.textStyle -> {
                        textStyleManager.showTextStyleChooserDialog(linearLayoutTextStyle)
                        Log.d("MemoFragment", "textStyle 작동")
                        return true
                    }

                    R.id.textAlign -> {
                        textAlignmentManager.showTextAlignChooserDialog(linearLayoutTextAlign)
                        Log.d("MemoFragment", "textAlign 작동")
                        return true
                    }
                }
                return false
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
            textAlignmentManager.setAlign(alignTag)
            // 마지막 활성 이미지 버튼과 현재 활성 이미지 버튼의 배경을 바꿉니다.
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.pallet_pressed)
            )

            imageButtonCurrentAlign.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.pallet_normal)
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
    }

    // Brush 크기를 조절하는 Dialog
    private fun showBrushSizeChooserDialog() {
        val brushSeekBar = brushDialog.findViewById<SeekBar>(R.id.brushSeekBar)
        val brushSizeText = brushDialog.findViewById<TextView>(R.id.brushSizeText)
        imageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        imageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.pallet_pressed)
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
                ContextCompat.getDrawable(requireContext(), R.drawable.pallet_pressed)
            )

            imageButtonCurrentPaint.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.pallet_normal)
            )

            // 현재 뷰는 ImageButton 형태로 선택된 뷰로 업데이트됩니다.
            imageButtonCurrentPaint = view
        }
    }

    // 내부 저장소에 저장
    private fun saveBitmapToFileInternalStorage(bitmap: Bitmap, timeStamp: Long): String {
        val contextWrapper = ContextWrapper(requireContext())
        // 내부 저장소의 디렉토리를 참조
        val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        // 파일 객체 생성
        val file = File(directory, "$timeStamp.png")

        try {
            val stream: OutputStream = FileOutputStream(file)
            // 비트맵을 PNG 형식으로 압축 및 저장
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 저장된 파일의 절대 경로 반환
        return file.absolutePath
    }

    // View로부터 Bitmap 얻기
    private fun getBitmapFromView(view: View): Bitmap {
        // view 와 동일한 크기의 Bitmap 생성
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)

        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private fun getMemoInfo(currentMemo: Memo) {
        currentId = currentMemo.id
        currentTitle = currentMemo.title!!
        currentContent = currentMemo.content!!
        bitmap = currentMemo.imagePath?.let { loadBitmapFromInternalStorage(it) }
        bitmap?.let {
            // DrawingView에 Bitmap 설정
            drawingView.setBitmap(it)
        }

        editTitle.setText(currentTitle)
        editContent.setText(currentContent)
    }

    private fun saveMemo() {
        Log.d("MemoFragment", "isEditMode: $isEditMode")
        if (isEditMode) {
            // EditMemoActivity에서의 동작
            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            val bitmap = getBitmapFromView(drawingView)
            Log.d("MemoFragment", "currentMemo : $currentMemo")
            saveBitmapToFileInternalStorage(bitmap, currentMemo?.date!!)
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                val memo = Memo(currentId!!, title, content, currentMemo?.date, false, folderId, currentMemo?.imagePath)
                memoViewModel.update(memo)
            }

        } else {
            // NewMemoActivity에서의 동작
            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                val timeStamp = System.currentTimeMillis()
                val bitmap = getBitmapFromView(drawingView)
                val memo = Memo(null, title, content, timeStamp, false, folderId, saveBitmapToFileInternalStorage(bitmap, timeStamp))
                memoViewModel.insert(memo)
            }
        }
    }

    private fun loadBitmapFromInternalStorage(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(isEditMode: Boolean, currentMemo: Memo?) =
            MemoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEditMode", isEditMode)
                    putParcelable("currentMemo", currentMemo)
                }
            }
    }
}