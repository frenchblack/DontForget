package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.repo.ConditionDefManageRepo
import com.example.dontforget.data.repo.ResultDefManageRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConditionDefManageViewModel(
    private val repo: ConditionDefManageRepo
) : ViewModel() {

    val list: StateFlow<List<ConditionDefinitionEntity>> =
        repo.observe_all_ordered().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, input_type: InputType, is_active: Int, sort_order: Int) {
        viewModelScope.launch {
            repo.create(name, input_type, is_active, sort_order)
        }
    }

    fun update_basic(id: Long, name: String, is_active: Int, sort_order: Int) {
        viewModelScope.launch {
            repo.update_basic(id, name, is_active, sort_order)
        }
    }

    fun deactivate(id: Long) {
        viewModelScope.launch {
            repo.deactivate(id)
        }
    }
}

class ResultDefManageViewModel(
    private val repo: ResultDefManageRepo
) : ViewModel() {

    val list: StateFlow<List<ResultDefinitionEntity>> =
        repo.observe_all_ordered().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, input_type: InputType, is_active: Int, sort_order: Int) {
        viewModelScope.launch {
            repo.create(name, input_type, is_active, sort_order)
        }
    }

    fun update_basic(id: Long, name: String, is_active: Int, sort_order: Int) {
        viewModelScope.launch {
            repo.update_basic(id, name, is_active, sort_order)
        }
    }

    fun deactivate(id: Long) {
        viewModelScope.launch {
            repo.deactivate(id)
        }
    }
}

class ConditionDefManageVmFactory(
    private val repo: ConditionDefManageRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConditionDefManageViewModel(repo) as T
    }
}

class ResultDefManageVmFactory(
    private val repo: ResultDefManageRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ResultDefManageViewModel(repo) as T
    }
}
