package eu.tutorials.mymemo.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import eu.tutorials.mymemo.MemoViewModel
import eu.tutorials.mymemo.MemoViewModelFactory
import eu.tutorials.mymemo.MemosApplication
import eu.tutorials.mymemo.R
import eu.tutorials.mymemo.activity.FolderListAdapter
import eu.tutorials.mymemo.activity.MemoListAdapter
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.viewmodel.FolderViewModel
import eu.tutorials.mymemo.viewmodel.FolderViewModelFactory

class FolderListDialogFragment(private val message: String, val selectedMemos: List<Memo>) : DialogFragment() {

    private val folderViewModel: FolderViewModel by activityViewModels() {
        FolderViewModelFactory((activity?.application as MemosApplication).folderRepository)
    }
    private lateinit var folderAdapter: FolderListAdapter

    private val memoViewModel: MemoViewModel by activityViewModels {
        MemoViewModelFactory((activity?.application as MemosApplication).repository)
    }
    private lateinit var memoAdapter: MemoListAdapter
    private var folderId: Int? = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // MainActivity로부터 folderId 읽기
        val sharedPref = this.activity?.getSharedPreferences("MemosApplication", Context.MODE_PRIVATE)
        folderId = sharedPref?.getInt("lastSelectedFolderId", -1)

        val view = inflater.inflate(R.layout.fragment_folder_list_dialog, container, false)
        val folderListView: ExpandableListView = view.findViewById(R.id.expandableListView)
        folderAdapter = FolderListAdapter(requireContext())
        memoAdapter = MemoListAdapter()
        folderListView.setAdapter(folderAdapter)

        // folderlist 가져오기
        folderViewModel.folderList.observe(this, Observer { folders ->
            folders?.let { folderAdapter.setFolder(Folder(null, "폴더", 0), it) }
            // expandableListView가 펼쳐진 상태로 보이게 하기
            folderListView.expandGroup(0)
        })

        // 자식 폴더를 클릭 시
        folderListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedFolder = folderListView.expandableListAdapter.getChild(
                groupPosition,
                childPosition
            ) as Folder
            val selectedFolderId = selectedFolder.id
            memoViewModel.updateMemoFolder(selectedMemos, selectedFolderId)
            dismiss()
            true
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            // width, height 설정
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        memoViewModel.setCurrentFolderId(folderId)
        memoViewModel.setCheckboxVisibility(false)
        memoViewModel.resetCheckboxStates()
    }
}