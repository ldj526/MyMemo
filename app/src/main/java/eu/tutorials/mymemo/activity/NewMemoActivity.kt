package eu.tutorials.mymemo.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)

        // MainActivity로부터 folderId 읽기
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        val folderId = sharedPref.getInt("lastSelectedFolderId", -1)
        val drawBottomAppbar = findViewById<BottomAppBar>(R.id.drawBottomAppbar)

        brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        linearLayoutPaintColors = brushDialog.findViewById(R.id.ll_paint_colors)
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

                else -> false
            }
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
}