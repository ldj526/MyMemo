package eu.tutorials.mymemo.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.databinding.ActivityNewMemoBinding
import eu.tutorials.mymemo.model.Memo

class NewMemoActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var memo: Memo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)

        editTitle = findViewById(R.id.et_title)
        editContent = findViewById(R.id.et_content)

        val button = findViewById<Button>(R.id.btn_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTitle.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val title = editTitle.text.toString()
                val content = editContent.text.toString()
                memo = Memo(null, title, content)
                replyIntent.putExtra(EXTRA_REPLY, memo)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.memolistsql.REPLY"
    }
}