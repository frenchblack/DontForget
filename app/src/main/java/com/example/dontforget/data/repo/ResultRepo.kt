package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.ResultDefinitionDao
import com.example.dontforget.data.dao.RunSummaryDao
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.entity.RunSummaryEntity
import kotlinx.coroutines.flow.Flow

class ResultRepo(
    private val def_dao: ResultDefinitionDao,
    private val summary_dao: RunSummaryDao
) {
    fun observe_active_defs(): Flow<List<ResultDefinitionEntity>> = def_dao.observe_active()

    suspend fun save_summary(
        session_id: Long,
        value_code_map: Map<Long, String>,
        value_text_map: Map<Long, String>,
        defs: List<ResultDefinitionEntity>
    ) {
        // 기존 저장된 summary는 덮어쓰기
        summary_dao.delete_by_session(session_id)

        val items = defs.map { def ->
            RunSummaryEntity(
                session_id = session_id,
                result_def_id = def.result_def_id,
                value_code = value_code_map[def.result_def_id] ?: "",
                value = value_text_map[def.result_def_id] ?: ""
            )
        }.filter { it.value_code.isNotBlank() || it.value.isNotBlank() }

        if (items.isNotEmpty()) summary_dao.insert_all(items)
    }

    suspend fun get_summary(session_id: Long): List<RunSummaryEntity> {
        return summary_dao.get_by_session(session_id)
    }
}
