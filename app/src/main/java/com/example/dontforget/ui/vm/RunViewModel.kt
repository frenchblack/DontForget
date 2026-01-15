package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.RunRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RunViewModel(
    private val repo: RunRepo,
    private val condition_def_repo: ConditionDefinitionRepo

) : ViewModel() {

    // ✅ 컨디션 정의 목록(DB 보고 자동으로 뜨게 할 데이터)
    val condition_defs = condition_def_repo.observe_active()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    private val _start_value_code_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val start_value_code_map = _start_value_code_map.asStateFlow()

    private val _start_value_text_map = MutableStateFlow<Map<Long, String>>(emptyMap())
    val start_value_text_map = _start_value_text_map.asStateFlow()

    private val _in_progress = MutableStateFlow<RunSessionEntity?>(null)
    val in_progress: StateFlow<RunSessionEntity?> = _in_progress

    private val _current_session_id = MutableStateFlow<Long?>(null)
    val current_session_id: StateFlow<Long?> = _current_session_id

    private val _step = MutableStateFlow(RunStep.HOME)
    val step: StateFlow<RunStep> = _step

    private val _current_started_at = MutableStateFlow<Long?>(null)
    val current_started_at: StateFlow<Long?> = _current_started_at

    fun refresh_in_progress() {
        viewModelScope.launch {
            val s = repo.get_in_progress()

            if (s == null) {
                _in_progress.value = null
                return@launch
            }

            val now = System.currentTimeMillis()
            val limit_ms = 24L * 60L * 60L * 1000L // ✅ 24시간
            val ok = (now - s.start_time) <= limit_ms

            if (ok) {
                _in_progress.value = s
            } else {
                // ✅ 24시간 지난 진행중 세션은 이어하기 대상에서 제외 + DB도 정리
                repo.abandon_session(s.session_id)
                _in_progress.value = null
            }
        }
    }

    fun start_new() {
        viewModelScope.launch {
            val new_id = repo.start_new_session()
            _current_session_id.value = new_id
            _in_progress.value = null

            _current_started_at.value = System.currentTimeMillis() // ✅ 추가

            clear_condition_start_cache()   // ✅ 새로하기면 값 비움
            _step.value = RunStep.CONDITION_START
        }
    }

    fun resume_existing(session_id: Long) {
        _current_session_id.value = session_id

        _current_started_at.value = System.currentTimeMillis() // ✅ 추가(이어하기 시작 시점 기준)

        load_condition_start(session_id)   // ✅ 이어하기면 DB값 로드
        _step.value = RunStep.CONDITION_START
    }

    fun go_condition_start() {
        _step.value = RunStep.CONDITION_START
    }

    fun go_home() {
        _step.value = RunStep.HOME
    }

    fun finish_current() {
        val sid = _current_session_id.value ?: return
        viewModelScope.launch {
            repo.finish_session(sid)
            _current_session_id.value = null
            _current_started_at.value = null // ✅ 추가
            refresh_in_progress()
        }
    }

    fun finish_current_go_summary() {
        val sid = _current_session_id.value ?: return
        viewModelScope.launch {

            // ✅ 여기 한 줄이 핵심: 적용완료 목록을 run_item.success로 반영
            repo.apply_success_from_progress(sid)

            repo.finish_session(sid)
            _step.value = RunStep.FINISH_SUMMARY
            // sessionId는 유지 (요약 화면에서 필요)
        }
    }

    fun save_condition_start(
        session_id: Long,
        value_code_map: Map<Long, String>,
        value_text_map: Map<Long, String>,
        on_done: () -> Unit
    ) {
        viewModelScope.launch {

            // condition_defs 기반으로 “정의된 항목들만” 저장
            val defs = condition_defs.value

            val items = defs.map { def ->
                RunConditionEntity(
                    session_id = session_id,
                    condition_def_id = def.condition_def_id,
                    value_code = value_code_map[def.condition_def_id] ?: "",
                    value = value_text_map[def.condition_def_id] ?: "",
                    phase = ConditionPhase.START
                )
            }.filter { it.value_code.isNotBlank() || it.value.isNotBlank() } // ✅ 둘 다 빈 값이면 저장 안 함

            repo.save_conditions_start(
                session_id = session_id,
                items = items
            )
            _start_value_code_map.value = items.associate { it.condition_def_id to it.value_code }
            _start_value_text_map.value = items.associate { it.condition_def_id to it.value }

            _step.value = RunStep.RUN_ITEMS // ✅ 저장 끝나면 다음 화면
            on_done()
        }
    }

    fun load_condition_start(session_id: Long) {
        viewModelScope.launch {
            val rows = repo.get_conditions_start(session_id)

            _start_value_code_map.value = rows.associate { it.condition_def_id to it.value_code }
            _start_value_text_map.value = rows.associate { it.condition_def_id to it.value }
        }
    }

    private fun clear_condition_start_cache() {
        _start_value_code_map.value = emptyMap()
        _start_value_text_map.value = emptyMap()
    }

    fun go_run_items() {
        _step.value = RunStep.RUN_ITEMS
    }

    fun go_today_summary() {
        _step.value = RunStep.TODAY_SUMMARY
    }

    fun mark_cancel(session_id: Long, item_id: Long) = viewModelScope.launch {
        repo.mark_cancel(session_id, item_id)
    }

    fun mark_fail(session_id: Long, item_id: Long) = viewModelScope.launch {
        repo.mark_fail(session_id, item_id)
    }

    fun clear_one(session_id: Long, item_id: Long) = viewModelScope.launch {
        repo.clear_one(session_id, item_id)
    }

    fun append_condition_mid(
        session_id: Long,
        value_code_map: Map<Long, String>,
        value_text_map: Map<Long, String>,
        on_done: () -> Unit
    ) {
        viewModelScope.launch {
            val defs = condition_defs.value

            val items = defs.map { def ->
                RunConditionEntity(
                    session_id = session_id,
                    condition_def_id = def.condition_def_id,
                    value_code = value_code_map[def.condition_def_id] ?: "",
                    value = value_text_map[def.condition_def_id] ?: "",
                    phase = ConditionPhase.MID,
                    created_at = System.currentTimeMillis()
                )
            }.filter { it.value_code.isNotBlank() || it.value.isNotBlank() }

            repo.append_conditions_mid(session_id, items)
            on_done()
        }
    }

    enum class RunStep {
        HOME,
        CONDITION_START ,
        RUN_ITEMS, // ✅ 추가: 체크리스트 진행 화면,
        FINISH_SUMMARY,   // ✅ 연습 종료 요약 화면
        TODAY_SUMMARY
    }
}
