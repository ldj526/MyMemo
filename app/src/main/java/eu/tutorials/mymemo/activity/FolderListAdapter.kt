package eu.tutorials.mymemo.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.model.Folder

class FolderListAdapter(private val context: Context) : BaseExpandableListAdapter() {

    private val parentList = mutableListOf<Folder>()
    private val childList = mutableListOf<MutableList<Folder>>()
    var showCheckBoxes = false
    private var checkboxChangedListener: OnCheckboxChangedListener? = null

    interface OnCheckboxChangedListener {
        fun onCheckboxChanged(selectedCount: Int)
    }

    override fun getGroupCount() = parentList.size

    override fun getChildrenCount(groupPosition: Int) = childList[groupPosition].size

    override fun getGroup(groupPosition: Int) = parentList[groupPosition].name

    override fun getChild(groupPosition: Int, childPosition: Int) =
        childList[groupPosition][childPosition].name

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun hasStableIds() = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val parentView = inflater.inflate(R.layout.drawer_parent, parent, false)
        val parentCategory = parentView.findViewById<TextView>(R.id.folderList)
        parentCategory.text = getGroup(groupPosition)
        return parentView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val childView = inflater.inflate(R.layout.drawer_child, parent, false)
        val childCategory = childView.findViewById<TextView>(R.id.folder1)
        val checkBox = childView.findViewById<CheckBox>(R.id.checkBox)
        checkBox.visibility =
            if (showCheckBoxes) View.VISIBLE else View.GONE
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            childList[groupPosition][childPosition].isChecked = isChecked
            // checkbox에 check된 개수
            val selectedCount = childList[0].count { it.isChecked }
            checkboxChangedListener?.onCheckboxChanged(selectedCount)
        }
        childCategory.text = getChild(groupPosition, childPosition)
        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun setOnCheckboxChangedListener(listener: OnCheckboxChangedListener) {
        this.checkboxChangedListener = listener
    }

    // checkbox 보여주는 기능
    fun showCheckboxes() {
        showCheckBoxes = !showCheckBoxes
        notifyDataSetChanged()
    }

    // check 된 항목들을 return 해줌
    fun getSelectedItems(): List<Folder> {
        return childList[0].filter { it.isChecked }
    }

    fun setFolder(parentFolders: Folder, childFolders: List<Folder>) {
        parentList.apply {
            clear()
            add(parentFolders)
        }
        childList.apply {
            clear()
            add(ArrayList(childFolders))
        }
        notifyDataSetChanged()
    }
}