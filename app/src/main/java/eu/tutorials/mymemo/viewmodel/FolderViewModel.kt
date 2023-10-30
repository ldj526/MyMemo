package eu.tutorials.mymemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.repository.FolderRepository
import kotlinx.coroutines.launch

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

    val folderList: LiveData<List<Folder>> = repository.folderList

    fun insert(folder: Folder) = viewModelScope.launch {    // insert 구현이 UI에서 캡슐화된다.
        repository.insert(folder)
    }

    fun delete(folder: List<Folder>) = viewModelScope.launch {
        repository.delete(folder)
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