package eu.tutorials.mymemo.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Memo

class MemoListAdapter :
    RecyclerView.Adapter<MemoListAdapter.MemoViewHolder>() {

    private var memoList = ArrayList<Memo>()
    private var fullList = ArrayList<Memo>()
    var showCheckBoxes = false
    var onItemLongClicked: (() -> Unit)? = null
    private var checkboxChangedListener: OnCheckboxChangedListener? = null

    interface OnCheckboxChangedListener {
        fun onCheckboxChanged(selectedCount: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        return MemoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: MemoViewHolder,
        position: Int
    ) {  // onCreateViewHolder와 MemoViewHolder를 바인딩한다.
        val current = memoList[position]
        holder.bind(current)
    }

    inner class MemoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val content: TextView = itemView.findViewById(R.id.tv_content)

        fun bind(memo: Memo) {
            title.text = memo.title
            content.text = memo.content
            checkBox.isChecked = memo.isChecked
            checkBox.visibility =
                if (showCheckBoxes) View.VISIBLE else View.GONE
            // Checkbox 상태 변화 check
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                memo.isChecked = isChecked
                // checkbox에 check된 개수
                val selectedCount = memoList.count { it.isChecked }
                checkboxChangedListener?.onCheckboxChanged(selectedCount)
            }
            itemView.setOnLongClickListener {
                onItemLongClicked?.invoke()
                showCheckboxes()
                true
            }
        }
    }

    // checkbox 보이기/숨기기 상태
    fun setCheckboxVisibility(isVisible: Boolean) {
        showCheckBoxes = isVisible
        notifyDataSetChanged()
    }

    // memoList의 상태를 함수화
    fun getCheckboxStates(): List<Boolean> {
        return memoList.map { it.isChecked }
    }

    // checkbox check 상태 확인
    fun updateCheckboxStates(states: MutableList<Boolean>){
        for (i in memoList.indices) {
            memoList[i].isChecked = states[i]
        }
        notifyDataSetChanged()
    }

    fun setOnCheckboxChangedListener(listener: OnCheckboxChangedListener) {
        this.checkboxChangedListener = listener
    }

    // 검색할 때 filter하는 함수
    fun filterList(search: String) {
        memoList.clear()
        for (item in fullList) {
            if (item.title?.lowercase()?.contains(search.lowercase()) == true ||
                item.content?.lowercase()?.contains(search.lowercase()) == true
            ) {
                memoList.add(item)
            }
        }
        notifyDataSetChanged()
    }

    fun setMemo(memos: List<Memo>) {
        fullList.clear()
        fullList.addAll(memos)

        memoList.clear()
        memoList.addAll(fullList)
        notifyDataSetChanged()
    }

    // checkbox 보여주는 기능
    fun showCheckboxes() {
        showCheckBoxes = !showCheckBoxes
        notifyDataSetChanged()
    }

    // check 된 항목들을 return 해줌
    fun getSelectedItems(): List<Memo> {
        return memoList.filter { it.isChecked }
    }

    override fun getItemCount(): Int = memoList.size
}