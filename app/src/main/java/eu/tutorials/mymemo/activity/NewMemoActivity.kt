package eu.tutorials.mymemo.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.draw.DrawingView
import eu.tutorials.mymemo.model.Memo

class NewMemoActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private var isDrawingEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)

        // MainActivity로부터 folderId 읽기
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        val folderId = sharedPref.getInt("lastSelectedFolderId", -1)
        val drawBottomAppbar = findViewById<BottomAppBar>(R.id.drawBottomAppbar)

        editTitle = findViewById(R.id.et_title)
        editContent = findViewById(R.id.et_content)

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
                    drawingView = findViewById(R.id.drawingView)
                    isDrawingEnabled = !isDrawingEnabled
                    if (isDrawingEnabled) {
                        drawingView?.enableDrawing()
                    } else {
                        drawingView?.disableDrawing()
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
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        val brushSeekBar = brushDialog.findViewById<SeekBar>(R.id.brushSeekBar)
        val brushSizeText = brushDialog.findViewById<TextView>(R.id.brushSizeText)

        val currentBrushSize = drawingView?.brushSize ?: 3.0.toFloat()  // null 이면 초기값 3으로 설정
        brushSeekBar.progress = currentBrushSize.toInt()    // SeekBar의 progress
        brushSizeText.text = brushSeekBar.progress.toString()   // SeekBar의 progress 표현

        brushSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView?.setSizeForBrush(progress.toFloat())
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
}