package eu.tutorials.mymemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private val adapter = MemoListAdapter()
    private var searchIcon: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedInstanceState?.let {
            // 화면 회전 시 BottomAppBar 유지시키기 위함
            val isBottomAppBarVisible = it.getBoolean("BOTTOM_APPBAR_VISIBLE")
            binding.bottomAppbar.visibility = if (isBottomAppBarVisible) View.VISIBLE else View.GONE
            // 화면 회전 시 Floating action button 유지시키기 위함
            val isFabVisible = it.getBoolean("FAB_VISIBLE")
            binding.fab.visibility = if (isFabVisible) View.GONE else View.VISIBLE
        }

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // toolbar의 title 제거
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = GridLayoutManager(this, 2)

        // drawerLayout 왼쪽에서 열기
        binding.drawerMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // getAlphabetizedWords에 의해 반환된 LiveData에 관찰자를 추가합니다.
        // onChanged() 메서드는 관찰되는 데이터가 변경되고 액티비티가
        // 포그라운드에 있을 때 실행됩니다.
        memoViewModel.memoList.observe(this, Observer { memos ->
            // 어댑터 내의 단어들의 캐시된 복사본을 업데이트합니다.
            memos?.let { adapter.setMemo(it) }
        })

        // checkbox check 상태 관찰
        memoViewModel.checkboxStates.observe(this, Observer { states ->
            adapter.updateCheckboxStates(states)
        })

        // checkbox 보이기 / 숨기기 관찰
        memoViewModel.isCheckboxVisible.observe(this, Observer { isVisible ->
            adapter.setCheckboxVisibility(isVisible)

            searchIcon?.isVisible = !isVisible
        })

        // 격자로 보는 방식 관찰
        memoViewModel.spanCount.observe(this, Observer { spanCount ->
            val layoutManager = binding.recyclerview.layoutManager as? GridLayoutManager
            layoutManager?.spanCount = spanCount
        })

        adapter.onItemLongClicked = {
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
                    val selectedItems = adapter.getSelectedItems() // 선택된 항목들 가져오기
                    memoViewModel.delete(selectedItems) // ViewModel에서 삭제 로직 호출
                    adapter.notifyDataSetChanged() // Adapter에 데이터 변경 알림
                    adapter.showCheckboxes()
                    binding.bottomAppbar.visibility = View.GONE
                    binding.fab.visibility = View.VISIBLE
                    searchIcon?.isVisible = true
                    true
                }

                else -> false
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val states = adapter.getCheckboxStates().toMutableList()
        memoViewModel.updateCheckboxStates(states)  // Checkbox check상태 확인
        memoViewModel.setCheckboxVisibility(adapter.showCheckBoxes)     // checkbox 보이기/숨기기 확인
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
        adapter.setOnCheckboxChangedListener(object : MemoListAdapter.OnCheckboxChangedListener {
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
                    adapter.filterList(newText)
                }
                return true
            }
        })
        return true
    }

    // Menu 에서 선택했을 때
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 편집 버튼 눌렀을 시
            R.id.editIcon -> {
                memoViewModel.resetCheckboxStates()
                adapter.showCheckboxes()
                toggleFabVisibility()
                true
            }

            R.id.twoGrid -> {
                changeSpanCount(2)
                true
            }

            R.id.threeGrid -> {
                changeSpanCount(3)
                true
            }

            R.id.fourGrid -> {
                changeSpanCount(4)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // gridLayout에서 spanCount 변경
    private fun changeSpanCount(spanCount: Int) {
        val layoutManager = binding.recyclerview.layoutManager as? GridLayoutManager
        layoutManager?.spanCount = spanCount
        adapter.notifyDataSetChanged()
        memoViewModel.spanCount.value = spanCount
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}