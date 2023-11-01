package eu.tutorials.mymemo.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

        savedInstanceState?.let {
            // 화면 회전 시 BottomAppBar 유지시키기 위함
            val isBottomAppBarVisible = it.getBoolean("BOTTOM_APPBAR_VISIBLE")
            binding.bottomAppbar.visibility = if (isBottomAppBarVisible) View.VISIBLE else View.GONE
        }

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

        // checkbox check 상태 관찰
        folderViewModel.checkboxStates.observe(this, Observer { states ->
            folderAdapter.updateCheckboxStates(states)
        })

        // checkbox 보이기 / 숨기기 관찰
        folderViewModel.isCheckboxVisible.observe(this, Observer { isVisible ->
            folderAdapter.setCheckboxVisibility(isVisible)
        })

        // folderlist 가져오기
        folderViewModel.folderList.observe(this, Observer { folders ->
            folders?.let { folderAdapter.setFolder(Folder(null, "폴더", null), it) }
        })

        binding.bottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteIcon -> {
                    val selectedItems = folderAdapter.getSelectedItems() // 선택된 항목들 가져오기
                    folderViewModel.delete(selectedItems) // ViewModel에서 삭제 로직 호출
                    folderAdapter.notifyDataSetChanged() // Adapter에 데이터 변경 알림
                    folderAdapter.showCheckboxes()
                    binding.bottomAppbar.visibility = View.GONE
                    true
                }

                else -> false
            }
        }
    }

    // 메뉴와 layout 연결
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.folder_toolbar_menu, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val states = folderAdapter.getCheckboxStates().toMutableList()
        folderViewModel.updateCheckboxStates(states)  // Checkbox check상태 확인
        folderViewModel.setCheckboxVisibility(folderAdapter.showCheckBoxes)     // checkbox 보이기/숨기기 확인
        // BottomAppbar 상태 유지
        outState.putBoolean(
            "BOTTOM_APPBAR_VISIBLE",
            binding.bottomAppbar.visibility == View.VISIBLE
        )
    }

    // BottomAppBar View 관리
    private fun toggleBottomAppBarVisibility() {
        folderAdapter.setOnCheckboxChangedListener(object :
            FolderListAdapter.OnFolderCheckboxChangedListener {
            override fun onFolderCheckboxChanged(selectedCount: Int) {
                Log.d("check", "Selected count: $selectedCount")
                // 선택된 항목이 0보다 많을 경우에만 bottomAppBar 보이게 하기
                if (selectedCount > 0) {
                    binding.bottomAppbar.visibility = View.VISIBLE
                } else {
                    binding.bottomAppbar.visibility = View.GONE
                }
            }
        })
    }

    // Menu 에서 선택했을 때
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.editFolder -> {
                folderViewModel.resetCheckboxStates()
                folderAdapter.showCheckboxes()
                toggleBottomAppBarVisibility()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}