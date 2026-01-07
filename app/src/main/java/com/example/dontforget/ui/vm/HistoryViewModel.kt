package com.example.dontforget.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dontforget.data.analysis.AnalysisRange
import com.example.dontforget.data.analysis.AnalysisReport
import com.example.dontforget.data.dao.ConditionStatRow
import com.example.dontforget.data.dao.ItemDateAggRow
import com.example.dontforget.data.dao.ItemOptionRow
import com.example.dontforget.data.dao.RunDateAggRow
import com.example.dontforget.data.dao.SummaryStatRow
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.repo.AnalysisRepo
import com.example.dontforget.data.repo.HistoryRepo
import com.example.dontforget.data.repo.HistorySessionBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class HistoryViewModel(
    private val repo: HistoryRepo,
    private val analysis_repo: AnalysisRepo
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

    // ✅ 정의 리스트(선택용)
    private val _condition_defs = MutableStateFlow<List<ConditionDefinitionEntity>>(emptyList())
    val condition_defs: StateFlow<List<ConditionDefinitionEntity>> = _condition_defs

    private val _result_defs = MutableStateFlow<List<ResultDefinitionEntity>>(emptyList())
    val result_defs: StateFlow<List<ResultDefinitionEntity>> = _result_defs

    private val _item_options = MutableStateFlow<List<ItemOptionRow>>(emptyList())
    val item_options: StateFlow<List<ItemOptionRow>> = _item_options

    // ✅ 통계 결과
    private val _condition_stat_rows = MutableStateFlow<List<ConditionStatRow>>(emptyList())
    val condition_stat_rows: StateFlow<List<ConditionStatRow>> = _condition_stat_rows

    private val _summary_stat_rows = MutableStateFlow<List<SummaryStatRow>>(emptyList())
    val summary_stat_rows: StateFlow<List<SummaryStatRow>> = _summary_stat_rows

    private val _item_stat_rows = MutableStateFlow<List<ItemDateAggRow>>(emptyList())
    val item_stat_rows: StateFlow<List<ItemDateAggRow>> = _item_stat_rows

    // ✅ 분석 기간 상태
    private val _analysis_range = MutableStateFlow(value = AnalysisRange.D30)
    val analysis_range: StateFlow<AnalysisRange> = _analysis_range

    // ✅ 분석 결과(히스토리 메인에 띄울 내용)
    private val _analysis_report = MutableStateFlow<AnalysisReport?>(value = null)
    val analysis_report: StateFlow<AnalysisReport?> = _analysis_report

    fun set_analysis_range(r: AnalysisRange) {
        _analysis_range.value = r
        load_analysis_report()
    }

    fun load_analysis_report() {
        viewModelScope.launch {
            // ✅ 분석은 AnalysisRepo가 계산(ALL이면 null 처리도 AnalysisRepo에서)
            _analysis_report.value = analysis_repo.build_report(_analysis_range.value)
        }
    }

    fun set_analysis_report(r: AnalysisReport) {
        _analysis_report.value = r
    }

    fun clear_analysis_report() {
        _analysis_report.value = null
    }

    fun load_recent_dates(days: Int = 30) {
        viewModelScope.launch {
            val (from, to) = range_ms(days)

            _date_rows.value = repo.get_date_aggs(from, to)

            val cond_defs = repo.get_condition_defs()
            _condition_name_map.value = cond_defs.associate { it.condition_def_id to it.name }
            _condition_defs.value = cond_defs

            val res_defs = repo.get_result_defs()
            _result_name_map.value = res_defs.associate { it.result_def_id to it.name }
            _result_defs.value = res_defs
        }
    }

    fun load_item_options() {
        viewModelScope.launch {
            _item_options.value = repo.get_item_options()
        }
    }

    fun load_condition_stats(
        condition_def_id: Long,
        days: Int,
        phase: ConditionPhase = ConditionPhase.START
    ) {
        viewModelScope.launch {
            val (from, to) = range_ms(days)
            _condition_stat_rows.value = repo.get_condition_stats(from, to, condition_def_id, phase)
        }
    }

    fun load_summary_stats(result_def_id: Long, days: Int) {
        viewModelScope.launch {
            val (from, to) = range_ms(days)
            _summary_stat_rows.value = repo.get_summary_stats(from, to, result_def_id)
        }
    }

    fun load_item_stats(item_id: Long, days: Int) {
        viewModelScope.launch {
            val (from, to) = range_ms(days)
            _item_stat_rows.value = repo.get_item_date_aggs(from, to, item_id)
        }
    }

    private fun range_ms(days: Int): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val from = now - days.toLong() * 24L * 60L * 60L * 1000L
        val to = now + 1L
        return from to to
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
    private val repo: HistoryRepo,
    private val analysis_repo: AnalysisRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repo, analysis_repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
