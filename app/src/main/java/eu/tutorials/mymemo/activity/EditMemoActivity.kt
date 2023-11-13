package eu.tutorials.mymemo.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Memo

class EditMemoActivity : AppCompatActivity() {

    private lateinit var updateTitle: EditText
    private lateinit var updateContent: EditText
    private lateinit var currentTitle: String
    private lateinit var currentContent: String
    private var currentId: Int? = null
    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memo)

        updateTitle = findViewById(R.id.update_title)
        updateContent = findViewById(R.id.update_content)

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

            updateTitle.setText(currentTitle)
            updateContent.setText(currentContent)
        } else {
            Toast.makeText(this, "불러오기 실패", Toast.LENGTH_LONG).show()
        }
        val btnUpdate: ImageView = findViewById(R.id.btn_update)

        btnUpdate.setOnClickListener {
            val title = updateTitle.text.toString()
            val content = updateContent.text.toString()
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(applicationContext, "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                val memo = Memo(currentId!!, title, content, currentMemo?.date, false, folderId)
                memoViewModel.update(memo)
                finish()
            }
        }
    }
}