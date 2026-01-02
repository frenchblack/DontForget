package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.RunConditionDao
import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunStatus

class RunRepo(
    private val dao: RunDao,
    private val condition_dao: RunConditionDao
) {
    suspend fun get_in_progress(): RunSessionEntity? {
        return dao.get_latest_by_status(RunStatus.IN_PROGRESS)
    }

    suspend fun start_new_session(now: Long = System.currentTimeMillis()): Long {
        val entity = RunSessionEntity(
            start_time = now,
            end_time = null,
            status = RunStatus.IN_PROGRESS,
            created_at = now
        )
        return dao.insert_session(entity)
    }

    suspend fun finish_session(session_id: Long) {
        dao.finish_session(
            session_id = session_id,
            end_time = System.currentTimeMillis(),
            status = RunStatus.COMPLETED
        )
    }

    // ✅ START 컨디션 저장 (기존꺼 지우고 다시 insert)
    suspend fun save_conditions_start(
        session_id: Long,
        items: List<RunConditionEntity>
    ) {
        condition_dao.delete_by_session_phase(
            session_id = session_id,
            phase = ConditionPhase.START
        )
        if (items.isNotEmpty()) {
            condition_dao.insert_all(items)
        }
    }

    suspend fun get_conditions_start(session_id: Long): List<RunConditionEntity> {
        return condition_dao.get_by_session_phase(
            session_id = session_id,
            phase = ConditionPhase.START
        )
    }

    suspend fun save_conditions(
        session_id: Long,
        phase: ConditionPhase,
        items: List<RunConditionEntity>
    ) {
        condition_dao.delete_by_session_phase(session_id = session_id, phase = phase)
        if (items.isNotEmpty()) condition_dao.insert_all(items)
    }

    suspend fun get_conditions(
        session_id: Long,
        phase: ConditionPhase
    ): List<RunConditionEntity> {
        return condition_dao.get_by_session_phase(session_id = session_id, phase = phase)
    }
}
