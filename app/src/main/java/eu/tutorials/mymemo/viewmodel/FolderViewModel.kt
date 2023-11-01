package eu.tutorials.mymemo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.repository.FolderRepository
import kotlinx.coroutines.launch

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

    val folderList: LiveData<List<Folder>> = repository.folderList
    val checkboxStates = MutableLiveData<MutableList<Boolean>>()
    val isCheckboxVisible = MutableLiveData<Boolean>().apply { value = false }  // 체크박스 보이기/숨기기 상태

    fun insert(folder: Folder) = viewModelScope.launch {    // insert 구현이 UI에서 캡슐화된다.
        repository.insert(folder)
    }

    fun delete(folder: List<Folder>) = viewModelScope.launch {
        repository.delete(folder)
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
        val resetStates = MutableList(folderList.value?.size ?: 0) { false }
        checkboxStates.value = resetStates
    }
}

class FolderViewModelFactory(private val repository: FolderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
            return FolderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}