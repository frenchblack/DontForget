package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.dao.RunDateAggRow
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.repo.HistoryRepo
import com.example.dontforget.data.repo.HistorySessionBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class HistoryViewModel(
    private val repo: HistoryRepo
) : ViewModel() {

    private val _date_rows = MutableStateFlow<List<RunDateAggRow>>(emptyList())
    val date_rows: StateFlow<List<RunDateAggRow>> = _date_rows

    private val _selected_date = MutableStateFlow<String?>(null)
    val selected_date: StateFlow<String?> = _selected_date

    private val _sessions = MutableStateFlow<List<RunSessionEntity>>(emptyList())
    val sessions: StateFlow<List<RunSessionEntity>> = _sessions

    private val _selected_bundle = MutableStateFlow<HistorySessionBundle?>(null)
    val selected_bundle: StateFlow<HistorySessionBundle?> = _selected_bundle

    private val _condition_name_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val condition_name_map: StateFlow<Map<Long, String>> = _condition_name_map

    private val _result_name_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val result_name_map: StateFlow<Map<Long, String>> = _result_name_map

    fun load_recent_dates(days: Int = 30) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val from = now - days.toLong() * 24L * 60L * 60L * 1000L

            _date_rows.value = repo.get_date_aggs(from, now + 1L)

            val cond_defs = repo.get_condition_defs()
            _condition_name_map.value = cond_defs.associate { it.condition_def_id to it.name }

            val res_defs = repo.get_result_defs()
            _result_name_map.value = res_defs.associate { it.result_def_id to it.name }
        }
    }

    fun select_date(date_yyyy_mm_dd: String) {
        _selected_date.value = date_yyyy_mm_dd
        _selected_bundle.value = null

        viewModelScope.launch {
            val (start_ms, end_ms) = day_range_ms(date_yyyy_mm_dd)
            _sessions.value = repo.get_sessions_by_range(start_ms, end_ms)
        }
    }

    fun clear_date() {
        _selected_date.value = null
        _sessions.value = emptyList()
        _selected_bundle.value = null
    }

    fun select_session(session_id: Long) {
        viewModelScope.launch {
            _selected_bundle.value = repo.get_session_bundle(session_id)
        }
    }

    fun clear_session_detail() {
        _selected_bundle.value = null
    }

    private fun day_range_ms(date_yyyy_mm_dd: String): Pair<Long, Long> {
        val parts = date_yyyy_mm_dd.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt() - 1
        val d = parts[2].toInt()

        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m)
        cal.set(Calendar.DAY_OF_MONTH, d)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val start = cal.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L
        return start to end
    }
}

class HistoryVmFactory(
    private val repo: HistoryRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
