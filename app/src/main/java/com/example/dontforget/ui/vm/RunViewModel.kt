package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.repo.RunRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunViewModel(
    private val repo: RunRepo
) : ViewModel() {

    // 진행중 세션 (있으면 이어하기/새로하기 판단)
    private val _in_progress = MutableStateFlow<RunSessionEntity?>(null)
    val in_progress: StateFlow<RunSessionEntity?> = _in_progress

    // 현재 세션 id (시작/이어하기 결과)
    private val _current_session_id = MutableStateFlow<Long?>(null)
    val current_session_id: StateFlow<Long?> = _current_session_id

    private val _step = MutableStateFlow(RunStep.HOME)
    val step: StateFlow<RunStep> = _step

    fun refresh_in_progress() {
        viewModelScope.launch {
            _in_progress.value = repo.get_in_progress()
        }
    }

    fun start_new() {
        viewModelScope.launch {
            val new_id = repo.start_new_session()
            _current_session_id.value = new_id
            _in_progress.value = null
            _step.value = RunStep.CONDITION_START
        }
    }

    fun resume_existing(session_id: Long) {
        // 이미 진행중인 세션을 그대로 사용
        _current_session_id.value = session_id
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
            refresh_in_progress()
        }
    }

    enum class RunStep {
        HOME,          // 시작 버튼 있는 화면
        CONDITION_START // 시작 컨디션 입력 화면
    }
}
