package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.ResultRepo
import com.example.dontforget.data.repo.RunRepo

class TodaySummaryVmFactory(
    private val run_repo: RunRepo,
    private val condition_def_repo: ConditionDefinitionRepo,
    private val result_repo: ResultRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodaySummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodaySummaryViewModel(run_repo, condition_def_repo, result_repo) as T
        }
        throw IllegalArgumentException("Unknown VM: ${modelClass.name}")
    }
}
