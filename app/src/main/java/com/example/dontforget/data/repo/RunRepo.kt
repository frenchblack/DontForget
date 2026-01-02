package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunStatus

class RunRepo(
    private val dao: RunDao
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

    suspend fun finish_session(session_id: Long, now: Long = System.currentTimeMillis()) {
        dao.finish_session(session_id = session_id, end_time = now, status = RunStatus.COMPLETED)
    }
}
