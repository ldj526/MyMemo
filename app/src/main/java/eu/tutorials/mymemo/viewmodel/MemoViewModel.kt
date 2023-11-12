package eu.tutorials.mymemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.repository.MemoRepository
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    val memoList: LiveData<List<Memo>> = repository.memoList
    val checkboxStates = MutableLiveData<MutableList<Boolean>>()
    val isCheckboxVisible = MutableLiveData<Boolean>().apply { value = false }  // 체크박스 보이기/숨기기 상태
    val spanCount = MutableLiveData(2)

    fun insert(memo: Memo) = viewModelScope.launch {    // insert 구현이 UI에서 캡슐화된다.
        repository.insert(memo)
    }

    fun delete(memo: List<Memo>) = viewModelScope.launch {
        repository.delete(memo)
    }

    fun update(memo: Memo) = viewModelScope.launch {
        repository.update(memo)
    }

    // checkbox check 상태 유지
    fun updateCheckboxStates(states: MutableList<Boolean>) {
        checkboxStates.value = states
    }

    // checkbox 보이기 / 숨기기 유지
    fun setCheckboxVisibility(visible: Boolean) {
        isCheckboxVisible.value = visible
    }

    // checkbox 초기화
    fun resetCheckboxStates() {
        val resetStates = MutableList(memoList.value?.size ?: 0) { false }
        checkboxStates.value = resetStates
    }

    // Memo에서 FolderId에 따른 List
    fun getMemoListByFolderId(folderId: Int?): LiveData<List<Memo>> {
        return if (folderId == null) {
            memoList
        } else {
            repository.getMemosByFolderId(folderId)
        }
    }
}

class MemoViewModelFactory(private val repository: MemoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            return MemoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}