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
    private var showCheckBoxes = false

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
            itemView.setOnLongClickListener {
                showCheckBoxes = !showCheckBoxes
                notifyDataSetChanged()
                true
            }
        }
    }

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

    override fun getItemCount(): Int = memoList.size


}