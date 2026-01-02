package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.RunRepo

class RunVmFactory(
    private val repo: RunRepo,
    private val condition_def_repo: ConditionDefinitionRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RunViewModel(
                repo = repo,
                condition_def_repo = condition_def_repo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
