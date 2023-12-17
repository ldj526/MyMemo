package eu.tutorials.mymemo.textattribute

import android.content.Context
import android.util.AttributeSet

class CustomEditText(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet) {

    var selectionChangedListener: (() -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        selectionChangedListener?.invoke()
    }
}