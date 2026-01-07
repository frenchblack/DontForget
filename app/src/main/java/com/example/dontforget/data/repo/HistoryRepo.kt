package com.example.dontforget.data.repo

import com.example.dontforget.data.analysis.AnalysisReport
import com.example.dontforget.data.dao.ConditionDefinitionDao
import com.example.dontforget.data.dao.ConditionStatRow
import com.example.dontforget.data.dao.ItemDateAggRow
import com.example.dontforget.data.dao.ItemOptionRow
import com.example.dontforget.data.dao.ResultDefinitionDao
import com.example.dontforget.data.dao.RunConditionDao
import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.dao.RunDateAggRow
import com.example.dontforget.data.dao.RunSummaryDao
import com.example.dontforget.data.dao.SummaryStatRow
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunSummaryEntity

data class HistorySessionBundle(
    val session: RunSessionEntity,
    val items: List<RunItemEntity>,
    val start_conditions: List<RunConditionEntity>,
    val mid_conditions: List<RunConditionEntity>,
    val end_conditions: List<RunConditionEntity>,
    val summaries: List<RunSummaryEntity>
)

class HistoryRepo(
    private val run_dao: RunDao,
    private val run_condition_dao: RunConditionDao,
    private val run_summary_dao: RunSummaryDao,
    private val condition_def_dao: ConditionDefinitionDao,
    private val result_def_dao: ResultDefinitionDao
) {
    suspend fun get_date_aggs(from_ms: Long, to_ms: Long): List<RunDateAggRow> {
        return run_dao.get_date_aggs(from_ms, to_ms)
    }

    suspend fun get_sessions_by_range(day_start_ms: Long, day_end_ms: Long): List<RunSessionEntity> {
        return run_dao.get_sessions_by_range(day_start_ms, day_end_ms)
    }

    suspend fun get_condition_defs(): List<ConditionDefinitionEntity> = condition_def_dao.get_all_active()

    suspend fun get_result_defs(): List<ResultDefinitionEntity> = result_def_dao.get_all_active()

    suspend fun get_session_bundle(session_id: Long): HistorySessionBundle {
        val session = run_dao.get_session_by_id(session_id)
            ?: throw IllegalStateException("session not found: $session_id")

        val items = run_dao.get_run_items_by_session(session_id)

        val start = run_condition_dao.get_by_session_phase(session_id, ConditionPhase.START)
        val mid = run_condition_dao.get_by_session_phase(session_id, ConditionPhase.MID)
        val end = run_condition_dao.get_by_session_phase(session_id, ConditionPhase.END)

        val summaries = run_summary_dao.get_by_session(session_id)

        return HistorySessionBundle(
            session = session,
            items = items,
            start_conditions = start,
            mid_conditions = mid,
            end_conditions = end,
            summaries = summaries
        )
    }

    suspend fun get_item_options(): List<ItemOptionRow> = run_dao.get_item_options()

    suspend fun get_item_date_aggs(from_ms: Long, to_ms: Long, item_id: Long): List<ItemDateAggRow> {
        return run_dao.get_item_date_aggs(from_ms, to_ms, item_id)
    }

    suspend fun get_condition_stats(
        from_ms: Long,
        to_ms: Long,
        condition_def_id: Long,
        phase: ConditionPhase
    ): List<ConditionStatRow> {
        return run_condition_dao.get_condition_stats(from_ms, to_ms, condition_def_id, phase)
    }

    suspend fun get_summary_stats(from_ms: Long, to_ms: Long, result_def_id: Long): List<SummaryStatRow> {
        return run_summary_dao.get_summary_stats(from_ms, to_ms, result_def_id)
    }
}
