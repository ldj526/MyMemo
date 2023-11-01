package eu.tutorials.mymemo.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.databinding.ActivityFolderListBinding
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.viewmodel.FolderViewModel
import eu.tutorials.mymemo.viewmodel.FolderViewModelFactory

class FolderListActivity : AppCompatActivity() {

    private var _binding: ActivityFolderListBinding? = null
    private val binding get() = _binding!!

    private val folderViewModel: FolderViewModel by viewModels() {
        FolderViewModelFactory((application as MemosApplication).folderRepository)
    }
    private val folderAdapter = FolderListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFolderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.folderListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.title = "폴더 관리"

        binding.addFolderLayout.setOnClickListener {
            // 다이얼로그로 새 폴더 이름 입력 받기
            val input = EditText(this)
            val dialog = AlertDialog.Builder(this)
                .setTitle("새 폴더 이름 입력")
                .setView(input)
                .setPositiveButton("추가") { dialog, _ ->
                    val folderName = input.text.toString()
                    if (folderName.isNotEmpty()) {
                        // 새 폴더 데이터 추가
                        val newFolder = Folder(null, folderName, null)
                        // 여기에 데이터베이스 저장 로직 추가
                        folderViewModel.insert(newFolder)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }

        binding.expandableListView.setAdapter(folderAdapter)

        // folderlist 가져오기
        folderViewModel.folderList.observe(this, Observer { folders ->
            folders?.let { folderAdapter.setFolder(Folder(null, "폴더", null), it) }
        })
    }

    // Menu 에서 선택했을 때
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editIcon -> {

            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}