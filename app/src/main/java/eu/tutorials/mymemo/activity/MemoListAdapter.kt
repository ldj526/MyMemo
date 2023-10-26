package eu.tutorials.mymemo.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Memo

class MemoListAdapter :
    RecyclerView.Adapter<MemoListAdapter.MemoViewHolder>() {

    private var memoList = ArrayList<Memo>()
    private var fullList = ArrayList<Memo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        return MemoViewHolder.create(parent)
    }

    override fun onBindViewHolder(
        holder: MemoViewHolder,
        position: Int
    ) {  // onCreateViewHolder와 MemoViewHolder를 바인딩한다.
        val current = memoList[position]
        holder.title.text = current.title
        holder.content.text = current.content
    }

    class MemoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.tv_title)
        val content: TextView = itemView.findViewById(R.id.tv_content)

        companion object {
            fun create(parent: ViewGroup): MemoViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return MemoViewHolder(view)
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