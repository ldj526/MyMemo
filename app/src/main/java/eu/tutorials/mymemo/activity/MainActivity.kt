package eu.tutorials.mymemo.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Memo

class MainActivity : AppCompatActivity() {

    private val memoViewModel: MemoViewModel by viewModels() {
        MemoViewModelFactory((application as MemosApplication).repository)
    }
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val adapter = MemoListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)

        // getAlphabetizedWords에 의해 반환된 LiveData에 관찰자를 추가합니다.
        // onChanged() 메서드는 관찰되는 데이터가 변경되고 액티비티가
        // 포그라운드에 있을 때 실행됩니다.
        memoViewModel.memoList.observe(this, Observer { memos ->
            // 어댑터 내의 단어들의 캐시된 복사본을 업데이트합니다.
            memos?.let { adapter.setMemo(it) }
        })

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val memo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getSerializableExtra(
                            NewMemoActivity.EXTRA_REPLY,
                            Memo::class.java
                        )
                    } else {
                        result.data?.getSerializableExtra(NewMemoActivity.EXTRA_REPLY) as Memo?
                    }
                    if (memo != null) {
                        memoViewModel.insert(memo)
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.empty_not_saved,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMemoActivity::class.java)
            launcher.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu?.findItem(R.id.searchIcon)
        val searchView = searchItem?.actionView as SearchView

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
}