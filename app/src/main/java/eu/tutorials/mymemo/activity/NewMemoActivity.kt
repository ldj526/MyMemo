package eu.tutorials.mymemo.activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

        val exam = findViewById<Button>(R.id.exam)
        exam.setOnClickListener {
            // 버튼을 클릭했을 때 그림 그리는 기능을 활성화/비활성화
            drawingView = findViewById(R.id.drawingView)
            isDrawingEnabled = !isDrawingEnabled
            if (isDrawingEnabled) {
                drawingView?.enableDrawing()
            } else {
                drawingView?.disableDrawing()
            }
        }
    }
}