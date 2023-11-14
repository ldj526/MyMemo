package eu.tutorials.mymemo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.repository.MemoRepository
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    val memoList: LiveData<List<Memo>> = repository.memoList

    private val _checkboxStates = MutableLiveData<MutableList<Boolean>>()
    val checkboxStates: MutableLiveData<MutableList<Boolean>> = _checkboxStates

    private val _checkboxVisibility = MutableLiveData<Boolean>()
    val checkboxVisibility: LiveData<Boolean> = _checkboxVisibility

    // 비동기 작업 결과를 알릴 LiveData
    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    val spanCount = MutableLiveData(2)

    private val _currentFolderId = MutableLiveData<Int?>()

    // 현재 선택된 폴더 ID에 따라 필터링된 메모 목록
    val filteredMemos: LiveData<List<Memo>> = this._currentFolderId.switchMap { folderId ->
        if (folderId == -1) {
            memoList // 모든 메모를 가져옴
        } else {
            repository.getMemosByFolderId(folderId!!) // 특정 폴더 ID에 해당하는 메모만 가져옴
        }
    }

    init {
        // 앱 시작 시 전체목록을 보여주기 위해 초기값 설정
        setCurrentFolderId(-1)
    }

    // 선택한 메모들의 folderId 변경
    fun updateMemoFolder(selectedMemos: List<Memo>, newFolderId: Int?, isChecked: Boolean) = viewModelScope.launch {
        // 선택한 메모들의 folderId를 새로운 folderId로 설정하고 업데이트
        val updatedMemos = selectedMemos.map { it.copy(folderId = newFolderId, isChecked = false) }
        repository.updateMemos(updatedMemos)
        Log.d("check", "updateMemoFolder")
        _updateResult.value = true
    }

    // updateResult 값 초기화
    fun resetUpdateResult() {
        _updateResult.value = false
    }

    // 현재 선택된 폴더 ID를 업데이트하는 함수
    fun setCurrentFolderId(folderId: Int?) {
        _currentFolderId.value = folderId
    }

    fun insert(memo: Memo) = viewModelScope.launch {    // insert 구현이 UI에서 캡슐화된다.
        repository.insert(memo)
    }

    fun delete(memo: List<Memo>) = viewModelScope.launch {
        repository.delete(memo)
    }

    fun update(memo: Memo) = viewModelScope.launch {
        repository.update(memo)
    }

    // checkbox 보이기 / 숨기기 유지
    fun setCheckboxVisibility(visible: Boolean) {
        _checkboxVisibility.value = visible
    }

    // checkbox 초기화
    fun resetCheckboxStates() {
        val resetStates = MutableList(memoList.value?.size ?: 0) { false }
        Log.d("check", "resetCheckboxStates()에서 $resetStates")
        _checkboxStates.value = resetStates
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