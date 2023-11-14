package eu.tutorials.mymemo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    // FAB, BottomAppBar, SearchIcon의 가시성을 관리하는 LiveData
    private val _isCheckboxVisible = MutableLiveData<Boolean>()
    val isCheckboxVisible: LiveData<Boolean> = _isCheckboxVisible

    // Checkbox의 가시성을 업데이트하는 함수
    fun setCheckboxVisibility(isVisible: Boolean) {
        _isCheckboxVisible.value = isVisible
        Log.d("check", "setCheckboxVisibility")
    }
}
