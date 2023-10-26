package eu.tutorials.mymemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.repository.MemoRepository
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    val memoList: LiveData<List<Memo>> = repository.memoList

    fun insert(memo: Memo) = viewModelScope.launch {    // insert 구현이 UI에서 캡슐화된다.
        repository.insert(memo)
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