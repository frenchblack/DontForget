package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.dao.RunItemProgressDao
import kotlinx.coroutines.flow.Flow
import com.example.dontforget.data.entity.CheckItemEntity

class CheckItemRepo(
    private val dao: CheckItemDao,
    private val progress_dao: RunItemProgressDao
) {

    fun observe_active(): Flow<List<CheckItemEntity>> = dao.observe_active()

    fun observe_mastered(): Flow<List<CheckItemEntity>> = dao.observe_mastered()

    fun observe_completed_ids(session_id: Long): Flow<List<Long>> =
        progress_dao.observe_completed_ids(session_id)

    suspend fun practice_complete(session_id: Long, item_id: Long) {
        progress_dao.mark_completed(session_id, item_id)
        dao.inc_practice_success(item_id)
    }

    suspend fun practice_revert(session_id: Long, item_id: Long) {
        progress_dao.unmark_completed(session_id, item_id)
        dao.inc_practice_revert(item_id)
    }

    // ---- 너 기존 기능들(이미 있던 것들) 그대로 유지/이어서 ----
    suspend fun add_item(title: String, note: String, confidence: Int) {
        dao.insert(CheckItemEntity(title = title, note = note, confidence = confidence))
    }

    suspend fun add_item(title: String) {
        dao.insert(CheckItemEntity(title = title))
    }

    suspend fun to_mastered(item_id: Long) {
        dao.update_status(item_id = item_id, status = "MASTERED")
    }

    suspend fun to_active(item_id: Long) {
        dao.update_status(item_id = item_id, status = "ACTIVE")
    }

    suspend fun update_item(item_id: Long, title: String, note: String, confidence: Int) {
        dao.update_item(item_id = item_id, title = title, note = note, confidence = confidence)
    }

    suspend fun delete_item(item_id: Long) {
        dao.delete_item(item_id)
    }

    suspend fun add_mistake(item_id: Long) {
        dao.inc_mistake(item_id)
    }

    suspend fun revert(item_id: Long) {
        dao.revert_to_active(item_id)
    }

    suspend fun practice_add_item(
        session_id: Long,
        title: String,
        note: String,
        confidence: Int
    ): Long {
        val new_id = dao.insert(
            CheckItemEntity(
                title = title,
                note = note,
                confidence = confidence.coerceIn(0, 5),
                status = "ACTIVE"
            )
        )

        // ✅ 세션에도 명시적으로 추가(완료 아님 = 0)
        progress_dao.upsert(
            com.example.dontforget.data.entity.RunItemProgressEntity(
                session_id = session_id,
                item_id = new_id,
                is_completed = 0
            )
        )

        return new_id
    }
}
