package eu.tutorials.mymemo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.fragment.MemoFragment
import eu.tutorials.mymemo.utils.BottomAppBarHost

class NewMemoActivity : AppCompatActivity(), BottomAppBarHost {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)

        val memoFragment = MemoFragment.newInstance(false, null)

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, memoFragment)
            .commit()
    }


    fun onStyleClicked(view: View) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MemoFragment
        fragment?.styleClicked(view)
    }

    fun onTextAlignClicked(view: View) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MemoFragment
        fragment?.textAlignClicked(view)
    }

    fun onPaintClicked(view: View) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? MemoFragment
        fragment?.paintClicked(view)
    }

    override fun setBottomAppBar(toolbar: BottomAppBar) {
        setSupportActionBar(toolbar)
    }
}