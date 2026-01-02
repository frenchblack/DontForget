package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dontforget.data.repo.RunRepo

class RunVmFactory(
    private val repo: RunRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RunViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
