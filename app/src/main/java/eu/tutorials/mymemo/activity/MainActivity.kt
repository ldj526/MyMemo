package eu.tutorials.mymemo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.navigation.NavigationView
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.databinding.ActivityMainBinding
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.utils.FolderListDialogFragment
import eu.tutorials.mymemo.viewmodel.FolderViewModel
import eu.tutorials.mymemo.viewmodel.FolderViewModelFactory

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private val folderViewModel: FolderViewModel by viewModels() {
        FolderViewModelFactory((application as MemosApplication).folderRepository)
    }
    private val memoAdapter = MemoListAdapter()
    private val folderAdapter = FolderListAdapter(this)
    private var searchIcon: MenuItem? = null

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {    // 뒤로가기 클릭 시
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawers()
            }
        }
    }

    private var folderId: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)    // 뒤로가기 클릭 시

        savedInstanceState?.let {
            // 화면 회전 시 BottomAppBar 유지시키기 위함
            val isBottomAppBarVisible = it.getBoolean("BOTTOM_APPBAR_VISIBLE")
            binding.bottomAppbar.visibility = if (isBottomAppBarVisible) View.VISIBLE else View.GONE
            // 화면 회전 시 Floating action button 유지시키기 위함
            val isFabVisible = it.getBoolean("FAB_VISIBLE")
            binding.fab.visibility = if (isFabVisible) View.GONE else View.VISIBLE
        }

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayShowTitleEnabled(false) // toolbar의 title 제거
        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.expandableListView.setAdapter(folderAdapter)

        binding.recyclerview.adapter = memoAdapter
        binding.recyclerview.layoutManager = GridLayoutManager(this, 2)

        // folderlist 가져오기
        folderViewModel.folderList.observe(this, Observer { folders ->
            folders?.let { folderAdapter.setFolder(Folder(null, "폴더", 0), it) }
            // expandableListView가 펼쳐진 상태로 보이게 하기
            binding.expandableListView.expandGroup(0)
        })

        // Memo 목록 가져오기
        memoViewModel.filteredMemos.observe(this, Observer { memos ->
            memos?.let { memoAdapter.setMemo(it) }
        })
        Log.d("Check", "main에서 목록 folderId: $folderId")

        // checkbox check 상태 관찰
        memoViewModel.checkboxStates.observe(this, Observer { states ->
            memoAdapter.updateCheckboxStates(states)
        })

        // checkbox 보이기 / 숨기기 관찰
        memoViewModel.isCheckboxVisible.observe(this, Observer { isVisible ->
            memoAdapter.setCheckboxVisibility(isVisible)

            searchIcon?.isVisible = !isVisible
        })

        // 격자로 보는 방식 관찰
        memoViewModel.spanCount.observe(this, Observer { spanCount ->
            val layoutManager = binding.recyclerview.layoutManager as? GridLayoutManager
            layoutManager?.spanCount = spanCount
        })

        memoAdapter.onItemLongClicked = {
            toggleBottomAppBarVisibility()
            toggleFabVisibility()
            memoViewModel.resetCheckboxStates()
            searchIcon?.isVisible = false
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMemoActivity::class.java)
            startActivity(intent)
        }

        binding.bottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // BottomAppBar에서 Delete를 눌렀을 때
                R.id.deleteIcon -> {
                    val selectedItems = memoAdapter.getSelectedItems() // 선택된 항목들 가져오기
                    memoViewModel.delete(selectedItems) // ViewModel에서 삭제 로직 호출
                    memoAdapter.notifyDataSetChanged() // Adapter에 데이터 변경 알림
                    memoAdapter.showCheckboxes()
                    binding.bottomAppbar.visibility = View.GONE
                    binding.fab.visibility = View.VISIBLE
                    searchIcon?.isVisible = true
                    Log.d("Check", "삭제하고 목록 folderId: $folderId")
                    true
                }

                // 메모를 다른 폴더로 이동시키기 위한 버튼
                R.id.moveMemo -> {
                    folderViewModel.folderNames.observe(this, Observer { folderNames ->
                        FolderListDialogFragment("폴더 목록").show(supportFragmentManager, "CustomDialog")
                    })
                    true
                }

                else -> false
            }
        }
        binding.manageFolder.setOnClickListener {
            val intent = Intent(this, FolderListActivity::class.java)
            startActivity(intent)
        }

        // 부모 폴더를 클릭 시
        binding.expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
            binding.drawerLayout.closeDrawers()
            folderId = -1
            sharedPrefFolderId()
            memoViewModel.setCurrentFolderId(folderId)
            Log.d("Check", "부모 폴더 선택하고 목록 folderId: $folderId")
            true
        }

        // 자식 폴더를 클릭 시
        binding.expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedFolder = binding.expandableListView.expandableListAdapter.getChild(
                groupPosition,
                childPosition
            ) as Folder
            folderId = selectedFolder.id
            sharedPrefFolderId()
            binding.drawerLayout.closeDrawers()
            memoViewModel.setCurrentFolderId(folderId)
            Log.d("Check", "폴더 선택하고 목록 folderId: $folderId")
            true
        }
    }

    private fun sharedPrefFolderId() {
        // SharedPreference로 folderId 저장
        val sharedPref = getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("lastSelectedFolderId", folderId!!)
            apply()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val states = memoAdapter.getCheckboxStates().toMutableList()
        memoViewModel.updateCheckboxStates(states)  // Checkbox check상태 확인
        memoViewModel.setCheckboxVisibility(memoAdapter.showCheckBoxes)     // checkbox 보이기/숨기기 확인
        // BottomAppbar 상태 유지
        outState.putBoolean(
            "BOTTOM_APPBAR_VISIBLE",
            binding.bottomAppbar.visibility == View.VISIBLE
        )
        // Floating action button 상태 유지
        outState.putBoolean("FAB_VISIBLE", binding.fab.visibility == View.GONE)
    }

    // Floating action button view 관리
    private fun toggleFabVisibility() {
        if (binding.fab.visibility == View.GONE) {
            binding.fab.visibility = View.VISIBLE
            searchIcon?.isVisible = true
        } else {
            binding.fab.visibility = View.GONE
            searchIcon?.isVisible = false
        }
    }

    // BottomAppBar View 관리
    private fun toggleBottomAppBarVisibility() {
        memoAdapter.setOnCheckboxChangedListener(object :
            MemoListAdapter.OnCheckboxChangedListener {
            override fun onCheckboxChanged(selectedCount: Int) {
                // 선택된 항목이 0보다 많을 경우에만 bottomAppBar 보이게 하기
                if (selectedCount > 0) {
                    binding.bottomAppbar.visibility = View.VISIBLE
                } else {
                    binding.bottomAppbar.visibility = View.GONE
                }
            }
        })
    }

    // 메뉴와 layout 연결
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        searchIcon = menu?.findItem(R.id.searchIcon)
        val searchView = searchIcon?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // 검색 버튼 입력 시 호출
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            // 텍스트 입력 or 수정 시에 호출
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    memoAdapter.filterList(newText)
                }
                return true
            }
        })
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.folder -> Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_LONG).show()

        }
        return false
    }

    // Menu 에서 선택했을 때
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 편집 버튼 눌렀을 시
            R.id.editIcon -> {
                memoViewModel.resetCheckboxStates()
                memoAdapter.showCheckboxes()
                toggleBottomAppBarVisibility()
                toggleFabVisibility()
            }

            R.id.twoGrid -> {
                changeSpanCount(2)
            }

            R.id.threeGrid -> {
                changeSpanCount(3)
            }

            R.id.fourGrid -> {
                changeSpanCount(4)
            }

            R.id.addFolder -> {
                // 다이얼로그로 새 폴더 이름 입력 받기
                val input = EditText(this)
                val dialog = AlertDialog.Builder(this)
                    .setTitle("새 폴더 이름 입력")
                    .setView(input)
                    .setPositiveButton("추가") { dialog, _ ->
                        val folderName = input.text.toString()
                        if (folderName.isNotEmpty()) {
                            // 새 폴더 데이터 추가
                            val newFolder = Folder(null, folderName, 0)
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

            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    // gridLayout에서 spanCount 변경
    private fun changeSpanCount(spanCount: Int) {
        val layoutManager = binding.recyclerview.layoutManager as? GridLayoutManager
        layoutManager?.spanCount = spanCount
        memoAdapter.notifyDataSetChanged()
        memoViewModel.spanCount.value = spanCount
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}