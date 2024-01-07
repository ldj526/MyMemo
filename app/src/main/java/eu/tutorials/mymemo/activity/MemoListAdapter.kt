package eu.tutorials.mymemo.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.text.Spannable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.utils.convertTimestampToDate

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

        val id: TextView = itemView.findViewById(R.id.id)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val content: TextView = itemView.findViewById(R.id.tv_content)
        val date: TextView = itemView.findViewById(R.id.date)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(memo: Memo) {
            id.text = memo.id.toString()
            title.text = memo.title
            val loadedSpannable = deserializeSpannable(memo.content!!)
            content.setText(loadedSpannable, TextView.BufferType.SPANNABLE)
            date.text = convertTimestampToDate(memo.date!!)
            checkBox.isChecked = memo.isChecked
            val bitmap = loadBitmapFromInternalStorage(memo.imagePath!!)
            // 저장되어 있는 drawing을 imageView에서 보여줌
            bitmap?.let {
                imageView.setImageBitmap(it)
            }
            checkBox.visibility =
                if (showCheckBoxes) View.VISIBLE else View.GONE
            // Checkbox 상태 변화 check
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                memo.isChecked = isChecked
                // checkbox에 check된 개수
                val selectedCount = memoList.count { it.isChecked }
                checkboxChangedListener?.onCheckboxChanged(selectedCount)
            }
            itemView.setOnClickListener {
                val intent = Intent(it.context, EditMemoActivity::class.java)
                intent.putExtra("currentMemo", memo)
                it.context.startActivity(intent)
            }
            itemView.setOnLongClickListener {
                onItemLongClicked?.invoke()
                showCheckboxes()
                true
            }
        }
    }

    // 저장된 데이터 로드 및 역직렬화
    private fun deserializeSpannable(spannableBytes: ByteArray): Spannable {
        val parcel = Parcel.obtain()
        parcel.unmarshall(spannableBytes, 0, spannableBytes.size)
        parcel.setDataPosition(0)
        val spannable = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel) as Spannable
        parcel.recycle()
        return spannable
    }

    private fun loadBitmapFromInternalStorage(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // checkbox 보이기/숨기기 상태
    fun setCheckboxVisibility(isVisible: Boolean) {
        showCheckBoxes = isVisible
        notifyDataSetChanged()
    }

    // checkbox check 상태 확인
    fun updateCheckboxStates(states: MutableList<Boolean>) {
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
                item.content?.toString()!!.lowercase().contains(search.lowercase()) == true
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