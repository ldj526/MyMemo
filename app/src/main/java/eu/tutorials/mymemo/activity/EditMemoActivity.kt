package eu.tutorials.mymemo.activity

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.draw.DrawingView
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.textattribute.CustomEditText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class EditMemoActivity : AppCompatActivity() {

    private lateinit var updateTitle: EditText
    private lateinit var updateContent: CustomEditText
    private lateinit var currentTitle: String
    private lateinit var currentContent: String
    private lateinit var updateDrawingView: DrawingView
    private var currentId: Int? = null
    private var bitmap: Bitmap? = null
    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)

        updateTitle = findViewById(R.id.et_title)
        updateContent = findViewById(R.id.et_content)
        updateDrawingView = findViewById(R.id.drawingView)

        // MainActivity로부터 folderId 읽기
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        val folderId = sharedPref.getInt("lastSelectedFolderId", -1)

        val currentMemo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentMemo", Memo::class.java)
        } else {
            intent.getParcelableExtra<Memo>("currentMemo")
        }

        if (currentMemo != null) {
            currentId = currentMemo.id
            currentTitle = currentMemo.title!!
            currentContent = currentMemo.content!!
            bitmap = currentMemo.imagePath?.let { loadBitmapFromInternalStorage(it) }
            bitmap?.let {
                // DrawingView에 Bitmap 설정
                updateDrawingView.setBitmap(it)
            }

            updateTitle.setText(currentTitle)
            updateContent.setText(currentContent)
        } else {
            Toast.makeText(this, "불러오기 실패", Toast.LENGTH_LONG).show()
        }
        val btnUpdate: ImageView = findViewById(R.id.btn_save)

        btnUpdate.setOnClickListener {
            val title = updateTitle.text.toString()
            val content = updateContent.text.toString()
            val bitmap = getBitmapFromView(updateDrawingView)
            saveBitmapToFileInternalStorage(bitmap, currentMemo?.date!!)
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(applicationContext, "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                val memo = Memo(currentId!!, title, content, currentMemo.date, false, folderId, currentMemo.imagePath)
                memoViewModel.update(memo)
                finish()
            }
        }
    }

    // 내부 저장소에 저장
    private fun saveBitmapToFileInternalStorage(bitmap: Bitmap, timeStamp: Long) {
        val contextWrapper = ContextWrapper(this)
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
    }

    private fun loadBitmapFromInternalStorage(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        // view 와 동일한 크기의 Bitmap 생성
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)

        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }
}