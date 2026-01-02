package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.ResultRepo
import com.example.dontforget.data.repo.RunRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodaySummaryViewModel(
    private val run_repo: RunRepo,
    private val condition_def_repo: ConditionDefinitionRepo,
    private val result_repo: ResultRepo
) : ViewModel() {

    // 종료 후 컨디션 정의
    val condition_defs = condition_def_repo.observe_active()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 오늘정리(자기평가) 정의
    val result_defs = result_repo.observe_active_defs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _end_value_code_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val end_value_code_map: StateFlow<Map<Long, String>> = _end_value_code_map.asStateFlow()

    private val _end_value_text_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val end_value_text_map: StateFlow<Map<Long, String>> = _end_value_text_map.asStateFlow()

    private val _result_value_code_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val result_value_code_map: StateFlow<Map<Long, String>> = _result_value_code_map.asStateFlow()

    private val _result_value_text_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val result_value_text_map: StateFlow<Map<Long, String>> = _result_value_text_map.asStateFlow()

    fun load_end_condition(session_id: Long) {
        viewModelScope.launch {
            val rows = run_repo.get_conditions(session_id, ConditionPhase.END)
            _end_value_code_map.value = rows.associate { it.condition_def_id to it.value_code }
            _end_value_text_map.value = rows.associate { it.condition_def_id to it.value }
        }
    }

    fun load_result_summary(session_id: Long) {
        viewModelScope.launch {
            val rows = result_repo.get_summary(session_id)
            _result_value_code_map.value = rows.associate { it.result_def_id to it.value_code }
            _result_value_text_map.value = rows.associate { it.result_def_id to it.value }
        }
    }

    fun save_all(
        session_id: Long,
        end_code_map: Map<Long, String>,
        end_text_map: Map<Long, String>,
        result_code_map: Map<Long, String>,
        result_text_map: Map<Long, String>,
        on_done: () -> Unit
    ) {
        viewModelScope.launch {
            // 1) 종료 후 컨디션 저장 (phase END)
            val cdefs = condition_defs.value
            val end_items = cdefs.map { def ->
                RunConditionEntity(
                    session_id = session_id,
                    condition_def_id = def.condition_def_id,
                    value_code = end_code_map[def.condition_def_id] ?: "",
                    value = end_text_map[def.condition_def_id] ?: "",
                    phase = ConditionPhase.END
                )
            }.filter { it.value_code.isNotBlank() || it.value.isNotBlank() }

            run_repo.save_conditions(session_id, ConditionPhase.END, end_items)

            // 2) 오늘정리(자기평가) 저장
            val rdefs = result_defs.value
            result_repo.save_summary(
                session_id = session_id,
                value_code_map = result_code_map,
                value_text_map = result_text_map,
                defs = rdefs
            )

            on_done()
        }
    }
}
