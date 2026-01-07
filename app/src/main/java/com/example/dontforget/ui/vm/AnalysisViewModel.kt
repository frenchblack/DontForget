package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.analysis.AnalysisRange
import com.example.dontforget.data.analysis.AnalysisReport
import com.example.dontforget.data.repo.AnalysisRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val repo: AnalysisRepo
) : ViewModel() {

    private val _range = MutableStateFlow(AnalysisRange.D30)
    val range: StateFlow<AnalysisRange> = _range

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun set_range(r: AnalysisRange) {
        _range.value = r
    }

    fun build(on_done: (AnalysisReport) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val rep = repo.build_report(_range.value)
                on_done(rep)
            } finally {
                _loading.value = false
            }
        }
    }
}

class AnalysisVmFactory(
    private val repo: AnalysisRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalysisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalysisViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
