package eu.tutorials.mymemo.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import eu.tutorials.mymemo.R

class FolderListAdapter(
    private val context: Context,
    private val parentList: MutableList<String>,
    private val childList: MutableList<MutableList<String>>
) : BaseExpandableListAdapter() {
    override fun getGroupCount() = parentList.size

    override fun getChildrenCount(groupPosition: Int) = childList[groupPosition].size

    override fun getGroup(groupPosition: Int) = parentList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int) =
        childList[groupPosition][childPosition]

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
        parentCategory.text = parentList[groupPosition]
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
        childCategory.text = getChild(groupPosition, childPosition) as String
        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}