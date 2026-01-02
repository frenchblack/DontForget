package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.repo.CheckItemRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemsViewModel(private val repo: CheckItemRepo) : ViewModel() {

    val active = repo.observe_active()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val mastered = repo.observe_mastered()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add_item(title: String, note: String, confidence: Int) = viewModelScope.launch {
        val t = title.trim()
        if (t.isNotEmpty()) repo.add_item(t, note, confidence.coerceIn(0, 5))
    }

    fun add_item(title: String) = viewModelScope.launch {
        val t = title.trim()
        if (t.isNotEmpty()) repo.add_item(t)
    }



    fun to_mastered(item_id: Long) = viewModelScope.launch { repo.to_mastered(item_id) }
    fun to_active(item_id: Long) = viewModelScope.launch { repo.to_active(item_id) }

    fun update_item(item_id: Long, title: String, note: String, confidence: Int) = viewModelScope.launch {
        val t = title.trim()
        if (t.isNotEmpty()) repo.update_item(item_id, t, note, confidence.coerceIn(0, 5))
    }

    fun delete_item(item_id: Long) = viewModelScope.launch {
        repo.delete_item(item_id)
    }

    fun add_mistake(item_id: Long) = viewModelScope.launch {
        repo.add_mistake(item_id)
    }

    fun revert(item_id: Long) = viewModelScope.launch {
        repo.revert(item_id)
    }
}